package codes.belle.swift;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.platform.lsp.api.LspServer;
import com.intellij.platform.lsp.api.LspServerManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.util.PsiUtilCore;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SwiftLspDefinitionTarget extends LightElement implements Navigatable {
    private static final Logger LOG = Logger.getInstance(SwiftLspDefinitionTarget.class);
    private final @NotNull SwiftIdentifierPsiElement source;

    public SwiftLspDefinitionTarget(@NotNull SwiftIdentifierPsiElement source) {
        super(source.getManager(), source.getLanguage());
        this.source = source;
    }

    @Override
    public @NotNull Project getProject() {
        return source.getProject();
    }

    @Override
    public @NotNull PsiElement getParent() {
        return source;
    }

    @Override
    public @Nullable PsiFile getContainingFile() {
        return source.getContainingFile();
    }

    @Override
    public @NotNull PsiElement getNavigationElement() {
        return source;
    }

    @Override
    public @NotNull PsiElement getOriginalElement() {
        return source;
    }

    private @Nullable VirtualFile getVirtualFile() {
        PsiFile psiFile = getContainingFile();
        return psiFile == null ? null : PsiUtilCore.getVirtualFile(psiFile);
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
    public void navigate(boolean requestFocus) {
        Project project = getProject();
        PsiFile psiFile = getContainingFile();
        VirtualFile vf = getVirtualFile();

        if (vf == null || psiFile == null) return;

        // 1. Get current cursor position
        Document doc = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (doc == null) return;

        int offset = source.getTextOffset();
        int line = doc.getLineNumber(offset);
        int col = offset - doc.getLineStartOffset(line);

        // 2. Run LSP request in background
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                // Find the LSP server
                Collection<LspServer> servers = LspServerManager.getInstance(project).getServersForProvider(SwiftLspServerSupportProvider.class);
                if (servers.isEmpty()) return;

                LspServer server = servers.iterator().next();

                // Prepare LSP Params
                DefinitionParams params = new DefinitionParams();
                params.setTextDocument(new TextDocumentIdentifier(vf.getUrl()));
                params.setPosition(new Position(line, col));

                // Access the language server via reflection since the API is Kotlin-based
                org.eclipse.lsp4j.services.LanguageServer lsp4jServer;
                try {
                    java.lang.reflect.Method method = server.getClass().getMethod("getLanguageServer");
                    lsp4jServer = (org.eclipse.lsp4j.services.LanguageServer) method.invoke(server);
                } catch (Exception e) {
                    LOG.warn("Failed to get language server", e);
                    return;
                }

                if (lsp4jServer == null) return;

                CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> request = lsp4jServer.getTextDocumentService().definition(params);

                if (request == null) return;

                // Wait for response (timeout 3s)
                Either<List<? extends Location>, List<? extends LocationLink>> response = request.get(3, TimeUnit.SECONDS);

                if (response == null) return;

                // 3. Handle response logic
                if (response.isLeft()) {
                    List<? extends Location> locations = response.getLeft();
                    if (!locations.isEmpty()) {
                        openLocation(project, locations.getFirst(), requestFocus);
                    }
                } else if (response.isRight()) {
                    List<? extends LocationLink> links = response.getRight();
                    if (!links.isEmpty()) {
                        openLocationLink(project, links.getFirst(), requestFocus);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Swift LSP navigation failed", e);
            }
        });
    }

    private void openLocation(@NotNull Project project, @NotNull Location loc, boolean requestFocus) {
        // Convert URI string to VirtualFile
        VirtualFile targetFile = VirtualFileManager.getInstance().findFileByUrl(loc.getUri());
        if (targetFile == null) return;

        int line = loc.getRange().getStart().getLine();
        int col = loc.getRange().getStart().getCharacter();

        // Must open editor on the UI thread
        ApplicationManager.getApplication().invokeLater(() -> new OpenFileDescriptor(project, targetFile, line, col).navigate(requestFocus));
    }

    private void openLocationLink(@NotNull Project project, @NotNull LocationLink link, boolean requestFocus) {
        VirtualFile targetFile = VirtualFileManager.getInstance().findFileByUrl(link.getTargetUri());
        if (targetFile == null) return;

        int line = link.getTargetSelectionRange().getStart().getLine();
        int col = link.getTargetSelectionRange().getStart().getCharacter();

        ApplicationManager.getApplication().invokeLater(() -> new OpenFileDescriptor(project, targetFile, line, col).navigate(requestFocus));
    }

    @Override
    public @NotNull String toString() {
        return "SwiftLspDefinitionTarget(" + source.getText() + ")";
    }
}
