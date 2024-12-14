package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class Celestial {
    public final int itemID;
    public final int typeID;
    public final int groupID;
    public final String itemName;
    public final @Nullable Integer celestialIndex;
    public final @Nullable Integer orbitIndex;

    public Celestial(int itemID, int typeID, int groupID, String itemName, @Nullable Integer celestialIndex, @Nullable Integer orbitIndex) {
        this.itemID = itemID;
        this.typeID = typeID;
        this.groupID = groupID;
        this.itemName = Objects.requireNonNull(itemName);
        this.celestialIndex = celestialIndex;
        this.orbitIndex = orbitIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Celestial) obj;
        return this.itemID == that.itemID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemID, typeID, groupID, itemName, celestialIndex, orbitIndex);
    }

    @Override
    public String toString() {
        return "Celestial[" +
               "itemID=" + itemID + ", " +
               "itemName=" + itemName+ ']';
    }

}
