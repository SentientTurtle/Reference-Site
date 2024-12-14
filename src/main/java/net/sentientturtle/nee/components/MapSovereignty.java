package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Faction;
import net.sentientturtle.nee.data.datatypes.MapItem;

import java.util.OptionalInt;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays sovereignty ownership of a {@link MapItem} object
 */
public class MapSovereignty extends Component {
    private final MapItem mapItem;
    private final String fallBackText;
    private final ResourceLocation fallBackIcon;

    public MapSovereignty(MapItem mapItem, String fallBackText, ResourceLocation fallBackIcon) {
        super("map_sovereignty colour_theme_minor");
        this.mapItem = mapItem;
        this.fallBackText = fallBackText;
        this.fallBackIcon = fallBackIcon;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        OptionalInt factionID = mapItem.getSovFactionID();
        if (factionID.isPresent()) {
            Faction faction = context.sde.getFactions().get(factionID.getAsInt());

            return new HTML[]{
                DIV("font_header").text("Sovereignty"),
                DIV("map_sovereignty_faction").content(
                    IMG(ResourceLocation.factionLogo(faction.factionID), null, 64).className("map_sovereignty_icon"),
                    TEXT_BOLD(faction.factionName).className("font_header map_sovereignty_text")
                )
            };
        } else {
            return new HTML[]{
                DIV("font_header").text("Sovereignty"),
                DIV("map_sovereignty_faction").content(
                    IMG(fallBackIcon, null, 64).className("map_sovereignty_icon"),
                    TEXT_BOLD(fallBackText).className("font_header map_sovereignty_text")
                )
            };
        }
    }

    @Override
    protected String getCSS() {
        return
            """
                .map_sovereignty {
                  padding: 1rem;
                }
                
                .map_sovereignty_faction {
                  display: flex;
                  align-items: center;
                }
                
                .map_sovereignty_icon {
                    width: 4rem;
                    height: 4rem;
                }
                
                .map_sovereignty_text {
                  margin-left: 1rem;
                }""";
    }
}
