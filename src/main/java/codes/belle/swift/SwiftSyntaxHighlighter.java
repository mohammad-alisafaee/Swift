package codes.belle.swift;

import codes.belle.swift.psi.SwiftTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class SwiftSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEYWORD =
            TextAttributesKey.createTextAttributesKey("SWIFT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey IDENTIFIER =
            TextAttributesKey.createTextAttributesKey("SWIFT_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey COMMENT =
            TextAttributesKey.createTextAttributesKey("SWIFT_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey STRING =
            TextAttributesKey.createTextAttributesKey("SWIFT_STRING", DefaultLanguageHighlighterColors.STRING);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new SwiftLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(SwiftTokenTypes.KEYWORD)) return pack(KEYWORD);
        if (tokenType.equals(SwiftTokenTypes.IDENTIFIER)) return pack(IDENTIFIER);
        if (tokenType.equals(SwiftTokenTypes.COMMENT)) return pack(COMMENT);
        if (tokenType.equals(SwiftTokenTypes.STRING)) return pack(STRING);
        return new TextAttributesKey[0];
    }
}
