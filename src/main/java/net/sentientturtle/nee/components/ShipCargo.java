package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.Map;
import java.util.Objects;

import static net.sentientturtle.html.HTML.*;

/**
 * Cargo stats of a ship {@link Type}
 */
public class ShipCargo extends Component {
    private final Type type;

    public ShipCargo(Type type) {
        super("ship_cargo colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        var table = TABLE("ship_cargo_table");
        int category = context.sde.getGroups().get(type.groupID).categoryID;

        if (type.capacity > 0) {
            String basicName;
            // If type is a module
            if (category == 7 || category == 66) {
                basicName = "Ammo Capacity";
            } else {
                basicName = "Cargo Bay Capacity";
            }

            table.content(
                TR().content(
                    TD().text(basicName),   // TODO: Link to cargo bay explanation page
                    TD().content(context.sde.format_with_unit(type.capacity, 9))
                )
            );
        }
        Map<Integer, Attribute> attributeMap = context.sde.getAttributes();
        Map<Integer, Map<Integer, Double>> attributeValueMap = context.sde.getTypeAttributes();

        attributeMap.values()
            .stream()
            .filter(attribute -> Objects.equals(attribute.categoryID, 40) && attribute.published)
            .forEach(attribute -> {
                Double value = attributeValueMap.getOrDefault(type.typeID, Map.of()).get(attribute.attributeID);
                if (value != null) {
                    table.content(TR().content(
                        TD().text(attribute.displayName),
                        TD().content(context.sde.format_with_unit(value, attribute.unitID))
                    ));
                }
            });

        return new HTML[]{
            HEADER("font_header").text("Cargo bays"),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .ship_cargo {
                padding: 0.5rem;
            }
            
            .ship_cargo_table {
                width: 100%;
                padding: 0.5rem;
            }""";
    }
}
