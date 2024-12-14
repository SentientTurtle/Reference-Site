package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.MapItem;

import java.util.OptionalDouble;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays security rating for a {@link MapItem} object.
 */
public class MapSecurity extends Component {
    private final MapItem mapItem;

    public static final String[] SECURITY_COLORS = {
        "#8E3163",
        "#70211E",
        "#BC1212",
        "#CA4812",
        "#DC6C09",
        "#F0FF85",
        "#73E352",
        "#5DDCA6",
        "#48D0F2",
        "#3A9CF1",
        "#2E74DE"
    };

    public MapSecurity(MapItem mapItem) {
        super("map_security colour_theme_minor");
        this.mapItem = mapItem;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        OptionalDouble security = mapItem.getSecurity(context.sde);
        assert security.isPresent();
        double secDouble = security.getAsDouble();
        if (secDouble > 0) secDouble = Math.max(0.1, secDouble);    // Special-case: Anything >0.0 is rounded up to 0.1
        int color = (int) Math.max(0, Math.round(secDouble * 10));

        return new HTML[]{
            TEXT_BOLD("Security: " + ((double) Math.round(security.getAsDouble() * 100)) / 100.0),
            SPAN("map_security_square").style("background-color: " + SECURITY_COLORS[color]).text(" ")
        };
    }

    @Override
    protected String getCSS() {
        return """
            .map_security {
              padding: 0.5rem;
              display: flex;
              align-items: center;
            }
            
            .map_security_square {
                width: 1em;
                height: 1em;
                border-radius: 0.25rem;
                margin-left: 0.5rem;
            }""";
    }
}
