package codes.belle.swift;

import codes.belle.swift.psi.SwiftTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class SwiftParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(SwiftLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new SwiftLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return (root, builder) -> {
            PsiBuilder.Marker rootMarker = builder.mark();

            while (!builder.eof()) {
                IElementType type = builder.getTokenType();

                // THE FIX: Wrap IDENTIFIER tokens in a composite node
                if (type == SwiftTokenTypes.IDENTIFIER) {
                    PsiBuilder.Marker mark = builder.mark();
                    builder.advanceLexer(); // Consume the token
                    mark.done(SwiftTokenTypes.IDENTIFIER_WRAPPER);
                } else {
                    // Just consume other tokens (keywords, symbols, comments)
                    builder.advanceLexer();
                }
            }

            rootMarker.done(root);
            return builder.getTreeBuilt();
        };
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.create(SwiftTokenTypes.COMMENT);
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.create(SwiftTokenTypes.STRING);
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        // THE FIX: Return a specialized element for our wrapper
        if (node.getElementType() == SwiftTokenTypes.IDENTIFIER_WRAPPER) {
            return new SwiftIdentifierPsiElement(node);
        }
        return new com.intellij.extapi.psi.ASTWrapperPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        // THE FIX: Return the properly initialized SwiftFile class
        return new SwiftFile(viewProvider);
    }
}
