package codes.belle.swift.settings;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JComponent;
import javax.swing.JPanel;

public final class SwiftLspSettingsUi {
    private final JBTextField commandField = new JBTextField();
    private final TextFieldWithBrowseButton workingDirField = new TextFieldWithBrowseButton();
    private final JPanel panel;

    public SwiftLspSettingsUi() {
        // We use FormBuilder to build the entire panel.
        // No need for a wrapping JPanel with BorderLayout anymore.
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("SourceKit-LSP command:"), commandField, 1, false)
                .addTooltip("Example: xcrun sourcekit-lsp  OR  /path/to/sourcekit-lsp")

                // Adds a small vertical gap for better readability
                .addSeparator(4)

                .addLabeledComponent(new JBLabel("Working directory override (optional):"), workingDirField, 1, false)
                .addTooltip("Empty = use project base directory. Set to SwiftPM root (folder with Package.swift) if needed.")

                // THE FIX: This adds an expanding spacer at the bottom.
                // It pushes all previous components up to the top of the window.
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JComponent getComponent() {
        return panel;
    }

    public void reset(SwiftLspSettings.State state) {
        commandField.setText(state.sourceKitLspCommand);
        workingDirField.setText(state.workingDirectory);
    }

    public boolean isModified(SwiftLspSettings.State state) {
        String c = commandField.getText().trim();
        String wd = workingDirField.getText().trim();
        return !c.equals(state.sourceKitLspCommand.trim()) || !wd.equals(state.workingDirectory.trim());
    }

    public SwiftLspSettings.State applyTo(SwiftLspSettings.State ignored) {
        // We create a new state object to ensure cleanliness,
        // essentially treating it as an immutable update.
        SwiftLspSettings.State s = new SwiftLspSettings.State();

        String cmd = commandField.getText().trim();
        // Default to xcrun if the user clears the field, to prevent errors
        s.sourceKitLspCommand = cmd.isEmpty() ? "xcrun sourcekit-lsp" : cmd;

        s.workingDirectory = workingDirField.getText().trim();
        return s;
    }
}
