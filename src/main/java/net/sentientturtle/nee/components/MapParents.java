package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.MapItem;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

import static net.sentientturtle.html.HTML.SPAN;
import static net.sentientturtle.html.HTML.TEXT_BOLD;

public class MapParents extends Component {
    private final List<MapItem> items;

    public MapParents(@NonNull List<MapItem> items) {
        super("map_parents colour_theme_minor font_header");
        this.items = Objects.requireNonNull(items);
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        HTML[] array = new HTML[items.size() + Math.max(items.size() - 1, 0)];
        int i = 0;
        for (MapItem item : items) {
            array[i] = TEXT_BOLD().className("map_parents_text").content(new PageLink(item.getPage()));
            i += 1;
            if (i < array.length) {
                array[i] = SPAN("map_parents_separator").text("|");
                i += 1;
            }
        }
        return array;
    }

    @Override
    protected String getCSS() {
        return """
            .map_parents {
                min-height: 3rem;
                display: flex;
                align-items: center;
                padding-inline: 0.5rem;
            }
            
            .map_parents_text {
                flex-grow: 1;
                flex-basis: 0;
                text-align: center;
            }
            
            .map_parents_separator {
                font-size: 2rem;
                width: 1.5rem;
                text-align: center;
            }""";
    }
}
