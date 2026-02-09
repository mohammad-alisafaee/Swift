package codes.belle.swift;

import codes.belle.swift.settings.SwiftLspSettingsTopic;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public final class SwiftLspServerSupportProvider implements LspServerSupportProvider {

    private static final ConcurrentHashMap<Project, Boolean> SUBSCRIBED = new ConcurrentHashMap<>();

    @Override
    public void fileOpened(@NotNull Project project, @NotNull VirtualFile file, @NotNull LspServerSupportProvider.LspServerStarter serverStarter) {
        if (!"swift".equalsIgnoreCase(file.getExtension())) return;

        serverStarter.ensureServerStarted(new SwiftLspServerDescriptor(project));
        ensureSubscribed(project);
    }

    private static void ensureSubscribed(Project project) {
        if (SUBSCRIBED.putIfAbsent(project, Boolean.TRUE) != null) return;

        project.getMessageBus().connect().subscribe(SwiftLspSettingsTopic.TOPIC, newState -> SwiftLspRestart.restart(project));
    }
}
