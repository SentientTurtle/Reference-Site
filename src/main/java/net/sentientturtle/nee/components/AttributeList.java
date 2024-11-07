package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.util.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/// List of {@link Type} attribute-values, specialized in subclasses
public abstract class AttributeList extends Component {
    private final @Nullable String title;
    private final Type type;
    private final boolean skipMissing;
    private final Entry[][] data;

    protected AttributeList(@Nullable String title, Type type, boolean skipMissing, Entry[][] data) {
        super("attribute_list colour_theme_minor");
        this.title = title;
        this.type = type;
        this.skipMissing = skipMissing;
        this.data = data;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Attribute> attributeMap = context.data.getAttributes();
        Map<Integer, Map<Integer, Double>> attributeValueMap = context.data.getTypeAttributes();

        var table = TABLE("attribute_list_table font_text");

        for (Entry[] rowData : data) {
            var row = TR();
            for (Entry entry : rowData) {
                Element icon;
                Integer iconID = attributeMap.get(entry.attributeID).iconID;
                if (iconID != null) {
                    icon = IMG(ResourceLocation.iconOfIconID(iconID, context), null, 32).className("attribute_list_icon");
                } else {
                    icon = DIV("attribute_list_icon");
                }

                double value;
                Double valueNullable = attributeValueMap.get(type.typeID).get(entry.attributeID);
                if (valueNullable == null) {
                    if (entry.defaultValue == null) {
                        if (skipMissing) {
                            continue;
                        } else {
                            throw new IllegalStateException("Missing attribute " + entry.attributeID + " on type " + type);
                        }
                    } else {
                        value = entry.defaultValue;
                    }
                } else {
                    value = valueNullable;
                }

                if (entry.name != null) {
                    row.content(
                        TD().content(
                            SPAN("attribute_list_span").title(entry.name).content(
                                icon,
                                TEXT(entry.name + ": "),
                                context.data.format_with_unit(value, attributeMap.get(entry.attributeID).unitID)
                            )
                        )
                    );
                } else {
                    row.content(
                        TD().content(
                            SPAN("attribute_list_span").content(
                                icon,
                                context.data.format_with_unit(value, attributeMap.get(entry.attributeID).unitID)
                            )
                        )
                    );
                }
            }
            table.content(row);
        }


        return new HTML[]{
            this.title != null ? HEADER("font_header").text(title) : HTML.empty(),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .attribute_list {
              padding: 0.5rem;
            }
            
            .attribute_list_table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 0.5rem;
            }
            
            .attribute_list_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .attribute_list_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .attribute_list_icon {
                width: 2rem;
                height: 2rem;
            }
            """;
    }

    public static final class Entry {
        public final @Nullable String name;
        public final int attributeID;
        public final Double defaultValue;

        public Entry(@Nullable String name, int attributeID) {
            this.name = name;
            this.attributeID = attributeID;
            this.defaultValue = null;
        }

        public Entry(@Nullable String name, int attributeID, @Nullable Double defaultValue) {
            this.name = name;
            this.attributeID = attributeID;
            this.defaultValue = defaultValue;
        }
    }
}
