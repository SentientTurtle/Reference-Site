package net.sentientturtle.nee.data.datatypes.singleton;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Jump;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.datatypes.Mappable;
import net.sentientturtle.nee.data.datatypes.SolarSystem;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * {@link Mappable} type for the clusters in EVE Online
 */
public interface Cluster extends Mappable {
    /**
     * Singleton representing the "New Eden Cluster", also known as "Known Space"
     */
    Mappable K_SPACE = new Cluster() {
        @Override
        public int getID() {
            return -1;
        }

        @Override
        public double x() {
            return 0;
        }

        @Override
        public double y() {
            return 0;
        }

        @Override
        public double z() {
            return 0;
        }

        @Override
        public Stream<SolarSystem> getMapPoints(DataSupplier dataSupplier) {
            return dataSupplier.getSolarSystems().stream().filter(solarSystem -> solarSystem.solarSystemID < 31_000_000);
        }

        @Override
        public Stream<Jump> getMapLines(DataSupplier dataSupplier) {
            return Stream.empty();
        }


        @Override
        public OptionalInt getFactionID() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalDouble getSecurity(DataSupplier dataSupplier) {
            return getMapPoints(dataSupplier).mapToDouble(solarsystem -> solarsystem.security).average();
        }

        @Override
        public String getConstituentName() {
            return "Regions";
        }

        @Override
        public Stream<? extends Mappable> getConstituents(DataSupplier dataSupplier) {
            return dataSupplier.getRegions().stream().filter(region -> region.regionID < 11_000_000);
        }

        @Override
        public boolean hasRender() {
            return true;
        }

        @Override
        public String getName() {
            return "New Eden Cluster";
        }

        @Override
        public ResourceLocation getIcon(HtmlContext context) {
            return ResourceLocation.iconOfIconID(2355, context);
        }
    };

    /**
     * Singleton representing the "Anoikis" cluster, also known as "Wormhole Space" or "Unknown Space"
     */
    Mappable W_SPACE = new Cluster() {
        @Override
        public int getID() {
            return -2;
        }

        @Override
        public double x() {
            return 0;
        }

        @Override
        public double y() {
            return 0;
        }

        @Override
        public double z() {
            return 0;
        }

        @Override
        public Stream<SolarSystem> getMapPoints(DataSupplier dataSupplier) {
            return dataSupplier.getSolarSystems().stream().filter(solarSystem -> solarSystem.solarSystemID > 31_000_000 && solarSystem.solarSystemID < 32_000_000);
        }

        @Override
        public Stream<Jump> getMapLines(DataSupplier dataSupplier) {
            return Stream.empty();
        }

        @Override
        public OptionalInt getFactionID() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalDouble getSecurity(DataSupplier dataSupplier) {
            return getMapPoints(dataSupplier).mapToDouble(solarsystem -> solarsystem.security).average();
        }

        @Override
        public String getConstituentName() {
            return "Regions";
        }

        @Override
        public Stream<? extends Mappable> getConstituents(DataSupplier dataSupplier) {
            return dataSupplier.getRegions().stream().filter(region -> region.regionID > 11_000_000);
        }

        @Override
        public boolean hasRender() {
            return true;
        }

        @Override
        public String getName() {
            return "Anoikis";
        }

        @Override
        public ResourceLocation getIcon(HtmlContext context) {
            return ResourceLocation.iconOfIconID(2355, context);
        }
    };
}
