package codes.belle.swift;

import codes.belle.swift.psi.SwiftTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Basic lexer for Swift syntax highlighting.
 *
 * This lexer handles only syntactic elements (keywords, strings, comments, annotations, compiler directives).
 * Semantic highlighting (types, functions, variables, parameters) is delegated to the LSP server via semantic tokens.
 * This hybrid approach provides instant basic highlighting while allowing accurate semantic analysis from SourceKit-LSP.
 */
public class SwiftLexer extends Lexer {
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;
    private int currentOffset;
    private IElementType currentToken;

    private static final Set<String> KEYWORDS = Set.of("associatedtype", "class", "deinit", "enum", "extension", "fileprivate", "func", "import", "init", "inout", "internal", "let", "open", "operator", "override", "private", "protocol", "public", "rethrows", "static", "struct", "subscript", "typealias", "var", "break", "case", "continue", "default", "defer", "do", "else", "fallthrough", "for", "guard", "if", "in", "repeat", "return", "switch", "weak", "where", "while", "as", "any", "catch", "false", "is", "nil", "super", "self", "Self", "throw", "throws", "true", "try", "required", "final");

    private static final Set<String> TYPE_KEYWORDS = Set.of("class", "struct", "enum", "protocol", "extension", "typealias", "actor");

    private static final Set<String> VARIABLE_KEYWORDS = Set.of("let", "var");

    // Primitive types that should be highlighted
    private static final Set<String> PRIMITIVE_TYPES = Set.of("Int", "Int8", "Int16", "Int32", "Int64", "UInt", "UInt8", "UInt16", "UInt32", "UInt64", "Float", "Double", "Bool", "String", "Character", "Void", "Array", "Dictionary", "Set", "Optional", "Any", "AnyObject");

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.currentOffset = startOffset;
        advance();
    }

    @Override
    public void advance() {
        startOffset = currentOffset;
        if (currentOffset >= endOffset) {
            currentToken = null;
            return;
        }

        char c = buffer.charAt(currentOffset);

        // 1. Handle Whitespace
        if (Character.isWhitespace(c)) {
            while (currentOffset < endOffset && Character.isWhitespace(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            currentToken = TokenType.WHITE_SPACE;
            return;
        }

        // 2. Handle Compiler Directives (#selector, #available, etc.)
        if (c == '#' && currentOffset + 1 < endOffset && Character.isJavaIdentifierStart(buffer.charAt(currentOffset + 1))) {
            currentOffset++; // Skip #
            while (currentOffset < endOffset && Character.isJavaIdentifierPart(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            currentToken = SwiftTokenTypes.COMPILER_DIRECTIVE;
            return;
        }

        // 3. Handle Annotations (@name)
        if (c == '@' && currentOffset + 1 < endOffset && Character.isJavaIdentifierStart(buffer.charAt(currentOffset + 1))) {
            currentOffset++; // Skip @
            while (currentOffset < endOffset && Character.isJavaIdentifierPart(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            currentToken = SwiftTokenTypes.ANNOTATION;
            return;
        }

        // 3. Handle Comments (Single line //)
        if (c == '/' && currentOffset + 1 < endOffset && buffer.charAt(currentOffset + 1) == '/') {
            while (currentOffset < endOffset && buffer.charAt(currentOffset) != '\n') {
                currentOffset++;
            }
            currentToken = SwiftTokenTypes.COMMENT;
            return;
        }

        // 4. Handle Strings ("...")
        if (c == '"') {
            currentOffset++; // skip opening quote
            while (currentOffset < endOffset) {
                if (buffer.charAt(currentOffset) == '"' && buffer.charAt(currentOffset - 1) != '\\') {
                    currentOffset++; // skip closing quote
                    break;
                }
                currentOffset++;
            }
            currentToken = SwiftTokenTypes.STRING;
            return;
        }

        // 5. Handle Identifiers and Keywords
        if (Character.isJavaIdentifierStart(c)) {
            currentOffset++;

            while (currentOffset < endOffset && Character.isJavaIdentifierPart(buffer.charAt(currentOffset))) {
                currentOffset++;
            }

            String text = buffer.subSequence(startOffset, currentOffset).toString();

            // Check if this is a keyword first
            if (KEYWORDS.contains(text)) {
                currentToken = SwiftTokenTypes.KEYWORD;
            }
            // Always mark primitive types as PRIMITIVE_TYPE
            else if (PRIMITIVE_TYPES.contains(text)) {
                currentToken = SwiftTokenTypes.PRIMITIVE_TYPE;
            }
            // Check if this is a type name (follows type keyword like "class", "struct", etc.)
            else if (isAfterTypeKeyword()) {
                currentToken = SwiftTokenTypes.TYPE_NAME;
            }
            // Check if uppercase identifier is likely a type
            else if (Character.isUpperCase(text.charAt(0)) && isLikelyType()) {
                currentToken = SwiftTokenTypes.TYPE_NAME;
            }
            // Check if this is a local variable (after let/var keyword)
            else if (isAfterVariableKeyword()) {
                currentToken = SwiftTokenTypes.LOCAL_VARIABLE;
            }
            // Check if this is a parameter (identifier followed by colon)
            else if (isFollowedByColon()) {
                currentToken = SwiftTokenTypes.PARAMETER;
            }
            // Everything else is an identifier
            else {
                currentToken = SwiftTokenTypes.IDENTIFIER;
            }
            return;
        }

        // 6. Handle everything else (symbols like { } ( ) . ,)
        currentOffset++;
        currentToken = SwiftTokenTypes.IDENTIFIER;
    }

    private boolean isLikelyType() {
        // Check what comes after the identifier
        int posAhead = currentOffset;
        while (posAhead < endOffset && Character.isWhitespace(buffer.charAt(posAhead))) {
            posAhead++;
        }

        if (posAhead < endOffset) {
            char nextChar = buffer.charAt(posAhead);
            // Followed by ( = constructor call like SettingsManager()
            if (nextChar == '(') return true;
            // Followed by . = static member access like SettingsManager.shared
            if (nextChar == '.') return true;
            // Followed by , = type list like "NSObject, NSApplicationDelegate"
            if (nextChar == ',') return true;
        }

        // Check what comes before the identifier
        int pos = startOffset - 1;

        // Skip whitespace backwards
        while (pos >= 0 && Character.isWhitespace(buffer.charAt(pos))) {
            pos--;
        }

        if (pos < 0) return false;

        char prevChar = buffer.charAt(pos);

        // After : (variable/parameter type annotation)
        if (prevChar == ':') return true;

        // After , (type list like "NSObject, NSApplicationDelegate")
        if (prevChar == ',') return true;

        // After -> (return type)
        if (pos >= 1 && prevChar == '>' && buffer.charAt(pos - 1) == '-') return true;

        // After . (member access on a type, like SomeType.staticMember)
        if (prevChar == '.') return true;

        return false;
    }

    private boolean isFollowedByColon() {
        // Look ahead to see if there's a colon after optional whitespace
        int pos = currentOffset;

        // Skip whitespace forward
        while (pos < endOffset && Character.isWhitespace(buffer.charAt(pos))) {
            pos++;
        }

        return pos < endOffset && buffer.charAt(pos) == ':';
    }

    private boolean isAfterTypeKeyword() {
        // Look backwards to see if we're after a type keyword like "class", "struct", etc.
        int pos = startOffset - 1;

        // Skip whitespace backwards
        while (pos >= 0 && Character.isWhitespace(buffer.charAt(pos))) {
            pos--;
        }

        if (pos < 0) return false;

        // Find the start of the previous word
        int wordEnd = pos + 1;
        while (pos >= 0 && Character.isJavaIdentifierPart(buffer.charAt(pos))) {
            pos--;
        }

        String prevWord = buffer.subSequence(pos + 1, wordEnd).toString();
        return TYPE_KEYWORDS.contains(prevWord);
    }

    private boolean isAfterVariableKeyword() {
        // Look backwards to see if we're after "let" or "var"
        int pos = startOffset - 1;

        // Skip whitespace backwards
        while (pos >= 0 && Character.isWhitespace(buffer.charAt(pos))) {
            pos--;
        }

        if (pos < 0) return false;

        // Find the start of the previous word
        int wordEnd = pos + 1;
        while (pos >= 0 && Character.isJavaIdentifierPart(buffer.charAt(pos))) {
            pos--;
        }

        String prevWord = buffer.subSequence(pos + 1, wordEnd).toString();
        return VARIABLE_KEYWORDS.contains(prevWord);
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return currentToken;
    }

    @Override
    public int getTokenStart() {
        return startOffset;
    }

    @Override
    public int getTokenEnd() {
        return currentOffset;
    }

    @Override
    public @NotNull LexerPosition getCurrentPosition() {
        return new LexerPositionImpl(currentOffset, 0);
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
        this.currentOffset = position.getOffset();
        advance();
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }

    private record LexerPositionImpl(int offset, int state) implements LexerPosition {
        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getState() {
            return state;
        }
    }
}
