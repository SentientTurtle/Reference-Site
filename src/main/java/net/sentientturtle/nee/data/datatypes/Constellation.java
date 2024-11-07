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
 * Data object representing EVE Online Constellations
 */
@SuppressWarnings("WeakerAccess")
public class Constellation implements Mappable {
    public final int regionID;
    public final int constellationID;
    public final String constellationName;
    public final double x;
    public final double y;
    public final double z;
    /**
     * May be left null to indicate this constellation does not belong to a faction
     */
    @Nullable
    public final Integer factionID;

    public Constellation(int regionID, int constellationID, String constellationName, double x, double y, double z, @Nullable Integer factionID) {
        this.regionID = regionID;
        this.constellationID = constellationID;
        this.constellationName = constellationName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.factionID = factionID;
    }

    @Override
    public int getID() {
        return constellationID;
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
        return dataSupplier.getConstellationSolarSystemMap().get(constellationID).stream();
    }

    @Override
    public Stream<Jump> getMapLines(DataSupplier dataSupplier) {
        return dataSupplier.getConstellationJumps().getOrDefault(this.constellationID, Set.of()).stream();
    }


    @Override
    @Nullable
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
        return "Solarsystems";
    }

    @Override
    public Stream<SolarSystem> getConstituents(DataSupplier dataSupplier) {
        return dataSupplier.getSolarSystems().stream().filter(solarSystem -> solarSystem.constellationID == this.constellationID);
    }

    @Override
    public boolean hasRender() {
        return true;
    }

    @Override
    public String getName() {
        return constellationName;
    }

    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.iconOfIconID(2355, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Constellation constellation) {
            return this.constellationID == constellation.constellationID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return constellationID;
    }
}
