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

        // 2. Handle Comments (Single line //)
        if (c == '/' && currentOffset + 1 < endOffset && buffer.charAt(currentOffset + 1) == '/') {
            while (currentOffset < endOffset && buffer.charAt(currentOffset) != '\n') {
                currentOffset++;
            }
            currentToken = SwiftTokenTypes.COMMENT;
            return;
        }

        // 3. Handle Strings ("...")
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

        // 4. Handle Identifiers and Keywords
        if (Character.isJavaIdentifierStart(c)) {
            // FIX: Always consume at least one character (the start char)
            currentOffset++;

            while (currentOffset < endOffset && Character.isJavaIdentifierPart(buffer.charAt(currentOffset))) {
                currentOffset++;
            }

            String text = buffer.subSequence(startOffset, currentOffset).toString();
            if (KEYWORDS.contains(text)) {
                currentToken = SwiftTokenTypes.KEYWORD;
            } else {
                currentToken = SwiftTokenTypes.IDENTIFIER;
            }
            return;
        }

        // 5. Handle everything else (symbols like { } ( ) . ,)
        // SAFETY NET: If we reached here, we MUST consume 1 char.
        currentOffset++;
        currentToken = SwiftTokenTypes.IDENTIFIER;
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
