package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.html.Frame;
import net.sentientturtle.nee.page.HasPage;
import net.sentientturtle.nee.page.TypePage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

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

    public static Comparator<Type> comparator(SDEData data) {
        Map<Integer, Integer> metaTypes = data.getMetaTypes();
        Map<Integer, Map<Integer, Double>> typeAttributes = data.getTypeAttributes();

        return (t1, t2) -> {
            int m1 = metaTypes.getOrDefault(t1.typeID, 1);
            m1 = switch (m1) {
                case 52 -> 54;
                case 53 -> 53;
                case 54 -> 52;
                default -> m1;
            };

            int m2 = metaTypes.getOrDefault(t2.typeID, 1);
            m2 = switch (m2) {
                case 52 -> 54;
                case 53 -> 53;
                case 54 -> 52;
                default -> m2;
            };

            // Compare by meta group
            int i = Integer.compare(m1, m2);
            if (i == 0) {
                // Compare by meta level
                int l1 = (int) (double) typeAttributes.getOrDefault(t1.typeID, Map.of()).getOrDefault(633, 0.0);
                int l2 = (int) (double) typeAttributes.getOrDefault(t2.typeID, Map.of()).getOrDefault(633, 0.0);
                i = Integer.compare(l1, l2);
                if (i == 0) {
                    // If no other ordering, order by typeID
                    return Integer.compare(t1.typeID, t2.typeID);
                } else {
                    return i;
                }
            } else {
                return i;
            }
        };
    }


    public static final int[] ACTIVATION_TIME_ATTRIBUTES = new int[] { 51, 73, 2397, 2398, 2399, 2400, 3115 };
    /// There are several attributes that can specify a module activation time, this utility method checks them
    ///
    /// @return Module activation time if present, else 0.0
    public double getModuleActivationTime(SDEData data) {
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
               ", name='" + name + '\'' +
               '}';
    }

    @Override
    public @NonNull Frame getPage() {
        return new TypePage(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Type type)) return false;
        return typeID == type.typeID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(typeID);
    }
}
