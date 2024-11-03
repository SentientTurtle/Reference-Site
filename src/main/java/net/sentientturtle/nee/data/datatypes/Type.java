package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.pages.HasPage;
import net.sentientturtle.nee.pages.Page;
import net.sentientturtle.nee.pages.TypePage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

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


    public static final int[] ACTIVATION_TIME_ATTRIBUTES = new int[] { 51, 73, 2397, 2398, 2399, 2400, 3115 };
    /// There are several attributes that can specify a module activation time, this utility method checks them
    ///
    /// @return Module activation time if present, else 0.0
    public double getModuleActivationTime(DataSupplier data) {
        Map<Integer, Double> typeAttributes = data.getTypeAttributes().getOrDefault(this.typeID, Map.of());
        double activationTime = 0.0;
        for (int activationTimeAttribute : ACTIVATION_TIME_ATTRIBUTES) {
            Double time = typeAttributes.get(activationTimeAttribute);
            if (time != null) {
                activationTime = time;
                break;
            }
        }

        return activationTime;
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
    public @NonNull Page getPage() {
        return new TypePage(this);
    }
}
