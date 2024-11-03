package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.util.ResourceLocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays fitting stats of a ship {@link Type}
 */
public class ModuleFitting extends Component {
    private final Type type;

    /// Attributes that are shown on ModuleFitting being visible
    public static final Set<Integer> INCLUDED_ATTRIBUTES = new HashSet<>(Set.of(30, 50, 1153, 669, 6));
    static {
        for (int activationTimeAttribute : Type.ACTIVATION_TIME_ATTRIBUTES) {
            INCLUDED_ATTRIBUTES.add(activationTimeAttribute);
        }
    }

    public ModuleFitting(Type type) {
        super("module_fitting colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Double> typeAttributes = context.data.getTypeAttributes().getOrDefault(type.typeID, Map.of());
        Set<Integer> typeEffects = context.data.getTypeEffects().getOrDefault(type.typeID, Set.of());

        var table = TABLE("module_fitting_table");
        if (typeEffects.contains(11)) {
            table.content(TR().content(TD().content(
                SPAN("module_fitting_span").content(
                    IMG(ResourceLocation.iconOfIconID(295), null, 32).className("module_fitting_icon"),
                    TEXT("Low power slot")
                )
            )));
        } else if (typeEffects.contains(12)) {
            var row = TR();

            row.content(TD().content(
                SPAN("module_fitting_span").content(
                    IMG(ResourceLocation.iconOfIconID(293), null, 32).className("module_fitting_icon"),
                    TEXT("High power slot")
                )
            ));

            if (typeEffects.contains(42)) {
                row.content(TD().content(
                    SPAN("module_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(387), null, 32).className("module_fitting_icon"),
                        TEXT("Turret Hardpoint")
                    )
                ));
            } else if (typeEffects.contains(40)) {
                row.content(TD().content(
                    SPAN("module_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(168), null, 32).className("module_fitting_icon"),
                        TEXT("Launcher Hardpoint")
                    )
                ));
            }

            table.content(row);
        } else if (typeEffects.contains(13)) {
            table.content(TR().content(TD().content(
                SPAN("module_fitting_span").content(
                    IMG(ResourceLocation.iconOfIconID(294), null, 32).className("module_fitting_icon"),
                    TEXT("Medium power slot")
                )
            )));
        } else if (typeEffects.contains(2663)) {
            double rigSize = typeAttributes.getOrDefault(1547, 0.0);
            Element rigRow = TR();

            rigRow.content(TD().content(
                SPAN("module_fitting_span").content(
                    IMG(ResourceLocation.iconOfIconID(3266), null, 32).className("module_fitting_icon"),
                    context.data.format_with_unit(rigSize, context.data.getAttributes().get(1547).unitID),
                    TEXT(" rigging slot")
                )
            ));

            double calibrationUsage = typeAttributes.getOrDefault(1153, 0.0);
            if (calibrationUsage > 0) {
                rigRow.content(
                    TD().content(
                        SPAN("module_fitting_span").content(
                            IMG(ResourceLocation.iconOfIconID(3266), null, 32).className("module_fitting_icon"),
                            TEXT("Calibration: "), context.data.format_with_unit(calibrationUsage, context.data.getAttributes().get(1153).unitID)
                        )
                    )
                );
            }

            table.content(rigRow);
        } else if (typeEffects.contains(3772)) {
            String subsystemType = switch (type.groupID) {
                case 954 -> "Defensive";
                case 956 -> "Offensive";
                case 957 -> "Propulsion";
                case 958 -> "Core";
                default -> throw new IllegalStateException("Unknown subsystem kind: " + type.groupID);
            };

            table.content(TR().content(TD().content(
                SPAN("module_fitting_span").content(
                    IMG(ResourceLocation.iconOfIconID(3266), null, 32).className("module_fitting_icon"),
                    TEXT("Subsystem slot (" + subsystemType + ")")
                )
            )));
        }

        double pgUsage = typeAttributes.getOrDefault(30, 0.0);
        double cpuUsage = typeAttributes.getOrDefault(50, 0.0);

        boolean showPGCPU = pgUsage != 0.0 || cpuUsage != 0.0;
        if (showPGCPU) {
            table.content(TR().content(
                TD().content(
                    SPAN("module_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1405), null, 32).className("module_fitting_icon"),
                        TEXT("CPU usage: "), context.data.format_with_unit(cpuUsage, context.data.getAttributes().get(50).unitID)
                    )
                ),
                TD().content(
                    SPAN("module_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1400), null, 32).className("module_fitting_icon"),
                        TEXT("Powergrid usage: "), context.data.format_with_unit(pgUsage, context.data.getAttributes().get(30).unitID)
                    )
                )
            ));
        }

        double activationTime = type.getModuleActivationTime(context.data);
        double reactivationDelay = typeAttributes.getOrDefault(669, 0.0);

        double activationCost = typeAttributes.getOrDefault(6, 0.0);
        if (activationCost > 0.0) {
            var row = TR().content(
                TD().content(
                    SPAN("module_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1668), null, 32).className("module_fitting_icon"),
                        TEXT("Activation cost: "), context.data.format_with_unit(activationCost, context.data.getAttributes().get(6).unitID)
                    )
                )
            );

            if (activationTime > 0.0) {
                double capacitorUsage = activationCost / ((activationTime + reactivationDelay) / 1000.0);
                row.content(
                    TD().content(
                        SPAN("module_fitting_span").content(
                            IMG(ResourceLocation.iconOfIconID(1668), null, 32).className("module_fitting_icon"),
                            TEXT("Capacitor usage: "), context.data.format_with_unit(capacitorUsage, context.data.getAttributes().get(6).unitID), TEXT("/s")
                        )
                    )
                );
            }
            table.content(row);
        }
        if (activationTime > 0.0) {
            var row = TR().content(
                TD().content(
                    SPAN("module_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1392), null, 32).className("module_fitting_icon"),
                        TEXT("Activation time: "), context.data.format_with_unit(activationTime, context.data.getAttributes().get(73).unitID)
                    )
                )
            );

            if (reactivationDelay > 0.0) {
                row.content(
                    TD().content(
                        SPAN("module_fitting_span").content(
                            IMG(ResourceLocation.iconOfIconID(1392), null, 32).className("module_fitting_icon"),
                            TEXT("Reactivation delay: "), context.data.format_with_unit(reactivationDelay, context.data.getAttributes().get(669).unitID)
                        )
                    )
                );
            }

            table.content(row);
        }

        return new HTML[]{
            HEADER("font_header").text("Fitting"),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .module_fitting {
                padding: 0.5rem;
            }
            
            .module_fitting_table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 0.5rem;
            }
            
            .module_fitting_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .module_fitting_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .module_fitting_icon {
                display: inline;
                width: 2rem;
                height: 2rem;
            }""";
    }
}
