package codes.belle.swift;

import codes.belle.swift.psi.SwiftTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class SwiftLexer extends Lexer {
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;
    private int currentOffset;
    private IElementType currentToken;

    private static final Set<String> KEYWORDS = Set.of("associatedtype", "class", "deinit", "enum", "extension", "fileprivate", "func", "import", "init", "inout", "internal", "let", "open", "operator", "override", "private", "protocol", "public", "rethrows", "static", "struct", "subscript", "typealias", "var", "break", "case", "continue", "default", "defer", "do", "else", "fallthrough", "for", "guard", "if", "in", "repeat", "return", "switch", "weak", "where", "while", "as", "any", "catch", "false", "is", "nil", "super", "self", "Self", "throw", "throws", "true", "try", "required", "final");

    private static final Set<String> TYPE_KEYWORDS = Set.of("class", "struct", "enum", "protocol", "extension", "typealias", "actor");

    // Primitive types that should always be highlighted as types
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

            // Always mark primitive types as PRIMITIVE_TYPE
            if (PRIMITIVE_TYPES.contains(text)) {
                currentToken = SwiftTokenTypes.PRIMITIVE_TYPE;
            }
            // Check if this is a type name (follows type keyword like "class", "struct", etc.)
            // This must be checked BEFORE parameter check to handle "class AppDelegate:" correctly
            else if (isAfterTypeKeyword()) {
                currentToken = SwiftTokenTypes.TYPE_NAME;
            }
            // Check if uppercase identifier is in type position (after : or ->)
            else if (Character.isUpperCase(text.charAt(0)) && isInTypePosition()) {
                currentToken = SwiftTokenTypes.TYPE_NAME;
            }
            // Check if this is a parameter (identifier followed by colon, but not a type)
            else if (isFollowedByColon()) {
                currentToken = SwiftTokenTypes.PARAMETER;
            } else if (KEYWORDS.contains(text)) {
                currentToken = SwiftTokenTypes.KEYWORD;
            } else {
                currentToken = SwiftTokenTypes.IDENTIFIER;
            }
            return;
        }

        // 6. Handle everything else (symbols like { } ( ) . ,)
        currentOffset++;
        currentToken = SwiftTokenTypes.IDENTIFIER;
    }

    private boolean isInTypePosition() {
        // Check if we're after : or -> which indicates a type position
        int pos = startOffset - 1;

        // Skip whitespace backwards
        while (pos >= 0 && Character.isWhitespace(buffer.charAt(pos))) {
            pos--;
        }

        if (pos < 0) return false;

        // Check for : (variable/parameter type annotation)
        if (buffer.charAt(pos) == ':') return true;

        // Check for -> (return type)
        if (pos >= 1 && buffer.charAt(pos) == '>' && buffer.charAt(pos - 1) == '-') return true;

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
