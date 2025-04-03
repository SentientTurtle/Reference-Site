package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.page.*;

import static net.sentientturtle.html.HTML.*;

/// Website navigation sidebar
public class Sidebar extends Component {
    public Sidebar() {
        super("nav", "sidebar font_header");
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            new PageLink(new IndexPage(), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Home")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new ShipTreePage(), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Ships")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new MarketGroupPage(context.sde.getMarketGroups().get(9)), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Ship Modules")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new StructureTreePage(), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Structures")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new MarketGroupPage(context.sde.getMarketGroups().get(2202)), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Structure Modules")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new MarketGroupPage(context.sde.getMarketGroups().get(-1)), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Items")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new DynamicMapPage(), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Map")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new DevResourcePage(), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("3rd Party Dev")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset"),
            new PageLink(new SettingsPage(), DIV("sidebar_button eve_clip_bottom_right eve_clip_mobile_unset").text("Settings")).className("sidebar_button_border eve_clip_bottom_right eve_clip_mobile_unset")
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
        
            @media (max-width: 47.5rem) {
                .sidebar {
                    flex-direction: row;
                    flex-wrap: wrap;
                    justify-content: center;
                }
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
