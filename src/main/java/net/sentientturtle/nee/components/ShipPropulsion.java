package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/**
 * Propulsion stats of a ship {@link Type}
 */
public class ShipPropulsion extends AttributeList {
    public ShipPropulsion(Type type) {
        super(
            "Propulsion",
            type,
            new Entry[][]{{
                new Entry.Attribute("Maximum Velocity", 37),
                new Entry.Custom((tr, context) -> {
                    Map<Integer, Double> attributes = context.sde.getTypeAttributes().get(type.typeID);
                    Double inertia = attributes.get(70);
                    if (inertia == null || attributes.getOrDefault(600, 0.0) == 0.0) return;

                    // Game ticks will always round up
                    double alignTime = Math.ceil(inertia * (type.mass / 1_000_000.0) * -Math.log(0.25));

                    tr.content(
                        TD().content(
                            SPAN("attribute_list_span").title("Align time").content(
                                IMG(ResourceLocation.ofIconID(1401, context), null, 32).className("attribute_list_icon"),
                                TEXT("Align time: "),
                                context.sde.format_with_unit(alignTime, 3)
                            )
                        )
                    );
                }),
            }, {
                new Entry.AttributeSkipIfAbsent("Warp Speed", 600)
            }}
        );
    }
}
