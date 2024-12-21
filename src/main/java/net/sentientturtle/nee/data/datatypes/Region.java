package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.SDEData;
import net.sentientturtle.nee.page.MapPage;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Data object representing EVE Online Regions
 */
@SuppressWarnings("WeakerAccess")
public class Region implements MapItem {
    public final int regionID;
    public final String regionName;
    public final double x;
    public final double y;
    public final double z;
    public final double xMin;
    public final double yMin;
    public final double zMin;
    public final double xMax;
    public final double yMax;
    public final double zMax;
    /// Can be left null to indicate this Region does not belong to a faction
    public final @Nullable Integer factionID;
    public final @Nullable Integer wormholeClassID;

    public Region(int regionID, String regionName, double x, double y, double z, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, @Nullable Integer factionID, @Nullable Integer wormholeClassID) {
        this.regionID = regionID;
        this.regionName = regionName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = -zMax;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = -zMin;
        this.factionID = factionID;
        this.wormholeClassID = wormholeClassID;
    }

    @Override
    public int getID() {
        return regionID;
    }

    @Nullable
    @Override
    public OptionalInt getSovFactionID() {
        return factionID != null ? OptionalInt.of(factionID) : OptionalInt.empty();
    }

    @Override
    @Nullable
    public OptionalDouble getSecurity(SDEData SDEData) {
        return SDEData.getRegionSolarSystemMap().get(regionID).stream().mapToDouble(solarsystem -> solarsystem.security).average();
    }

    @Override
    public String getConstituentName() {
        return "Constellations";
    }

    public Stream<MapItem.MapConstituent> getConstituents(SDEData sde) {
        return sde.getRegionConstellationMap()
            .get(this.regionID)
            .stream()
            .sorted(Comparator.comparing(c -> c.constellationName))
            .map(constellation -> new MapItem.MapConstituent(
                "constellation.png",
                constellation.constellationName,
                new MapPage(constellation),
                0
            ));
    }

    @Override
    public String getName() {
        return regionName;
    }

    @Override
    public @Nullable MapItem getParent(HtmlContext context) {
        if (Cluster.KSPACE_REGIONS.contains(this.regionID)) return Cluster.K_SPACE;
        if (Cluster.WSPACE_REGIONS.contains(this.regionID)) return Cluster.W_SPACE;
        return null;
    }

    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.ofIconID(2355, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region region)) return false;
        return regionID == region.regionID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(regionID);
    }
}
