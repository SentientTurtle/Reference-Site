package net.sentientturtle.nee.pages;

import org.jspecify.annotations.NonNull;

/// Interface for datatypes with a page
public interface HasPage {
    @NonNull Page getPage();
}
