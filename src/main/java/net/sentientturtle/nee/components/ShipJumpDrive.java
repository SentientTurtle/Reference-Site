package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.Resource;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.page.TypePage;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static net.sentientturtle.html.HTML.*;

/**
 * Propulsion stats of a ship {@link Type}
 */
public class ShipJumpDrive extends AttributeList {
    private static String getTitle(Type type) {
        if (type.groupID == 898) {
            return "Black Ops Jump Drive";
        } else if (type.groupID == 883 || type.groupID == 902) {
            return "Industrial Jump Drive";
        } else {
            return "Jump Drive";
        }
    }

    public ShipJumpDrive(Type type) {
        super(
            getTitle(type),
            type,
            new Entry[][]{{
                new Entry.Custom((t, table, _, context) -> {
                    Map<Integer, Double> attributes = context.sde.getTypeAttributes().get(t.typeID);
                    double range = attributes.get(867);  // All jump drive ships should have a range set

                    table.content(TR().content(
                        TD().content(
                            SPAN("attribute_list_span").content(
                                IMG(Resource.ofIconID(1391, context), null, 32).className("attribute_list_icon"),
                                TEXT("Base Jump Range: "),
                                context.sde.format_with_unit(range, 126)
                            )
                        ),
                        TD().content(
                            SPAN("attribute_list_span").content(
                                IMG(Resource.ofIconID(1391, context), null, 32).className("attribute_list_icon"),
                                TEXT("Max Jump Range: "),
                                context.sde.format_with_unit(range * 2.0, 126)
                            )
                        )
                    ));
                }),
            }, {
                new Entry.Attribute("Fuel", 866),
                new Entry.Custom((t,_, row, context) -> {
                    Map<Integer, Double> attributes = context.sde.getTypeAttributes().get(t.typeID);
                    double fuelAmount = attributes.get(868); // All jump drive ships should have a fuel consumption set

                    row.content(TR().content(
                        TD().content(
                            SPAN("attribute_list_span").content(
                                DIV("attribute_list_icon"),
                                TEXT("Fuel Usage: "),
                                context.sde.format_with_unit(fuelAmount, -8)
                            )
                        )
                    ));
                }),
            }, {
                new Entry.Custom((t, table, _, context) -> {
                    Map<Integer, Double> attributes = context.sde.getTypeAttributes().get(t.typeID);
                    Double conduitPassengers = attributes.get(3133);
                    Double fuelAmount = attributes.get(3131);
                    if (conduitPassengers != null && fuelAmount != null) {
                        String conduitJumpCapable;
                        if (t.groupID == 898) {
                            conduitJumpCapable = "Black Ops Conduit Jump Capable";
                        } else {
                            conduitJumpCapable = "Conduit Jump Capable";
                        }

                        table.content(TR().content(
                            TD().attribute("colspan", "2").content(
                                SPAN("attribute_list_span").style("justify-content: center;")
                                    .content(TEXT_BOLD(conduitJumpCapable))
                            )
                        ));

                        table.content(TR().content(
                            TD().content(
                                SPAN("attribute_list_span").content(
                                    DIV("attribute_list_icon"),
                                    TEXT("Passenger Limit: "),
                                    context.sde.format_with_unit(conduitPassengers, -9)
                                )
                            ),
                            TD().content(
                                SPAN("attribute_list_span").content(
                                    DIV("attribute_list_icon"),
                                    TEXT("Fuel Usage: "),
                                    context.sde.format_with_unit(fuelAmount, -8)
                                )
                            )
                        ));
                    }
                })
            }, {
                new Entry.Custom((t, table, _, context) -> {
                    table.content(TR().content(
                        TD().attribute("colspan", "2").content(
                            SPAN("attribute_list_span").style("justify-content: center;")
                                .content(TEXT_BOLD("Cynosural Beacon"))
                        )
                    ));

                    ArrayList<Element> cynoTypes = new ArrayList<>(5);  // 3 types + 2 separators
                    cynoTypes.add(new PageLink(new TypePage(context.sde.getTypes().get(21096)), "Standard"));

                    if (t.groupID == 883 || t.groupID == 902 || t.groupID == 898) { // Black ops can also use industrial cyno
                        cynoTypes.add(TEXT_ITALICS(" OR "));
                        cynoTypes.add(new PageLink(new TypePage(context.sde.getTypes().get(52694)), "Industrial"));
                    }
                    if (t.groupID == 898) {
                        cynoTypes.add(TEXT_ITALICS(" OR "));
                        cynoTypes.add(new PageLink(new TypePage(context.sde.getTypes().get(28646)), "Covert"));
                    }

                    table.content(TR().content(
                        TD().attribute("colspan", "2").content(
                            SPAN("attribute_list_span").style("justify-content: center;")
                                .content(cynoTypes.toArray(Element[]::new))
                        )
                    ));
                })
            }}
        );
    }
}
