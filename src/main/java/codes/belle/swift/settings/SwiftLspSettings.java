package codes.belle.swift.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "SwiftLspSettings", storages = @Storage("swift-lsp.xml"))
public final class SwiftLspSettings implements PersistentStateComponent<SwiftLspSettings.State> {

    public static final class State {
        public String sourceKitLspCommand = "xcrun sourcekit-lsp";
        public String workingDirectory = "";
    }

    private State state = new State();

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public void update(@NotNull State newState) {
        this.state = newState;
        ApplicationManager.getApplication()
                .getMessageBus()
                .syncPublisher(SwiftLspSettingsTopic.TOPIC)
                .settingsChanged(newState);
    }

    public static SwiftLspSettings getInstance() {
        return ApplicationManager.getApplication().getService(SwiftLspSettings.class);
    }
}
