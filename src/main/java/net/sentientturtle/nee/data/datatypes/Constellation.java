package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.Resource;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.page.MapFrame;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Data object representing EVE Online Constellations
 */
@SuppressWarnings("WeakerAccess")
public class Constellation implements MapItem {
    public final int regionID;
    public final int constellationID;
    public final String constellationName;
    public final double x;
    public final double y;
    public final double z;
    public @Nullable Integer factionID;
    public final @Nullable Integer wormholeClassID;

    public Constellation(int regionID, int constellationID, String constellationName, double x, double y, double z, @Nullable Integer factionID, @Nullable Integer wormholeClassID) {
        this.regionID = regionID;
        this.constellationID = constellationID;
        this.constellationName = constellationName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.factionID = factionID;
        this.wormholeClassID = wormholeClassID;
    }

    @Override
    public int getID() {
        return constellationID;
    }


    @Override
    @Nullable
    public OptionalInt getSovFactionID() {
        return factionID != null ? OptionalInt.of(factionID) : OptionalInt.empty();
    }

    @Override
    @Nullable
    public OptionalDouble getSecurity(SDEData SDEData) {
        return SDEData.getConstellationSolarSystemMap().get(constellationID).stream().mapToDouble(solarsystem -> solarsystem.security).average();
    }

    @Override
    public String getConstituentName() {
        return "Solarsystems";
    }

    @Override
    public Stream<MapItem.MapConstituent> getConstituents(SDEData sde) {
        return sde.getConstellationSolarSystemMap()
            .get(this.constellationID)
            .stream()
            .sorted(Comparator.comparing(s -> s.solarSystemName))
            .map(system -> new MapItem.MapConstituent(
                "solarsystem.png",
                system.solarSystemName,
                new MapFrame(system),
                0
            ));
    }

    @Override
    public String getName() {
        return constellationName;
    }

    @Override
    public @Nullable MapItem getParent(HtmlContext context) {
        return context.sde.getRegions().get(this.regionID);
    }

    @Override
    public Resource getIcon(HtmlContext context) {
        return Resource.ofIconID(2355, context);
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
