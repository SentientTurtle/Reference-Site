package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.Constellation;
import net.sentientturtle.nee.data.datatypes.Region;
import net.sentientturtle.nee.data.datatypes.SolarSystem;
import net.sentientturtle.nee.page.MapPage;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.datatypes.Mappable;

import static net.sentientturtle.html.HTML.*;

/**
 * Lists subsections of a map, such as {@link Region}s, {@link Constellation}s or {@link SolarSystem}s
 */
public class MapList extends Component {
    private final Mappable mappable;

    public MapList(Mappable mappable) {
        super("map_list colour_theme_minor");
        this.mappable = mappable;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            SPAN("map_list_title").content(HEADER("font_header").text(mappable.getConstituentName())),
            DIV("map_list_list font_text").content(   // TODO: Test what text copied out of browser does formatting-wise; Replace with table if copied text is not tabular
                mappable.getConstituents(context.sde).map(entry ->
                    DIV("map_list_entry").content(
                        (entry.getIcon(context) != null) ? IMG(entry.getIcon(context), null, 64).className("map_list_icon") : DIV("map_list_icon"),
                        SPAN("map_list_link").content(new PageLink(new MapPage(entry)))
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
              font-size: 1.75rem;
              margin-left: 1rem;
            }
            
            .map_list_list {
            
            }
            
            .map_list_entry {
              padding-top: 0.5rem;
            }
            
            .map_list_icon {
                width: 4rem;
                height: 4rem;
            }
            
            .map_list_link {
              font-size: 1.25rem;
              margin: 0.5rem;
            }""";
    }
}
