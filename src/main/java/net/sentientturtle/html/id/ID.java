package net.sentientturtle.html.id;

/// Wrapper class for HTML IDs guaranteeing uniqueness, obtained through {@link IDContext}
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
