package codes.belle.swift.psi;

import codes.belle.swift.SwiftLanguage;
import com.intellij.psi.tree.IElementType;

public interface SwiftTokenTypes {
    // Leaf tokens (from Lexer)
    IElementType IDENTIFIER = new IElementType("IDENTIFIER", SwiftLanguage.INSTANCE);
    IElementType KEYWORD = new IElementType("KEYWORD", SwiftLanguage.INSTANCE);
    IElementType COMMENT = new IElementType("COMMENT", SwiftLanguage.INSTANCE);
    IElementType STRING = new IElementType("STRING", SwiftLanguage.INSTANCE);
    IElementType ANNOTATION = new IElementType("ANNOTATION", SwiftLanguage.INSTANCE);
    IElementType TYPE_NAME = new IElementType("TYPE_NAME", SwiftLanguage.INSTANCE);

    // Composite elements (created by Parser)
    IElementType IDENTIFIER_WRAPPER = new IElementType("IDENTIFIER_WRAPPER", SwiftLanguage.INSTANCE);
}
