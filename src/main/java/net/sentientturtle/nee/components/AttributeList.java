package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

import static net.sentientturtle.html.HTML.*;

/// List of {@link Type} attribute-values, specialized in subclasses
public abstract class AttributeList extends Component {
    private final @Nullable String title;
    private final Type type;
    private final Entry[][] data;

    protected AttributeList(@Nullable String title, Type type, Entry[][] data) {
        super("attribute_list colour_theme_minor");
        this.title = title;
        this.type = type;
        this.data = data;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Attribute> attributeMap = context.sde.getAttributes();
        Map<Integer, Map<Integer, Double>> attributeValueMap = context.sde.getTypeAttributes();

        var table = TABLE("attribute_list_table font_text");

        for (Entry[] rowData : data) {
            var row = TR();
            for (Entry entry : rowData) {
                switch (entry) {
                    case Entry.Attribute(String name, int attributeID) -> {
                        Integer iconID = attributeMap.get(attributeID).iconID;
                        Double value = attributeValueMap.get(type.typeID).get(attributeID);
                        if (value == null) throw new IllegalStateException("Missing attribute (" + attributeID + ") on type: " + type);
                        row.content(
                            TD().content(
                                SPAN("attribute_list_span").title(name).content(
                                    iconID != null ? IMG(ResourceLocation.ofIconID(iconID, context), null, 32).className("attribute_list_icon") : DIV("attribute_list_icon"),
                                    TEXT(name + ": "),
                                    context.sde.format_with_unit(value, attributeMap.get(attributeID).unitID)
                                )
                            )
                        );
                    }
                    case Entry.AttributeWithDefault(String name, int attributeID, double defaultValue) -> {
                        Integer iconID = attributeMap.get(attributeID).iconID;
                        double value = attributeValueMap.get(type.typeID).getOrDefault(attributeID, defaultValue);
                        row.content(
                            TD().content(
                                SPAN("attribute_list_span").title(name).content(
                                    iconID != null ? IMG(ResourceLocation.ofIconID(iconID, context), null, 32).className("attribute_list_icon") : DIV("attribute_list_icon"),
                                    TEXT(name + ": "),
                                    context.sde.format_with_unit(value, attributeMap.get(attributeID).unitID)
                                )
                            )
                        );
                    }
                    case Entry.AttributeSkipIfAbsent(String name, int attributeID) -> {
                        Integer iconID = attributeMap.get(attributeID).iconID;
                        Double value = attributeValueMap.get(type.typeID).get(attributeID);
                        if (value != null) {
                            row.content(
                                TD().content(
                                    SPAN("attribute_list_span").title(name).content(
                                        iconID != null ? IMG(ResourceLocation.ofIconID(iconID, context), null, 32).className("attribute_list_icon") : DIV("attribute_list_icon"),
                                        TEXT(name + ": "),
                                        context.sde.format_with_unit(value, attributeMap.get(attributeID).unitID)
                                    )
                                )
                            );
                        }
                    }
                    case Entry.Custom(BiConsumer<Element, HtmlContext> render) -> render.accept(row, context);
                }
            }
            if (!row.isEmpty()) {
                table.content(row);
            }
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
                flex-wrap: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .attribute_list_icon {
                width: 2rem;
                height: 2rem;
            }
            """;
    }

    public sealed interface Entry {
        record Attribute(String name, int attributeID) implements Entry {}
        record AttributeSkipIfAbsent(String name, int attributeID) implements Entry {}
        record AttributeWithDefault(String name, int attributeID, double defaultValue) implements Entry {}
        record Custom(BiConsumer<Element, HtmlContext> render) implements Entry {}
    }
}
