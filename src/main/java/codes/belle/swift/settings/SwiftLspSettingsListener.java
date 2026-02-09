package codes.belle.swift.settings;

public interface SwiftLspSettingsListener {
    void settingsChanged(SwiftLspSettings.State newState);
}
