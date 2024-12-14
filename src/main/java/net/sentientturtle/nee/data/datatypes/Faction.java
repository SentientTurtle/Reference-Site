package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

/**
 * Data object to represent EVE Online Factions
 */
public final class Faction {
    public final int factionID;
    public final String factionName;
    public final int iconID;

    public Faction(int factionID, String factionName, int iconID) {
        this.factionID = factionID;
        this.factionName = factionName;
        this.iconID = iconID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof Faction faction) {
            return this.factionID == faction.factionID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return factionID;
    }
}
