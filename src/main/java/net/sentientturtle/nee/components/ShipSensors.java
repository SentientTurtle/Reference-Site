package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.ResourceLocation;

import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/**
 * Sensor stats of a ship {@link Type}
 */
public class ShipSensors extends Component {    // TODO: Maybe rename TypeSensors as drones/structures also have sensor stats
    private final Type type;

    public ShipSensors(Type type) {
        super("ship_sensors colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Attribute> attributeMap = context.data.getAttributes();
        Map<Integer, Double> typeAttributes = context.data.getTypeAttributes().get(type.typeID);
        var table = TABLE("ship_sensors_table font_text");

        double targetingRange = typeAttributes.getOrDefault(76, 0.0);
        if (targetingRange > 0.0) {
            table.content(
                TR().content(
                    TD().content(
                        SPAN("ship_sensors_span").title("Targeting Range").content(
                            IMG(ResourceLocation.iconOfIconID(attributeMap.get(76).iconID, context), null, 32).className("ship_sensors_icon"),
                            TEXT("Targeting Range: "),
                            context.data.format_with_unit(targetingRange > 1000.0 ? targetingRange / 1000.0 : targetingRange, -1),
                            TEXT(targetingRange > 1000.0 ? " km" : " m")
                        )
                    ),
                    TD().content(
                        SPAN("ship_sensors_span").title("Scan Resolution").content(
                            IMG(ResourceLocation.iconOfIconID(attributeMap.get(564).iconID, context), null, 32).className("ship_sensors_icon"),
                            TEXT("Scan Resolution: "),
                            context.data.format_with_unit(typeAttributes.getOrDefault(564, 0.0), attributeMap.get(564).unitID)
                        )
                    )
                ),
                TR().content(
                    TD().content(
                        SPAN("ship_sensors_span").title("Maximum Targets").content(
                            IMG(ResourceLocation.iconOfIconID(attributeMap.get(192).iconID, context), null, 32).className("ship_sensors_icon"),
                            TEXT("Maximum Targets: "),
                            context.data.format_with_unit(typeAttributes.getOrDefault(192, 0.0), attributeMap.get(192).unitID)
                        )
                    )
                )
            );
        }

        String sensorType;
        int sensorAttribute = 0;
        double sensorStrength;

        double radar = typeAttributes.getOrDefault(208, 0.0);
        double magnet = typeAttributes.getOrDefault(210, 0.0);
        double gravi = typeAttributes.getOrDefault(211, 0.0);
        double ladar = typeAttributes.getOrDefault(209, 0.0);

        if (radar == 0.0 && magnet == 0.0 && gravi == 0.0 && ladar == 0.0) {
            sensorType = null;
            sensorStrength = 0.0;
        } else if (radar > magnet && radar > gravi && radar > ladar) {
            sensorType = "Radar";
            sensorAttribute = 208;
            sensorStrength = radar;
        } else if (magnet > radar && magnet > gravi && magnet > ladar) {
            sensorType = "Magnetometric";
            sensorAttribute = 210;
            sensorStrength = magnet;
        } else if (gravi > radar && gravi > magnet && gravi > ladar) {
            sensorType = "Gravimetric";
            sensorAttribute = 211;
            sensorStrength = gravi;
        } else if (ladar > radar && ladar > magnet && ladar > gravi) {
            sensorType = "LADAR";
            sensorAttribute = 209;
            sensorStrength = ladar;
        } else if (radar == magnet && magnet == gravi && gravi == ladar) {
            sensorType = "Multi-spectrum";
            sensorAttribute = 210;
            sensorStrength = radar;
        } else {
            throw new IllegalStateException("Unknown sensor hybrid type: " + radar + "," + magnet + "," + gravi + "," + ladar);
        }

        if (sensorType != null) {
            table.content(TR().content(
                TD().content(
                    SPAN("ship_sensors_span").title("Sensor Type").content(
                        IMG(ResourceLocation.iconOfIconID(attributeMap.get(sensorAttribute).iconID, context), null, 32).className("ship_sensors_icon"),
                        TEXT("Sensor Type: " + sensorType)
                    )
                ),
                TD().content(
                    SPAN("ship_sensors_span").title("Sensor Strength").content(
                        IMG(ResourceLocation.iconOfIconID(attributeMap.get(sensorAttribute).iconID, context), null, 32).className("ship_sensors_icon"),
                        TEXT("Sensor Strength: "),
                        context.data.format_with_unit(sensorStrength, attributeMap.get(sensorAttribute).unitID)
                    )
                )
            ));
        }


        return new HTML[]{
            HEADER("font_header").text("Sensors"),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .ship_sensors {
              padding: 0.5rem;
            }
            
            .ship_sensors_table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 0.5rem;
            }
            
            .ship_sensors_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .ship_sensors_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .ship_sensors_icon {
                width: 2rem;
                height: 2rem;
            }""";
    }
}
