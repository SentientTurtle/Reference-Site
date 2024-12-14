package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Constellation;
import net.sentientturtle.nee.data.datatypes.Region;
import net.sentientturtle.nee.data.datatypes.SolarSystem;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.datatypes.MapItem;

import static net.sentientturtle.html.HTML.*;

/**
 * Lists subsections of a map, such as {@link Region}s, {@link Constellation}s or {@link SolarSystem}s
 */
public class MapList extends Component {
    private final MapItem mapItem;

    public MapList(MapItem mapItem) {
        super("map_list colour_theme_minor");
        this.mapItem = mapItem;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            HEADER("map_list_title font_header").text(mapItem.getConstituentName()),
            DIV("map_list_list font_text").content(
                mapItem.getConstituents(context.sde)
                    .map(entry ->
                        DIV("map_list_entry").content(
                            HTML.repeat(entry.indent(), DIV("map_list_icon")),
                            IMG(ResourceLocation.fromSharedCache("res:/ui/texture/shared/brackets/" + entry.bracketName(), context), null, 64).className("map_list_icon"),
                            SPAN().content(entry.frame() != null ? new PageLink(entry.frame(), entry.name()) : TEXT(entry.name()))
                        )
                    )
            )
        };
    }

    @Override
    protected String getCSS() {
        return """
            .map_list {
              padding: 0.5rem;
            }
            
            .map_list_title {
                margin-bottom: 0.5rem;
            }
            
            .map_list_entry {
                width: 100%;
                display: flex;
            }
            
            .map_list_icon {
                width: 1rem;
                height: 1rem;
                image-rendering: pixelated;
            }""";
    }
}
