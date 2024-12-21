package net.sentientturtle.nee.page;

import net.sentientturtle.html.Frame;
import org.jspecify.annotations.NonNull;

/// Interface for datatypes with a page
public interface HasPage {
    @NonNull
    Frame getPage();
}
