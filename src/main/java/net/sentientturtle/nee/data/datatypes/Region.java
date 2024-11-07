package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.DataSupplier;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Data object representing EVE Online Regions
 */
@SuppressWarnings("WeakerAccess")
public class Region implements Mappable{
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
    /**
     * Can be left null to indicate this Region does not belong to a faction
     */
    @Nullable
    public final Integer factionID;

    public Region(int regionID, String regionName, double x, double y, double z, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, @Nullable Integer factionID) {
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
    }

    @Override
    public int getID() {
        return regionID;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public Stream<SolarSystem> getMapPoints(DataSupplier dataSupplier) {
        return dataSupplier.getRegionSolarSystemMap().get(regionID).stream();
    }

    @Override
    public Stream<Jump> getMapLines(DataSupplier dataSupplier) {
        return dataSupplier.getRegionJumps().getOrDefault(this.regionID, Set.of()).stream();
    }

    @Nullable
    @Override
    public OptionalInt getFactionID() {
        return factionID != null ? OptionalInt.of(factionID) : OptionalInt.empty();
    }

    @Override
    @Nullable
    public OptionalDouble getSecurity(DataSupplier dataSupplier) {
        return getMapPoints(dataSupplier).mapToDouble(solarsystem -> solarsystem.security).average();
    }

    @Override
    public String getConstituentName() {
        return "Constellations";
    }

    @Override
    public Stream<? extends Mappable> getConstituents(DataSupplier dataSupplier) {
        return dataSupplier.getConstellations().stream().filter(constellation -> constellation.regionID == regionID);
    }

    @Override
    public boolean hasRender() {
        return true;
    }

    @Override
    public String getName() {
        return regionName;
    }

    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.iconOfIconID(2355, context);
    }
}
