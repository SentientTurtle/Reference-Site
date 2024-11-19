package net.sentientturtle.html;

import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.page.PageKind;

/// Top level interface for (HTML) Documents
public interface Document {
    /// Document "name", does not have to be unique
    String name();
    /// Document filename, without extension, must be unique
    String filename();
    PageKind getPageKind();

    /// Document, as used in {@code <title>}
    default String title() {
        return Main.WEBSITE_ABBREVIATION + " - " + this.name();
    }

    /// Absolute path to this document
    default String getPath() {
        return this.getPageKind().getPageFilePath(this.filename());
    }
}
