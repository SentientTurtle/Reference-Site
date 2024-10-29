package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.MetaGroup;
import net.sentientturtle.nee.pages.TypePage;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.*;

import static net.sentientturtle.html.HTML.*;

/**
 * Variants of a {@link Type}
 */
public class TypeVariants extends Component {
    private final Type type;

    public TypeVariants(Type type) {
        super("type_variants colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, MetaGroup> metaGroups = context.data.getMetaGroups();
        HashMap<Integer, Set<Integer>> metaVariants = new HashMap<>();
        for (int variantID : context.data.getVariants().get(type.typeID)) {
            Integer metaGroupID = context.data.getMetaTypes().getOrDefault(variantID, 1);
            metaVariants.computeIfAbsent(metaGroupID, _ -> new HashSet<>()).add(variantID);
        }

        var table = TABLE("type_variants_table font_text");

        metaVariants.entrySet()
            .stream()
            .sorted(MetaGroup.orderedByMetaGroup(Map.Entry::getKey))
            .forEach(entry -> {
                table.content(TR().content(TH("font_header").attribute("colspan", "2").text(metaGroups.get(entry.getKey()).metaGroupName)));
                for (Integer variantID : (Iterable<? extends Integer>) entry.getValue().stream().sorted()::iterator) {
                    table.content(TR().content(
                        TD().content(IMG(ResourceLocation.iconOfTypeID(variantID), null, 64).className("type_variants_icon")),
                        TD("font_header").content(SPAN("type_variants_type").content(new PageLink(new TypePage(context.data.getTypes().get(variantID)))))
                    ));
                }
            });

        return new HTML[]{
            HEADER("font_header").text("Variants"),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .type_variants {
                padding: 0.5rem;
            }
            
            .type_variants_table {
                margin-top: 0.5rem;
            }
            
            .type_variants_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .type_variants_type {
                font-size: 1.25rem;
                margin: 0.5rem;
            }""";
    }
}
