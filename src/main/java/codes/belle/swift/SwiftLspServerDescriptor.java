package codes.belle.swift;

import codes.belle.swift.settings.SwiftLspSettings;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class SwiftLspServerDescriptor extends ProjectWideLspServerDescriptor {

    public SwiftLspServerDescriptor(@NotNull Project project) {
        super(project, "SourceKit-LSP");
    }

    @Override
    public boolean isSupportedFile(@NotNull VirtualFile file) {
        return "swift".equalsIgnoreCase(file.getExtension());
    }

    @Override
    public @NotNull GeneralCommandLine createCommandLine() {
        SwiftLspSettings.State s = SwiftLspSettings.getInstance().getState();
        if (s == null) s = new SwiftLspSettings.State();

        // Use the safe tokenizer I provided earlier
        List<String> tokens = tokenizeCommand(s.sourceKitLspCommand);
        GeneralCommandLine cmd = new GeneralCommandLine(tokens);

        // ... existing directory logic ...

        // ADD THIS: Pass the SDK path environment variable
        // This helps SourceKit find the standard library on macOS
        cmd.withEnvironment("SOURCEKIT_LOGGING", "1"); // Optional: enables internal logging

        // If you are on macOS, this is often needed if not running from Xcode
        // cmd.withEnvironment("SDKROOT", "/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk");

        return cmd;
    }

    private static List<String> tokenizeCommand(String command) {
        if (command == null || command.isBlank()) return List.of("xcrun", "sourcekit-lsp");

        var tokenizer = new CommandLineTokenizer(command);
        List<String> args = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            args.add(tokenizer.nextToken());
        }
        return args;
    }
}
