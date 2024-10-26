package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

/**
 * Data object representing EVE Online item attributes
 */
public final class Attribute {
    public final int attributeID;
    public @Nullable Integer categoryID;
    public @Nullable String attributeName;
    public @Nullable String displayName;
    public @Nullable Integer unitID;
    public @Nullable Integer iconID;
    public boolean published;

    public Attribute(
        int attributeID,
        @Nullable Integer categoryID,
        @Nullable String attributeName,
        @Nullable String displayName,
        @Nullable Integer unitID,
        @Nullable Integer iconID,
        boolean published
    ) {
        this.attributeID = attributeID;
        this.categoryID = categoryID;
        this.attributeName = attributeName;
        this.displayName = displayName;
        this.unitID = unitID;
        this.iconID = iconID;
        this.published = published;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Attribute attribute) {
            return attributeID == attribute.attributeID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return attributeID;
    }

    @Override
    public String toString() {
        return "Attribute{" +
               "attributeID=" + attributeID +
               ", categoryID=" + categoryID +
               ", attributeName='" + attributeName + '\'' +
               ", displayName='" + displayName + '\'' +
               ", unitID=" + unitID +
               ", iconID=" + iconID +
               ", published=" + published +
               '}';
    }
}
