package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.IndustryActivity;
import net.sentientturtle.nee.data.datatypes.PlanetSchematic;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.page.TypePage;
import net.sentientturtle.nee.data.ResourceLocation;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.sentientturtle.html.HTML.*;

/**
 * Industry origin of a {@link Type}; "This type is produced from this blueprint"
 */
public class TypeBlueprint extends Component {
    private final Type type;
    private static final double[] RESEARCH_TIME_MULTIPLIERS = new double[]{
        105.0 / 105.0,
        250.0 / 105.0,
        595.0 / 105.0,
        1414.0 / 105.0,
        3360.0 / 105.0,
        8000.0 / 105.0,
        19000.0 / 105.0,
        45255.0 / 105.0,
        107700.0 / 105.0,
        256000.0 / 105.0
    };
    private static final double[] RESEARCH_TIME_CUMULATIVE_MULTIPLIERS = new double[]{
        105.0 / 105.0,
        (105.0 + 250.0) / 105.0,
        (105.0 + 250.0 + 595.0) / 105.0,
        (105.0 + 250.0 + 595.0 + 1414.0) / 105.0,
        (105.0 + 250.0 + 595.0 + 1414.0 + 3360.0) / 105.0,
        (105.0 + 250.0 + 595.0 + 1414.0 + 3360.0 + 8000.0) / 105.0,
        (105.0 + 250.0 + 595.0 + 1414.0 + 3360.0 + 8000.0 + 19000.0) / 105.0,
        (105.0 + 250.0 + 595.0 + 1414.0 + 3360.0 + 8000.0 + 19000.0 + 45255.0) / 105.0,
        (105.0 + 250.0 + 595.0 + 1414.0 + 3360.0 + 8000.0 + 19000.0 + 45255.0 + 107700.0) / 105.0,
        (105.0 + 250.0 + 595.0 + 1414.0 + 3360.0 + 8000.0 + 19000.0 + 45255.0 + 107700.0 + 256000.0) / 105.0
    };

    public TypeBlueprint(Type type) {
        super("type_blueprint colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        var table = TABLE("type_blueprint_table font_header");

        AtomicBoolean isFirst = new AtomicBoolean(true);
        Map<Integer, IndustryActivity> bpActivities = context.sde.getBpActivities().getOrDefault(type.typeID, Map.of());
        bpActivities.keySet().stream()
            .sorted(Comparator.comparingInt(value -> switch (value) {   // Sort in custom order
                case 1 -> 1;
                case 8 -> 2;
                case 5 -> 3;
                case 3 -> 4;
                case 4 -> 5;
                default -> value;
            }))
            .forEach(key -> {
                IndustryActivity activity = bpActivities.get(key);

                int metaGroup = context.sde.getMetaTypes().getOrDefault(activity.bpTypeID, 1);
                if (metaGroup != 1 && metaGroup != 54 && (activity.activityID == 3 || activity.activityID == 4 || activity.activityID == 5)) {
                    // Skip research & copying on Faction/T2/T3 blueprints, for which originals are generally unavailable
                    return;
                }

                if (activity.activityID == 4) {
                    // Time & Material efficiency are merged
                    return;
                }

                String activityName = switch (activity.activityID) {
                    case 1 -> "Manufacturing";
                    case 3 -> "Efficiency Research";
                    case 5 -> "Blueprint Copying";
                    case 8 -> "Invention";
                    case 11 -> "Reactions";
                    default -> throw new IllegalStateException("Unknown activityID: " + activity.activityID);
                };

                if (isFirst.get()) {
                    isFirst.set(false);
                } else {
                    table.content(TR("type_blueprint_spacer"));
                }
                table.content(TR().content(TH().attribute("colspan", "3").text(activityName)));

                if (activity.productMap.size() > 0) {
                    String outputDisplayText = activity.productMap.size() > 1 ? "Output (select one)" : "Output";
                    table.content(TR().content(TH("type_blueprint_subheader").attribute("colspan", "3").text(outputDisplayText)));   // TODO: Copy-this buttons
                    for (Map.Entry<Integer, Integer> entry : activity.productMap.entrySet()) {  // TODO: These should all be sorted, or perhaps pre-sort DataSupplier and remove sorting in components?
                        Element quantityTD = TD().content(context.sde.format_with_unit(entry.getValue(), -1));
                        Double probability = activity.probabilityMap.get(entry.getKey());
                        if (probability != null) {
                            quantityTD.content(TEXT(" ("), context.sde.format_with_unit(probability, 127), TEXT(")"));
                        }

                        table.content(TR().content(
                            TD().content(IMG(ResourceLocation.typeIcon(entry.getKey(), context), null, 64).className("type_blueprint_icon")),
                            TD().content(new PageLink(new TypePage(context.sde.getTypes().get(entry.getKey())))),
                            quantityTD
                        ));
                    }
                }

                if (activity.materialMap.size() > 0) {
                    table.content(TR().content(TH("type_blueprint_subheader").attribute("colspan", "3").text("Input")));

                    activity.materialMap.entrySet()
                        .stream()
                        .sorted(Comparator.<Map.Entry<Integer, Integer>>comparingInt(Map.Entry::getValue).reversed())
                        .forEach(entry -> {
                            table.content(TR().content(
                                TD().content(IMG(ResourceLocation.typeIcon(entry.getKey(), context), null, 64).className("type_blueprint_icon")),
                                TD().content(new PageLink(new TypePage(context.sde.getTypes().get(entry.getKey())))),
                                TD().content(context.sde.format_with_unit(entry.getValue(), -1))
                            ));
                        });
                }

                if (activity.time > 0 && activity.activityID != 3 && activity.activityID != 4) {
                    table.content(TR().content(
                        TD().attribute("colspan", "2").text("Duration:"),
                        TD().content(context.sde.format_with_unit(activity.time, 3))
                    ));
                } else if (activity.time > 0) {
                    for (int i = 0; i < RESEARCH_TIME_MULTIPLIERS.length; i++) {
                        table.content(TR().content(TD().attribute("colspan", "3").content(
                            SPAN("type_blueprint_research").content(
                                SPAN().content(
                                    TEXT("Level " + (i + 1) + ": "),
                                    context.sde.format_with_unit(activity.time * RESEARCH_TIME_MULTIPLIERS[i], 3)
                                ),
                                SPAN().content(
                                    TEXT(" (Total: "),
                                    context.sde.format_with_unit(activity.time * RESEARCH_TIME_CUMULATIVE_MULTIPLIERS[i], 3),
                                    TEXT(")")
                                )
                            )
                        )));
                    }
                }

                if (activity.skillMap.size() > 0) {
                    table.content(TR().content(TH("type_blueprint_subheader").attribute("colspan", "3").text("Required Skills")));

                    for (Map.Entry<Integer, Integer> entry : activity.skillMap.entrySet()) {
                        int level = entry.getValue();
                        if (level < 0 || level > 5) throw new RuntimeException("Invalid skill level: " + level);

                        table.content(TR().content(TD().attribute("colspan", "3").content(
                            SPAN("type_blueprint_skill").content(
                                context.sde.format_with_unit(entry.getKey(), 116), // 116 = typeID unit
                                SPAN("type_blueprint_skill_level font_roman_numeral").text(" " + level + " ").content(
                                    SPAN("type_blueprint_skill_indicator").text("■".repeat(level) + "□".repeat(5 - level))
                                )
                            )
                        )));
                    }
                }
            });

        PlanetSchematic schematic = context.sde.getOutputSchematicMap().get(this.type.typeID);
        if (schematic != null) {
            table.content(TR().content(TH().attribute("colspan", "3").text("Planetary Industry")));

            table.content(TR().content(TH("type_blueprint_subheader").attribute("colspan", "3").text("Output")));
            table.content(TR().content(
                TD().content(IMG(ResourceLocation.typeIcon(schematic.outputTypeID, context), null, 64).className("type_blueprint_icon")),
                TD().content(new PageLink(new TypePage(context.sde.getTypes().get(schematic.outputTypeID)))),
                TD().content(context.sde.format_with_unit(schematic.outputQuantity, -1))
            ));

            table.content(TR().content(TH("type_blueprint_subheader").attribute("colspan", "3").text("Input")));
            schematic.inputs.entrySet()
                .stream()
                .sorted(Comparator.<Map.Entry<Integer, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .forEach(entry -> {
                    table.content(TR().content(
                        TD().content(IMG(ResourceLocation.typeIcon(entry.getKey(), context), null, 64).className("type_blueprint_icon")),
                        TD().content(new PageLink(new TypePage(context.sde.getTypes().get(entry.getKey())))),
                        TD().content(context.sde.format_with_unit(entry.getValue(), -1))
                    ));
                });

            table.content(TR().content(
                TD().attribute("colspan", "2").text("Cycle time:"),
                TD().content(context.sde.format_with_unit(schematic.cycleTime, 3))
            ));
        }

        int categoryID = context.sde.getGroups().get(type.groupID).categoryID;
        String title = "Blueprint";
        if (categoryID == 34) {
            title = "Relic";
        } else if (categoryID == 43) {
            title = "Planetary Industry Schematic";
        }

        return new HTML[]{
            HEADER("font_header").text(title),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .type_blueprint {
                padding: 0.5rem;
            }
            
            .type_blueprint_table {
                margin-top: 0.5rem;
                width: 100%;
            }
            
            .type_blueprint_spacer {
                height: 1.5rem;
            }
            
            .type_blueprint_subheader {
                font-weight: normal;
            }
            
            .type_blueprint_research, .type_blueprint_skill {
                display: flex;
                justify-content: space-between;
            }
            
            .type_blueprint_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .type_blueprint_skill_level {
                white-space: pre;
                display: inline;
            }
            
            .type_blueprint_skill_indicator {
                user-select: none;
            }""";
    }
}
