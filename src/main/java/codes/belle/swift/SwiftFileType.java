package codes.belle.swift;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class SwiftFileType extends LanguageFileType {
    public static final SwiftFileType INSTANCE = new SwiftFileType();

    private SwiftFileType() {
        super(SwiftLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "Swift";
    }

    @Override
    public @NotNull String getDescription() {
        return "Swift source file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "swift";
    }

    @Override
    public Icon getIcon() {
        return SwiftIcons.SwiftFile;
    }
}
