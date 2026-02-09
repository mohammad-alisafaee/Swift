package codes.belle.swift;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class SwiftFile extends PsiFileBase {
    public SwiftFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, SwiftLanguage.INSTANCE); // This constructor automatically initializes the element type!
    }

    @Override
    public @NotNull FileType getFileType() {
        return SwiftFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Swift File";
    }
}
