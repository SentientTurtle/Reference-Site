package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.MapItem;
import net.sentientturtle.nee.data.datatypes.Station;

import java.util.Set;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays sovereignty ownership of a {@link MapItem} object
 */
public class MapStations extends Component {
    private final Set<Station> stations;

    public MapStations(Set<Station> stations) {
        super("map_stations colour_theme_minor");
        this.stations = stations;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            DIV("font_header").text("Stations"),
            DIV("map_stations_list").content(
                stations.stream().map(station ->
                    DIV("map_stations_entry").content(
                        IMG(ResourceLocation.typeIcon(station.stationTypeID, context), null, 32).className("map_stations_icon"),
                        DIV("map_stations_content").content(
                            SPAN().text(station.stationName),
                            DIV("map_stations_services").content(
                                station.services.stream().map(service ->
                                    IMG(ResourceLocation.fromSharedCache(service.iconResource, context), service.displayName, 32)
                                        .attribute("title", service.displayName)
                                )
                            )
                        )
                    )
                )
            )
        };
    }

    @Override
    protected String getCSS() {
        return
            """
                .map_stations {
                    padding: 1rem;
                }
                
                .map_stations_list, .map_stations_content {
                    display: flex;
                    flex-direction: column;
                }
                
                .map_stations_entry {
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    gap: 0.5rem;
                }
                .map_stations_services {
                    display: flex;
                    flex-direction: row;
                    flex-wrap: wrap;
                }
                
                .map_stations_icon {
                    width: 4rem;
                    height: 4rem;
                }""";
    }
}
