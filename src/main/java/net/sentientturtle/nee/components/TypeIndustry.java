package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.datatypes.IndustryActivity;
import net.sentientturtle.nee.data.datatypes.PlanetSchematic;
import net.sentientturtle.nee.pages.TypePage;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import static net.sentientturtle.html.HTML.*;

/**
 * Industry origin of a {@link Type}; "This type is produced from this blueprint"
 */
public class TypeIndustry extends Component {
    private final Type type;

    public TypeIndustry(Type type) {
        super("type_industry colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        var table = TABLE("type_industry_table font_header");


        Comparator<Type> typeComparator = Type.comparator(context.data);

        Set<IndustryActivity> outputActivities = context.data.getProductActivityMap().getOrDefault(type.typeID, Set.of());
        Set<Integer> reprocessingSources = context.data.getOreReprocessingMap().getOrDefault(type.typeID, Set.of());
        if (outputActivities.size() > 0 || reprocessingSources.size() > 0) {
            table.content(TR().content(TH().attribute("colspan", "3").text("Produced from")));
            for (IndustryActivity activity : outputActivities) {
                table.content(TR().content(
                    TD().content(IMG(ResourceLocation.typeIcon(activity.bpTypeID, context), null, 64).className("type_industry_icon")),
                    TD().content(new PageLink(new TypePage(context.data.getTypes().get(activity.bpTypeID)))),
                    TD().content(TEXT(context.data.getIndustryActivityTypes().get(activity.activityID).activityName))
                ));
            }

            reprocessingSources.stream()
                .map(context.data.getTypes()::get)
                .sorted(typeComparator)
                .forEach(type -> {
                    table.content(TR().content(
                        TD().content(IMG(ResourceLocation.typeIcon(type.typeID, context), null, 64).className("type_industry_icon")),
                        TD().content(new PageLink(new TypePage(type))),
                        TD().content(TEXT("Reprocessing"))
                    ));
                });
        }
        Comparator<IndustryActivity> activityComparator = (o1, o2) -> typeComparator.compare(
            context.data.getTypes().get(o1.bpTypeID),
            context.data.getTypes().get(o2.bpTypeID)
        );

        Set<PlanetSchematic> inputSchematics = context.data.getInputSchematicMap().getOrDefault(type.typeID, Set.of());
        Set<IndustryActivity> inputActivities = context.data.getMaterialActivityMap().getOrDefault(type.typeID, Set.of());
        if (inputSchematics.size() > 0 || inputActivities.size() > 0) {
            table.content(TR().content(TH().attribute("colspan", "3").text("Required for")));

            inputSchematics.stream()
                .map(schematic -> context.data.getTypes().get(schematic.outputTypeID))
                .sorted(Type.comparator(context.data))
                .forEach(outputType -> {
                    table.content(TR().content(
                        TD().content(IMG(ResourceLocation.typeIcon(outputType.typeID, context), null, 64).className("type_industry_icon")),
                        TD().content(new PageLink(new TypePage(outputType))),
                        TD().content(TEXT("Planetary Industry"))
                    ));
                });

            inputActivities.stream().sorted(activityComparator).forEach(activity -> {
                table.content(TR().content(
                    TD().content(IMG(ResourceLocation.typeIcon(activity.bpTypeID, context), null, 64).className("type_industry_icon")),
                    TD().content(new PageLink(new TypePage(context.data.getTypes().get(activity.bpTypeID)))),
                    TD().content(TEXT(context.data.getIndustryActivityTypes().get(activity.activityID).activityName))
                ));
            });
        }

        Map<Integer, Integer> reprocessingMaterials = context.data.getReprocessingMaterials().getOrDefault(type.typeID, Map.of());
        if (reprocessingMaterials.size() > 0) {
            table.content(TR().content(TH("font_header").attribute("colspan", "3").text("Reprocessing")));
            for (Map.Entry<Integer, Integer> entry : reprocessingMaterials.entrySet()) {
                table.content(TR().content(
                    TD().content(IMG(ResourceLocation.typeIcon(entry.getKey(), context), null, 64).className("type_industry_icon")),
                    TD().content(new PageLink(new TypePage(context.data.getTypes().get(entry.getKey())))),
                    TD().content(context.data.format_with_unit(entry.getValue(), -1))
                ));
            }
        }

        return new HTML[]{
            HEADER("font_header").text("Industry"),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .type_industry {
                padding: 0.5rem;
            }
            
            .type_industry_table {
                margin-top: 0.5rem;
                width: 100%;
            }
            
            .type_industry_icon {
                width: 2rem;
                height: 2rem;
            }""";
    }
}
