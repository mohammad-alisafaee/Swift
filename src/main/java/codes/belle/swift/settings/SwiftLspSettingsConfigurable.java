package codes.belle.swift.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public final class SwiftLspSettingsConfigurable implements Configurable {
    private SwiftLspSettingsUi ui;

    @Override
    public @Nls String getDisplayName() {
        return "Swift LSP (SourceKit-LSP)";
    }

    @Override
    public @Nullable JComponent createComponent() {
        ui = new SwiftLspSettingsUi();
        return ui.getComponent();
    }

    @Override
    public boolean isModified() {
        SwiftLspSettings.State s = SwiftLspSettings.getInstance().getState();
        return ui != null && s != null && ui.isModified(s);
    }

    @Override
    public void reset() {
        SwiftLspSettings.State s = SwiftLspSettings.getInstance().getState();
        if (ui != null && s != null) ui.reset(s);
    }

    @Override
    public void apply() {
        SwiftLspSettings settings = SwiftLspSettings.getInstance();
        SwiftLspSettings.State current = settings.getState();
        if (current == null) current = new SwiftLspSettings.State();
        SwiftLspSettings.State newState = ui.applyTo(current);
        settings.update(newState);
    }

    @Override
    public void disposeUIResources() {
        ui = null;
    }
}
