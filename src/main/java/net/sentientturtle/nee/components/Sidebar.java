package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.pages.*;

import static net.sentientturtle.html.HTML.*;

/// Website navigation sidebar
public class Sidebar extends Component {
    public Sidebar() {
        super("sidebar font_header");
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            new PageLink(new IndexPage(), DIV("sidebar_button eve_clip_bottom_right").text("Home")).className("sidebar_button_border eve_clip_bottom_right"),
            new PageLink(new ShipTreePage(), DIV("sidebar_button eve_clip_bottom_right").text("Ships")).className("sidebar_button_border eve_clip_bottom_right"),
            new PageLink(new StructureTreePage(), DIV("sidebar_button eve_clip_bottom_right").text("Structures")).className("sidebar_button_border eve_clip_bottom_right"),
            new PageLink(new MarketGroupPage(context.data.getMarketGroups().get(9)), DIV("sidebar_button eve_clip_bottom_right").text("Modules")).className("sidebar_button_border eve_clip_bottom_right"),
            new PageLink(new MarketGroupPage(context.data.getMarketGroups().get(-1)), DIV("sidebar_button eve_clip_bottom_right").text("Items")).className("sidebar_button_border eve_clip_bottom_right"),
            new PageLink(new DynamicMapPage(), DIV("sidebar_button eve_clip_bottom_right").text("Map")).className("sidebar_button_border eve_clip_bottom_right")
        };
    }

    @Override
    protected String getCSS() {
        return """
            .sidebar {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 0.5rem;
            }
            
            .sidebar a:link, .sidebar a:visited, .sidebar a:hover, .sidebar a:active  {
                color: var(--colour-text);
            }
            
            .sidebar_button_border {
                width: 11rem;
                height: 2rem;
                background: var(--colour-theme-border);
                padding: var(--border-size);
            }
            
            .sidebar_button_border:hover {
                background: var(--colour-theme-highlight-border);
            }
            
            .sidebar_button_border:hover > .sidebar_button {
                background: var(--colour-theme-highlight-bg);
            }
            
            .sidebar_button {
                width: 100%;
                height: 100%;
                display: flex;
                justify-content: center;
                align-items: center;
                background: var(--colour-theme-bg);
                pointer-events: none;
            }
            
            """;
    }
}
