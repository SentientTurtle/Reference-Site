package net.sentientturtle.html.context;

/// Wrapper class for HTML IDs guaranteeing uniqueness, obtained through {@link HtmlContext}
///
/// Methods that need unique IDs should use this type as parameter
public class ID {
    private final String idString;

    ID(String id) {
        this.idString = id;
    }

    @Override
    public String toString() {
        return idString;
    }
}
