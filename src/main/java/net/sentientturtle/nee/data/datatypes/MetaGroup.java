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

    /// Comparator for metagroups; Structure metaGroup order is reversed
    /// <br>
    /// 52	Structure Faction -> 54
    /// <br>
    /// 53	Structure Tech II -> 53
    /// <br>
    /// 54	Structure Tech I -> 52
    public static <T> Comparator<T> orderedByMetaGroup(ToIntFunction<T> toMetaGroupID) {
        return Comparator.comparingInt(
            v -> {
                int metaGroupID = toMetaGroupID.applyAsInt(v);
                return switch (metaGroupID) {
                    case 52 -> 54;
                    case 53 -> 53;
                    case 54 -> 52;
                    default -> metaGroupID;
                };
            }
        );
    }
}
