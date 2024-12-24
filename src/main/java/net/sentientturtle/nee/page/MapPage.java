package net.sentientturtle.nee.page;

import net.sentientturtle.html.Frame;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.components.*;
import net.sentientturtle.nee.data.datatypes.MapItem;
import net.sentientturtle.nee.data.datatypes.SolarSystem;
import net.sentientturtle.nee.data.datatypes.Station;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static net.sentientturtle.html.HTML.DIV;

/**
 * Map page for a {@link MapItem}
 * <br>
 * To be replaced by dynamic map
 */
public class MapPage extends Frame {
    public final MapItem mapItem;

    public MapPage(MapItem mapItem) {
        this.mapItem = mapItem;
    }

    @Override
    public String name() {
        return mapItem.getName();
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return String.valueOf(mapItem.getID());
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .map_page_column {
                width: 100%;
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }
            
            .map_security_square {
                width: 1em;
                height: 1em;
                border-radius: 0.25rem;
                margin-left: 0.5rem;
            }
            """;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        var grid = DIV("map_page_column").content(
            new ItemTitle(mapItem.getName(), getIcon(context))
        );

        ArrayList<MapItem> parents = new ArrayList<>();
        {
            MapItem parent = this.mapItem;
            while ((parent = parent.getParent(context)) != null) {
                parents.add(parent);
            }
        }

        if (parents.size() > 0) {
            grid.content(new MapParents(parents));
        }

        if (mapItem.getSecurity(context.sde).isPresent()) {
            grid.content(new MapSecurity(mapItem));
        }

        if (mapItem.getSovFactionID().isPresent()) {
            grid.content(new MapSovereignty(mapItem, null, null));
        } else if (mapItem instanceof SolarSystem solarSystem) {
            if (solarSystem.regionID <= 10001000 && solarSystem.regionID != 10000004) {
                grid.content(new MapSovereignty(mapItem, "[Capsuleer Alliance]", ResourceLocation.fromSharedCache("res:/ui/texture/alliance/1_128_1.png", context)));
            } else if (solarSystem.regionID >= 11000001 && solarSystem.regionID <= 11000030 && !(solarSystem.solarSystemID >= 31002505 && solarSystem.solarSystemID <= 31002604)) {
                grid.content(new MapSovereignty(mapItem, "[Disputed]", ResourceLocation.fromSharedCache("res:/ui/texture/alliance/1_128_1.png", context)));
            }
        }

        if (mapItem instanceof SolarSystem solarSystem) {
            Set<Station> stations = context.sde.getStations().getOrDefault(solarSystem.solarSystemID, Set.of());
            if (stations.size() > 0) {
                grid.content(new MapStations(stations));
            }
        }

        if (mapItem.getConstituents(context.sde).findAny().isPresent()) {
            grid.content(new MapList(mapItem));
        }

        return grid;
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.MAP;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return mapItem.getIcon(context);
    }
}
