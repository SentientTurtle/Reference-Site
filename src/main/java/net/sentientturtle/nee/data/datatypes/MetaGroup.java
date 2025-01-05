package net.sentientturtle.nee.data.datatypes;

import java.util.Comparator;
import java.util.function.ToIntFunction;

/**
 * Data object representing EVE Online MetaGroups, also known as item tiers.
 */
public class MetaGroup {
    public final int metaGroupID;
    public final String metaGroupName;

    public MetaGroup(int metaGroupID, String metaGroupName) {
        this.metaGroupID = metaGroupID;
        this.metaGroupName = metaGroupName;
    }

    public int getMetaLevel() {
        return switch (this.metaGroupID) {
            case 2 -> 5;
            case 3, 4, 14 -> 6;
            case 5, 6 -> 7;
            default -> 0;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MetaGroup metaGroup) {
            return metaGroupID == metaGroup.metaGroupID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return metaGroupID;
    }

    @Override
    public String toString() {
        return "MetaGroup{" +
                "metaGroupID=" + metaGroupID +
                ", metaGroupName='" + metaGroupName + '\'' +
                '}';
    }
}
