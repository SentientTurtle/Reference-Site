package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.SDEData;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Data object representing EVE Online SolarSystems
 */
@SuppressWarnings("WeakerAccess")
public final class SolarSystem implements MapItem {
    public final int regionID;
    public final int constellationID;
    public final int solarSystemID;
    public final String solarSystemName;
    public final double x;
    public final double y;
    public final double z;
    public final double security;
    public final @Nullable Integer factionID;
    public final @Nullable Integer sunTypeID;
    public final @Nullable Integer wormholeClassID;

    /**
     *
     */
    public SolarSystem(
        int regionID,
        int constellationID,
        int solarSystemID,
        String solarSystemName,
        double x,
        double y,
        double z,
        double security,
        @Nullable Integer factionID,
        @Nullable Integer sunTypeID, @Nullable Integer wormholeClassID
    ) {
        this.regionID = regionID;
        this.constellationID = constellationID;
        this.solarSystemID = solarSystemID;
        this.solarSystemName = solarSystemName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.security = security;
        this.factionID = factionID;
        this.sunTypeID = sunTypeID;
        this.wormholeClassID = wormholeClassID;
    }

    @Override
    public int getID() {
        return solarSystemID;
    }

    @Override
    public OptionalInt getSovFactionID() {
        return factionID != null ? OptionalInt.of(factionID) : OptionalInt.empty();
    }

    @Override
    public OptionalDouble getSecurity(SDEData SDEData) {
        return OptionalDouble.of(security);
    }

    @Override
    public String getConstituentName() {
        return "Celestials";
    }

    @Override
    public Stream<MapItem.MapConstituent> getConstituents(SDEData sde) {
        return sde.getCelestials().getOrDefault(this.solarSystemID, Set.of())
            .stream()
            .sorted(
                Comparator.<Celestial>comparingInt(celestial -> {
                        if (celestial.groupID == 995) {
                            return Integer.MAX_VALUE;
                        } else {
                            return celestial.celestialIndex == null ? 0 : celestial.celestialIndex;
                        }
                    })
                    .thenComparingInt(celestial -> celestial.orbitIndex == null ? 0 : celestial.orbitIndex)
            )
            .map(celestial -> {
                String itemName = celestial.itemName;

                Type type = sde.getTypes().get(celestial.typeID);
                if (type != null && type.groupID == 995) {
                    itemName = type.name;
                }

                return new MapItem.MapConstituent(
                    switch (celestial.groupID) {
                        case 6, 995 -> "sun.png";
                        case 7 -> "planet.png";
                        case 8 -> "moon.png";
                        case 9 -> "asteroidbelt.png";
                        default -> throw new IllegalStateException("Unknown celestial groupID: " + celestial.groupID + " " + celestial);
                    },
                    itemName,
                    null,
                    (celestial.celestialIndex != null ? 1 : 0) + (celestial.orbitIndex != null ? 1 : 0)
                );
            });
    }

    @Override
    public String getName() {
        return solarSystemName;
    }

    @Override
    public @Nullable MapItem getParent(HtmlContext context) {
        return context.sde.getConstellations().get(this.constellationID);
    }

    @Override
    public @Nullable ResourceLocation getIcon(HtmlContext context) {
        if (sunTypeID == null) {
            return null;
        } else {
            return ResourceLocation.typeIcon(sunTypeID, context);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof SolarSystem that) {
            return this.solarSystemID == that.solarSystemID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return solarSystemID;
    }

    @Override
    public String toString() {
        return "SolarSystem[" +
               "regionID=" + regionID + ", " +
               "constellationID=" + constellationID + ", " +
               "solarSystemID=" + solarSystemID + ", " +
               "solarSystemName=" + solarSystemName + ", " +
               "x=" + x + ", " +
               "y=" + y + ", " +
               "z=" + z + ", " +
               "security=" + security + ", " +
               "factionID=" + factionID + ", " +
               "sunTypeID=" + sunTypeID + ']';
    }

}
