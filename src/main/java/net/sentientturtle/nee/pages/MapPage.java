package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.components.*;
import net.sentientturtle.nee.data.datatypes.Mappable;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.DIV;

/**
 * Map page for a {@link Mappable}
 * <br>
 * To be replaced by dynamic map
 */
public class MapPage extends Page {
    public final Mappable mappable;

    public MapPage(Mappable mappable) {
        this.mappable = mappable;
    }

    @Override
    public String name() {
        return mappable.getName();
    }

    @Override
    public String filename() {
        return mappable.getName();
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        var grid = DIV("map_page_grid").content(
            new Title(mappable.getName(), getIcon())
        );

        if (mappable.hasRender()) {
            grid.content(new ItemRender(ResourceLocation.map(mappable)));
        }

        if (mappable.getSecurity(context.data).isPresent()) {
            grid.content(new MapSecurity(mappable));
        }

        if (mappable.getFactionID().isPresent()) {
            grid.content(new MapSovereignty(mappable));
        }

        grid.content(new MapList(mappable));

        return grid;
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.MAP;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon() {
        return mappable.getIcon();
    }
}
