package net.sentientturtle.html;

import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.page.PageKind;
import org.jspecify.annotations.Nullable;

/// Top level interface for HTML Documents
public interface Document {
    /// Document name, does not have to be unique
    String name();
    /// Document filename, without extension, must be unique
    String filename();
    /// PageKind enum for this document
    PageKind getPageKind();

    /// Document title, as used in {@code <title>}
    default String title() {
        return Main.WEBSITE_ABBREVIATION + " - " + this.name();
    }

    /// Absolute path to this document
    default String getPath() {
        return this.getPageKind().getPageFilePath(this.filename());
    }

    /// URL-escaped path to this document
    default String getURLPath() {
        return this.getPageKind().getPageURLPath(this.filename());
    }
}
