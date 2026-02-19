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
    public static final TextAttributesKey ANNOTATION =
            TextAttributesKey.createTextAttributesKey("SWIFT_ANNOTATION", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey TYPE_NAME =
            TextAttributesKey.createTextAttributesKey("SWIFT_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey PARAMETER =
            TextAttributesKey.createTextAttributesKey("SWIFT_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey COMPILER_DIRECTIVE =
            TextAttributesKey.createTextAttributesKey("SWIFT_COMPILER_DIRECTIVE", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey PRIMITIVE_TYPE =
            TextAttributesKey.createTextAttributesKey("SWIFT_PRIMITIVE_TYPE", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey LOCAL_VARIABLE =
            TextAttributesKey.createTextAttributesKey("SWIFT_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

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
        if (tokenType.equals(SwiftTokenTypes.ANNOTATION)) return pack(ANNOTATION);
        if (tokenType.equals(SwiftTokenTypes.TYPE_NAME)) return pack(TYPE_NAME);
        if (tokenType.equals(SwiftTokenTypes.PARAMETER)) return pack(PARAMETER);
        if (tokenType.equals(SwiftTokenTypes.COMPILER_DIRECTIVE)) return pack(COMPILER_DIRECTIVE);
        if (tokenType.equals(SwiftTokenTypes.PRIMITIVE_TYPE)) return pack(PRIMITIVE_TYPE);
        if (tokenType.equals(SwiftTokenTypes.LOCAL_VARIABLE)) return pack(LOCAL_VARIABLE);
        return new TextAttributesKey[0];
    }
}
