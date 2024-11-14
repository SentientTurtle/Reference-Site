package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Type;

import static net.sentientturtle.html.HTML.IMG;

/**
 * Large (512px) image render of a {@link Type}, usually a Ship, Drone, or Structure.
 */
public class ItemRender extends Component {
    private final ResourceLocation resourceLocation;

    public ItemRender(Type type) {
        super("item_render colour_theme_minor");
        resourceLocation = ResourceLocation.typeRender(type.typeID);
    }

    public ItemRender(ResourceLocation resourceLocation) {
        super("item_render");
        this.resourceLocation = resourceLocation;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            IMG(resourceLocation, null)
        };
    }

    @Override
    protected String getCSS() {
        return """            
            .item_render img {
                display: block;
                margin: 0 auto;
                width: min(100%, 512px);
                aspect-ratio: 1/1;
            }""";
    }
}
