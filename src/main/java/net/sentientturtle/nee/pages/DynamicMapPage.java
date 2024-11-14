package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

/// Page for the dynamic Map
public class DynamicMapPage extends Page {
    @Override
    public String name() {
        return "map";
    }

    @Override
    public String filename() {
        return "map";
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.STATIC;
    }

    @Override
    public @Nullable ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.iconOfIconID(2355, context);
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return HTML.TEXT("No map yet. Soz.");
    }
}
