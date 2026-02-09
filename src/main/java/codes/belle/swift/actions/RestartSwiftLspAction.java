package codes.belle.swift.actions;

import codes.belle.swift.SwiftLspRestart;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public final class RestartSwiftLspAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        SwiftLspRestart.restart(project);
    }
}
