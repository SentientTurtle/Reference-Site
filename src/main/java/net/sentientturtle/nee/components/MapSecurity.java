package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.Mappable;
import net.sentientturtle.nee.util.MapRenderer;

import java.util.OptionalDouble;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays security rating for a {@link Mappable} object.
 */
public class MapSecurity extends Component {
    private final Mappable mappable;

    public MapSecurity(Mappable mappable) {
        super("map_security colour_theme_minor");
        this.mappable = mappable;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        OptionalDouble security = mappable.getSecurity(context.data);
        assert security.isPresent();
        int color = (int) Math.max(0, Math.round(security.getAsDouble() * 10));

        return new HTML[]{
            TEXT_BOLD("Security: " + ((double) Math.round(security.getAsDouble() * 100)) / 100.0),
            SPAN("map_security_square").style("background-color: " + MapRenderer.SECURITY_COLORS[color]).text(" ")
        };
    }

    @Override
    protected String getCSS() {
        return """
            .map_security {
              padding: 1rem;
              display: flex;
              align-items: center;
              font-size: 1.5rem;
            }
            
            .map_security_square {
                width: 1em;
                height: 1em;
                border-radius: 0.25rem;
                margin-left: 0.5rem;
            }""";
    }
}
