package codes.belle.swift.settings;

import com.intellij.util.messages.Topic;

public final class SwiftLspSettingsTopic {
    private SwiftLspSettingsTopic() {
    }

    public static final Topic<SwiftLspSettingsListener> TOPIC =
            Topic.create("SwiftLspSettingsChanged", SwiftLspSettingsListener.class);
}
