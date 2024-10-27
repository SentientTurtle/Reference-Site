package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.*;

import static net.sentientturtle.html.HTML.*;

/**
 * Attribute list of a {@link Type}
 */
public class TypeAttributes extends Component {
    private final Type type;

    public TypeAttributes(Type type) {
        super("type_attributes colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Double> attributes = context.data.getTypeAttributes().get(type.typeID);
        Map<Integer, Attribute> attributeMap = context.data.getAttributes();

        LinkedHashMap<Attribute, Double> attributeValues = new LinkedHashMap<>();   // Ordered map is required here

        if (type.mass > 0 && type.mass <= 200000000000.0) attributeValues.put(attributeMap.get(4), type.mass);
        if (type.volume > 0) attributeValues.put(attributeMap.get(161), type.volume);

        for (Map.Entry<Integer, Double> entry : attributes.entrySet()) {
            Attribute attribute = attributeMap.get(entry.getKey());
            if (!attribute.published || attribute.attributeName == null) continue;
            attributeValues.put(attribute, entry.getValue());
        }

        var table = TABLE("type_attributes_table");

        attributeValues.forEach((attribute, value) -> {
            var row = TR();
            table.content(row);

            if (attribute.iconID != null) {
                row.content(TD("type_attributes_td type_attributes_icon").content(IMG(ResourceLocation.iconOfIconID(attribute.iconID), null, 32)));
            } else {
                row.content(TD("type_attributes_td type_attributes_icon"));
            }
            row.content(
                TD("type_attributes_td").text(attribute.displayName == null ? attribute.attributeName : attribute.displayName),
                TD("type_attributes_td").content(context.data.format_with_unit(value, attribute.unitID))
            );
        });

        return new HTML[] {
            HEADER("font_header").text("Attributes"),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .type_attributes {
                padding: 0.5rem;
            }
            
            .type_attributes_table {
                width: 100%;
            }
            
            .type_attributes_td {
                height: 2rem;
                font-size: 1rem;
                padding-right: 1rem;
            }
            
            .type_attributes_icon {
                width: 2rem;
            }""";
    }
}
