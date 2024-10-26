package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.nee.data.datatypes.singleton.Cluster;
import net.sentientturtle.nee.pages.HasPage;
import net.sentientturtle.nee.pages.MapPage;
import net.sentientturtle.nee.pages.Page;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.DataSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Data interface for objects that can be described as a map/chart.
 */
public interface Mappable extends HasPage {
    int getID();
    String getName();

    @Nullable
    ResourceLocation getIcon();

    double x();
    double y();
    double z();

    /**
     * @param dataSupplier Data supplier to use
     * @return Stream of mappable objects that form points on the map/chart of this mappable
     */
    Stream<? extends Mappable> getMapPoints(DataSupplier dataSupplier);

    /**
     * Returns a stream of lines between the points provided by {@link #getMapPoints(DataSupplier)
     *
     * @param dataSupplier Datasupplier to use
     * @return Stream of Line-tuple of IDs (of Mappables in stream returned by {@link #getMapPoints(DataSupplier)}
     */
    //
    Stream<Jump> getMapLines(DataSupplier dataSupplier);

    OptionalInt getFactionID();
    OptionalDouble getSecurity(DataSupplier dataSupplier);

    /**
     * @return The plural noun of the constituents of this mappable (Such as "Regions" being the constituents of a {@link Cluster})
     */
    String getConstituentName();
    Stream<? extends Mappable> getConstituents(DataSupplier dataSupplier);

    boolean hasRender();

    @Override
    default @NonNull Page getPage() {
        return new MapPage(this);
    }
}
