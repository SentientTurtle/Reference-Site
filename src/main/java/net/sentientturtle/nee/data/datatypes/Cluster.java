package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.SDEData;
import net.sentientturtle.nee.page.MapPage;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link MapItem} type for the clusters in EVE Online
 */
public interface Cluster extends MapItem {
    Set<Integer> KSPACE_REGIONS = Set.of(
        10000001, 10000002, 10000003, 10000004, 10000005, 10000006,
        10000007, 10000008, 10000009, 10000010, 10000011, 10000012,
        10000013, 10000014, 10000015, 10000016, 10000017, 10000018,
        10000019, 10000020, 10000021, 10000022, 10000023, 10000025,
        10000027, 10000028, 10000029, 10000030, 10000031, 10000032,
        10000033, 10000034, 10000035, 10000036, 10000037, 10000038,
        10000039, 10000040, 10000041, 10000042, 10000043, 10000044,
        10000045, 10000046, 10000047, 10000048, 10000049, 10000050,
        10000051, 10000052, 10000053, 10000054, 10000055, 10000056,
        10000057, 10000058, 10000059, 10000060, 10000061, 10000062,
        10000063, 10000064, 10000065, 10000066, 10000067, 10000068,
        10000069, 10001000
    );
    Set<Integer> WSPACE_REGIONS = Set.of(
        11000001, 11000002, 11000003, 11000004, 11000005, 11000006,
        11000007, 11000008, 11000009, 11000010, 11000011, 11000012,
        11000013, 11000014, 11000015, 11000016, 11000017, 11000018,
        11000019, 11000020, 11000021, 11000022, 11000023, 11000024,
        11000025, 11000026, 11000027, 11000028, 11000029, 11000030,
        11000031, 11000032, 11000033
    );

    /**
     * Singleton representing the "New Eden Cluster", also known as "Known Space"
     */
    MapItem K_SPACE = new Cluster() {
        @Override
        public int getID() {
            return -1;
        }


        @Override
        public OptionalInt getSovFactionID() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalDouble getSecurity(SDEData SDEData) {
            return OptionalDouble.empty();
        }

        @Override
        public String getConstituentName() {
            return "Regions";
        }

        @Override
        public Stream<MapItem.MapConstituent> getConstituents(SDEData sde) {
            return KSPACE_REGIONS.stream()
                .map(sde.getRegions()::get)
                .filter(region -> region.regionID < 11_000_000)
                .sorted(Comparator.comparing(r -> r.regionName))
                .map(region -> new MapItem.MapConstituent(
                    "region.png",
                    region.regionName,
                    new MapPage(region),
                    0
                ));
        }

        @Override
        public String getName() {
            return "New Eden Cluster";
        }

        @Override
        public @Nullable MapItem getParent(HtmlContext context) {
            return null;
        }

        @Override
        public ResourceLocation getIcon(HtmlContext context) {
            return ResourceLocation.ofIconID(2355, context);
        }
    };

    /**
     * Singleton representing the "Anoikis" cluster, also known as "Wormhole Space" or "Unknown Space"
     */
    MapItem W_SPACE = new Cluster() {
        @Override
        public int getID() {
            return -2;
        }

        @Override
        public OptionalInt getSovFactionID() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalDouble getSecurity(SDEData SDEData) {
            return OptionalDouble.empty();
        }

        @Override
        public String getConstituentName() {
            return "Regions";
        }

        @Override
        public Stream<MapItem.MapConstituent> getConstituents(SDEData sde) {
            return WSPACE_REGIONS.stream()
                .map(sde.getRegions()::get)
                .sorted(Comparator.comparing(r -> r.regionName))
                .map(region -> new MapItem.MapConstituent(
                    "region.png",
                    region.regionName,
                    new MapPage(region),
                    0
                ));
        }

        @Override
        public String getName() {
            return "Anoikis";
        }

        @Override
        public @Nullable MapItem getParent(HtmlContext context) {
            return null;
        }

        @Override
        public ResourceLocation getIcon(HtmlContext context) {
            return ResourceLocation.ofIconID(2355, context);
        }
    };
}
