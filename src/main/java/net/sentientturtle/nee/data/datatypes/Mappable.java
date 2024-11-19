package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.singleton.Cluster;
import net.sentientturtle.nee.page.HasPage;
import net.sentientturtle.nee.page.MapPage;
import net.sentientturtle.nee.page.Page;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.SDEData;
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
    ResourceLocation getIcon(HtmlContext context);

    double x();
    double y();
    double z();

    /**
     * @param SDEData Data supplier to use
     * @return Stream of mappable objects that form points on the map/chart of this mappable
     */
    Stream<? extends Mappable> getMapPoints(SDEData SDEData);

    /**
     * Returns a stream of lines between the points provided by {@link #getMapPoints(SDEData)
     *
     * @param dataSupplier Datasupplier to use
     * @return Stream of Line-tuple of IDs (of Mappables in stream returned by {@link #getMapPoints(SDEData)}
     */
    //
    Stream<Jump> getMapLines(SDEData SDEData);

    OptionalInt getFactionID();
    OptionalDouble getSecurity(SDEData SDEData);

    /**
     * @return The plural noun of the constituents of this mappable (Such as "Regions" being the constituents of a {@link Cluster})
     */
    String getConstituentName();
    Stream<? extends Mappable> getConstituents(SDEData SDEData);

    boolean hasRender();

    @Override
    default @NonNull Page getPage() {
        return new MapPage(this);
    }
}
