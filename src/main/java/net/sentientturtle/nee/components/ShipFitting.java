package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Type;


import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays fitting stats of a ship {@link Type}
 */
public class ShipFitting extends Component {
    private final Type type;

    public ShipFitting(Type type) {
        super("ship_fitting colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Double> typeAttributes = context.sde.getTypeAttributes().getOrDefault(type.typeID, Map.of());
        int highSlots = typeAttributes.getOrDefault(14, 0.0).intValue();
        int medSlots = typeAttributes.getOrDefault(13, 0.0).intValue();
        int lowSlots = typeAttributes.getOrDefault(12, 0.0).intValue();
        int rigSize = typeAttributes.getOrDefault(1547, 0.0).intValue();
        int rigSlots = typeAttributes.getOrDefault(1137, 0.0).intValue();
        int calibration = typeAttributes.getOrDefault(1132, 0.0).intValue();
        double powerGrid = typeAttributes.getOrDefault(11, 0.0);
        double cpuOutput = typeAttributes.getOrDefault(48, 0.0);
        int turretHardpoints = typeAttributes.getOrDefault(102, 0.0).intValue();
        int launcherHardpoints = typeAttributes.getOrDefault(101, 0.0).intValue();
        int subsystemSlots = typeAttributes.getOrDefault(1367, 0.0).intValue();

        double capacitorCapacity = typeAttributes.getOrDefault(482, 0.0);
        double capacitorRecharge = typeAttributes.getOrDefault(55, 0.0);

        double signatureRadius = typeAttributes.getOrDefault(552, 0.0);

        // Determine what parts of this component should be shown
        boolean showHMLSlots = highSlots != 0 || medSlots != 0 || lowSlots != 0;
        boolean showSubsystemSlots = subsystemSlots != 0;
        boolean showRigSlots = rigSlots != 0 || calibration != 0;
        boolean showPGCPU = powerGrid != 0 || cpuOutput != 0;
        boolean showTurrets = turretHardpoints != 0;
        boolean showLaunchers = launcherHardpoints != 0;
        boolean showCapacitor = capacitorCapacity != 0.0;
        boolean showSignature = signatureRadius != 0.0;

        var slot_table = TABLE("ship_fitting_table ship_fitting_table_slots");
        var stat_table = TABLE("ship_fitting_table");

        if (showTurrets) {
            slot_table.content(TR().content(
                TD().text("Turrets"),
                TD().content(HTML.repeat(turretHardpoints, IMG(ResourceLocation.iconOfIconID(387, context), null, 32).className("ship_fitting_icon"))),
                TD().content(context.sde.format_with_unit(turretHardpoints, context.sde.getAttributes().get(102).unitID))
            ));
        }
        if (showLaunchers) {
            slot_table.content(TR().content(
                TD().text("Launchers"),
                TD().content(HTML.repeat(launcherHardpoints, IMG(ResourceLocation.iconOfIconID(168, context), null, 32).className("ship_fitting_icon"))),
                TD().content(context.sde.format_with_unit(launcherHardpoints, context.sde.getAttributes().get(101).unitID))
            ));
        }
        if (showHMLSlots) {
            slot_table.content(TR().content(
                TD().text("High power"),
                TD().content(HTML.repeat(highSlots, IMG(ResourceLocation.iconOfIconID(293, context), null, 32).className("ship_fitting_icon"))),
                TD().content(context.sde.format_with_unit(highSlots, context.sde.getAttributes().get(14).unitID))
            ), TR().content(
                TD().text("Medium power"),
                TD().content(HTML.repeat(medSlots, IMG(ResourceLocation.iconOfIconID(294, context), null, 32).className("ship_fitting_icon"))),
                TD().content(context.sde.format_with_unit(medSlots, context.sde.getAttributes().get(13).unitID))
            ), TR().content(
                TD().text("Low power"),
                TD().content(HTML.repeat(lowSlots, IMG(ResourceLocation.iconOfIconID(295, context), null, 32).className("ship_fitting_icon"))),
                TD().content(context.sde.format_with_unit(lowSlots, context.sde.getAttributes().get(12).unitID))
            ));
        }
        if (showSubsystemSlots) {
            slot_table.content(TR().content(
                TD().text("Subsystem"),
                TD().content(HTML.repeat(subsystemSlots, IMG(ResourceLocation.iconOfIconID(3756, context), null, 32).className("ship_fitting_icon"))),
                TD().content(context.sde.format_with_unit(subsystemSlots, context.sde.getAttributes().get(1367).unitID))
            ));
        }
        if (showRigSlots) {
            slot_table.content(TR().content(
                TD().content(context.sde.format_with_unit(rigSize, context.sde.getAttributes().get(1547).unitID), TEXT(" rig")),
                TD().content(HTML.repeat(rigSlots, IMG(ResourceLocation.iconOfIconID(3266, context), null, 32).className("ship_fitting_icon"))),
                TD().content(
                    context.sde.format_with_unit(rigSlots, context.sde.getAttributes().get(1137).unitID),
                    BR(), TEXT("("),
                    context.sde.format_with_unit(calibration, -1),
                    TEXT(" calibration)")
                )
            ));
        }
        if (showPGCPU) {
            stat_table.content(TR().content(
                TD().content(
                    SPAN("ship_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1405, context), null, 32).className("ship_fitting_icon"),
                        TEXT("CPU Output: "), context.sde.format_with_unit(cpuOutput, context.sde.getAttributes().get(48).unitID)
                    )
                ),
                TD().content(
                    SPAN("ship_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1400, context), null, 32).className("ship_fitting_icon"),
                        TEXT("Powergrid output: "), context.sde.format_with_unit(powerGrid, context.sde.getAttributes().get(11).unitID)
                    )
                )
            ));
        }
        if (showCapacitor) {
            // TODO: Max sustained load
            stat_table.content(TR().content(
                TD().content(
                    SPAN("ship_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1668, context), null, 32).className("ship_fitting_icon"),
                        TEXT("Capacitor capacity: "), context.sde.format_with_unit(capacitorCapacity, context.sde.getAttributes().get(482).unitID)
                    )
                ),
                TD().content(
                    SPAN("ship_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1392, context), null, 32).className("ship_fitting_icon"),
                        TEXT("Capacitor recharge: "), context.sde.format_with_unit(capacitorRecharge, context.sde.getAttributes().get(55).unitID)
                    )
                )
            ));
        }
        if (showSignature) {
            stat_table.content(TR().content(
                TD().content(
                    SPAN("ship_fitting_span").content(
                        IMG(ResourceLocation.iconOfIconID(1390, context), null, 32).className("ship_fitting_icon"),
                        TEXT("Signature radius: "), context.sde.format_with_unit(signatureRadius, context.sde.getAttributes().get(552).unitID)
                    )
                )
            ));
        }



        if (stat_table.isEmpty()) {
            return new HTML[]{
                HEADER("font_header").text("Fitting"),
                slot_table
            };
        } else if (slot_table.isEmpty()) {
            return new HTML[]{
                HEADER("font_header").text("Fitting"),
                stat_table
            };
        } else {
            return new HTML[]{
                HEADER("font_header").text("Fitting"),
                slot_table,
                stat_table
            };
        }
    }

    @Override
    protected String getCSS() {
        return """
            .ship_fitting {
                padding: 0.5rem;
            }
            
            .ship_fitting_table {
                width: 100%;
                border-collapse: collapse;
            }
            
            .ship_fitting_table_slots {
                margin-top: 0.5rem;
                border-bottom: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .ship_fitting_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .ship_fitting_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .ship_fitting_icon {
                display: inline;
                width: 2rem;
                height: 2rem;
            }""";
    }
}
