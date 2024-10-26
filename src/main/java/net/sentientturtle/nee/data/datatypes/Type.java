package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.nee.pages.HasPage;
import net.sentientturtle.nee.pages.Page;
import net.sentientturtle.nee.pages.TypePage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Data object representing EVE Online items
 */
public class Type implements HasPage {
    public final int typeID;
    public int groupID;
    public String name;
    public String description;
    public double mass;
    public double volume;
    public double capacity;
    public boolean published;
    public @Nullable Integer iconID;
    public @Nullable Integer graphicID;
    public @Nullable Integer marketGroupID;

    public Type(int typeID, int groupID, String name, String description, double mass, double volume, double capacity, boolean published, @Nullable Integer iconID, @Nullable Integer graphicID, @Nullable Integer marketGroupID) {
        this.typeID = typeID;
        this.groupID = groupID;
        this.name = name;
        this.description = description;
        this.mass = mass;
        this.volume = volume;
        this.capacity = capacity;
        this.published = published;
        this.iconID = iconID;
        this.graphicID = graphicID;
        this.marketGroupID = marketGroupID;
    }

    @Override
    public String toString() {
        return "Type{" +
                "typeID=" + typeID +
                ", groupID=" + groupID +
                ", name='" + name +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return typeID == type.typeID;
    }

    @Override
    public int hashCode() {
        return typeID;
    }

    @Override
    public @NonNull Page getPage() {
        return new TypePage(this);
    }
}
