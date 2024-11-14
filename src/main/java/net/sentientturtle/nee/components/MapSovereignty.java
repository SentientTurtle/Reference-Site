package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Faction;
import net.sentientturtle.nee.data.datatypes.Mappable;

import java.util.OptionalInt;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays sovereignty ownership of a {@link Mappable} object
 */
public class MapSovereignty extends Component {
    private final Mappable mappable;

    public MapSovereignty(Mappable mappable) {
        super("map_sovereignty colour_theme_minor");
        this.mappable = mappable;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        OptionalInt factionID = mappable.getFactionID();
        assert factionID.isPresent();
        Faction faction = context.data.getFactions().get(factionID.getAsInt());
        Integer corporationID = faction.corporationID;

        if (corporationID != null) {
            return new HTML[]{
                DIV("font_header").text("Sovereignty"),
                DIV("map_sovereignty_faction").content(
                    IMG(ResourceLocation.iconOfCorpID(corporationID), null, 64).className("map_sovereignty_icon"),
                    TEXT_BOLD(faction.factionName).className("font_header map_sovereignty_text")
                )
            };
        } else {
            return new HTML[]{
                DIV("font_header").text("Sovereignty"),
                DIV("map_sovereignty_faction").content(
                    TEXT_BOLD(faction.factionName).className("font_header map_sovereignty_text")
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
                  font-size: 1.5rem;
                  margin-left: 1rem;
                }""";
    }
}
