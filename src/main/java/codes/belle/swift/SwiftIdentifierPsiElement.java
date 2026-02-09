package codes.belle.swift;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwiftIdentifierPsiElement extends ASTWrapperPsiElement implements PsiReference {
    public SwiftIdentifierPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull PsiReference getReference() {
        return this;
    }

    @Override
    public @NotNull PsiElement getElement() {
        return this;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        int length = getTextLength();
        return length <= 0 ? TextRange.EMPTY_RANGE : TextRange.from(0, length);
    }

    @Override
    public @Nullable PsiElement resolve() {
        // Return a navigable target that will call SourceKit-LSP definition on navigate()
        return new SwiftLspDefinitionTarget(this);
    }

    @Override
    public @NotNull String getCanonicalText() {
        return getText();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) throws IncorrectOperationException {
        throw new IncorrectOperationException("Rename not implemented yet");
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        throw new IncorrectOperationException("BindToElement not implemented");
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return element instanceof SwiftLspDefinitionTarget;
    }

    @Override
    public boolean isSoft() {
        return true;
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public String toString() {
        return "SwiftIdentifier";
    }
}
