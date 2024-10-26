package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.DataSupplier;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Data object representing EVE Online SolarSystems
 */
@SuppressWarnings("WeakerAccess")
public final class SolarSystem implements Mappable {
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
        @Nullable Integer sunTypeID
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
    }

    @Override
    public int getID() {
        return solarSystemID;
    }

    @Override
    public Stream<? extends Mappable> getMapPoints(DataSupplier dataSupplier) {
        return Stream.empty();
    }

    @Override
    public Stream<Jump> getMapLines(DataSupplier dataSupplier) {
        return Stream.empty();
    }

    @Override
    @Nullable
    public OptionalInt getFactionID() {
        return factionID != null ? OptionalInt.of(factionID) : OptionalInt.empty();
    }

    @Override
    @Nullable
    public OptionalDouble getSecurity(DataSupplier dataSupplier) {
        return OptionalDouble.of(security);
    }

    @Override
    public String getConstituentName() {
        return "Orbitals";
    }

    @Override
    public Stream<Mappable> getConstituents(DataSupplier dataSupplier) {
        return Stream.empty();
    }

    @Override
    public boolean hasRender() {
        return false;
    }

    @Override
    public String getName() {
        return solarSystemName;
    }

    @Override
    public @Nullable ResourceLocation getIcon() {
        if (sunTypeID == null) {
            return null;
        } else {
            return ResourceLocation.iconOfTypeID(sunTypeID);
        }
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
