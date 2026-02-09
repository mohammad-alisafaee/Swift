package codes.belle.swift;

import com.intellij.lang.Language;

public final class SwiftLanguage extends Language {
    public static final SwiftLanguage INSTANCE = new SwiftLanguage();

    private SwiftLanguage() {
        super("Swift");
    }
}
