package codes.belle.swift;

import com.intellij.openapi.project.Project;
import com.intellij.platform.lsp.api.LspServerManager;

import java.lang.reflect.Method;
import java.util.List;

public final class SwiftLspRestart {
    private SwiftLspRestart() {
    }

    public static void restart(Project project) {
        if (project == null) return;

        LspServerManager manager = LspServerManager.getInstance(project);
        SwiftLspServerDescriptor descriptor = new SwiftLspServerDescriptor(project);

        // stop (best-effort)
        invoke(manager, "stopServer", new Class<?>[]{descriptor.getClass()}, new Object[]{descriptor});
        invoke(manager, "stopServer", new Class<?>[]{descriptor.getClass().getSuperclass()}, new Object[]{descriptor});
        invoke(manager, "stopServers", new Class<?>[]{List.class}, new Object[]{List.of(descriptor)});

        // start (best-effort)
        invoke(manager, "ensureServerStarted", new Class<?>[]{descriptor.getClass()}, new Object[]{descriptor});
        invoke(manager, "ensureServerStarted", new Class<?>[]{descriptor.getClass().getSuperclass()}, new Object[]{descriptor});
    }

    private static void invoke(Object target, String method, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = target.getClass().getMethod(method, paramTypes);
            m.setAccessible(true);
            m.invoke(target, args);
        } catch (Throwable ignored) {
        }
    }
}
