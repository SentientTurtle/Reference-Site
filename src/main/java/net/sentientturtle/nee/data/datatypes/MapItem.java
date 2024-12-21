package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Frame;
import net.sentientturtle.nee.page.HasPage;
import net.sentientturtle.nee.page.MapPage;
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
public interface MapItem extends HasPage {
    int getID();
    String getName();

    @Nullable MapItem getParent(HtmlContext context);

    @Nullable ResourceLocation getIcon(HtmlContext context);

    OptionalInt getSovFactionID();
    OptionalDouble getSecurity(SDEData SDEData);

    record MapConstituent(String bracketName, String name, @Nullable Frame frame, int indent) {}

    /**
     * @return The plural noun of the constituents of this mappable (Such as "Regions" being the constituents of a {@link Cluster})
     */
    String getConstituentName();
    Stream<MapConstituent> getConstituents(SDEData sde);

    @Override
    default @NonNull Frame getPage() {
        return new MapPage(this);
    }
}
