package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Type;

import static net.sentientturtle.html.HTML.*;


/**
 * Damage resistance and health stats of a given subtype for a given ship {@link Type}
 * Health-subtype implemented by subclasses.
 *
 * @see ShipShield
 * @see ShipArmor
 * @see ShipHull
 */
public abstract class ShipHealth extends Component {
    protected Type type;

    ShipHealth(Type type) {
        super("ship_health colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        var table = TABLE("ship_health_table font_text").content(
            TR().content(
                TD("ship_health_icon").content(IMG(ResourceLocation.ofIconID(context.sde.getAttributes().get(getHpAttribute()).iconID, context), null, 32)),
                TD().content(TEXT(healthKindName() + " Hitpoints: "), getHp(context.sde, getHpAttribute())),
                TD()    // Intentionally blank column
            )
        );


        HTML[] rechargeText = getRechargeText(context.sde);
        if (rechargeText != null) {
            table.content(
                TR().content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.ofIconID(context.sde.getAttributes().get(getHpAttribute()).iconID, context), null, 32)),
                    TD().content(rechargeText),
                    TD()    // Intentionally blank column
                )
            );
        }

        HTML[] sustainText = getSustainText(context.sde);
        if (sustainText != null) {
            table.content(
                TR().content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.ofIconID(1392, context), null, 32)),
                    TD().content(sustainText),
                    TD()    // Intentionally blank column
                )
            );
        }

        Resists resists = getResists(context.sde);
        if (resists != null) {
            table.content(
                TR().title("Electromagnetic Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.ofIconID(1388, context), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").attribute("aria-label", "EM resis").content(
                        DIV("ship_health_em_resist")
                            .attribute("aria-label", "Electromagnetic Damage Resistance")
                            .style("width: " + (1 - resists.EM) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.sde.format_with_unit(resists.EM, context.sde.getAttributes().get(267).unitID))
                ),
                TR().title("Thermal Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.ofIconID(1386, context), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").content(
                        DIV("ship_health_th_resist")
                            .attribute("aria-label", "Thermal Damage Resistance")
                            .style("width: " + (1 - resists.TH) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.sde.format_with_unit(resists.TH, context.sde.getAttributes().get(270).unitID))
                ),
                TR().title("Kinetic Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.ofIconID(1385, context), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").content(
                        DIV("ship_health_ki_resist")
                            .attribute("aria-label", "Kinetic Damage Resistance")
                            .style("width: " + (1 - resists.KI) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.sde.format_with_unit(resists.KI, context.sde.getAttributes().get(269).unitID))
                ),
                TR().title("Explosive Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.ofIconID(1387, context), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").content(
                        DIV("ship_health_ex_resist")
                            .attribute("aria-label", "Explosive Damage Resistance")
                            .style("width: " + (1 - resists.EX) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.sde.format_with_unit(resists.EX, context.sde.getAttributes().get(268).unitID))
                )
            );
        }

        return new HTML[]{
            HEADER("font_header").text(this.healthKindName()),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .ship_health {
                padding: 0.5rem;
            }
            .ship_health_table {
                width: 100%;
                margin-top: 0.5rem;
            }
            
            .ship_health_icon > img {
                width: 2rem;
                height: 2rem;
            }
            
            .ship_health_text_td {
                padding-left: 1rem;
                padding-right: 1rem;
                white-space: nowrap;
            }
            
            .ship_health_bar {
                height: 1.5rem;
                width: 100%;
                padding: 0.25rem;
            }
            
            .ship_health_resist_bg {
                width: 100%;
                height: 100%;
                background: #242526;
            }
            
            .ship_health_em_resist {
                height: 100%;
                background: #258BCE;
            }
            
            .ship_health_th_resist {
                height: 100%;
                background: #CE2525;
            }
            
            .ship_health_ki_resist {
                height: 100%;
                background: #A9A9A9;
            }
            
            .ship_health_ex_resist {
                height: 100%;
                background: #CE8B25;
            }""";
    }

    /**
     * @return Attribute for the Hitpoints attribute for the health type implemented by subclass
     */
    protected abstract int getHpAttribute();

    /**
     * @return Name of health type implemented by subclass
     */
    protected abstract String healthKindName();

    /**
     * Specifies resist attribute values for the health type implemented by subclass
     *
     * @param sdeData Data supplier to use
     * @return Damage resistance values, as damage multiplier value (i.e. 60% resistance = 0.4)
     */
    protected abstract Resists getResists(SDEData sdeData);

    /**
     * Specifies text to be shown for HP regeneration time
     *
     * @param sdeData Data supplier to use
     * @return Text to be shown for HP regeneration time, or null if no such text should be shown
     */
    protected HTML[] getRechargeText(SDEData sdeData) {
        return null;
    }

    /**
     * Specifies text to be shown for HP regeneration sustain
     *
     * @param sdeData Data supplier to use
     * @return Text to be shown for HP regeneration sustain, or null if no such text should be shown
     */
    protected HTML[] getSustainText(SDEData sdeData) {
        return null;
    }

    // Helper method for fetching hitpoints string
    private HTML getHp(SDEData sdeData, int hpAttributeID) {
        return sdeData.format_with_unit(sdeData.getTypeAttributes().get(type.typeID).get(hpAttributeID), sdeData.getAttributes().get(hpAttributeID).unitID);
    }


    /**
     * Helper method for subclasses, fetches resists from a given data supplier using attributes for the resists of the health type implemented by subclass
     *
     * @param sdeData Data supplier to use
     * @param emAttribute  Electromagnetic resistance attributeID
     * @param thAttribute  Thermal resistance attributeID
     * @param kiAttribute  Kinetic resistance attributeID
     * @param exAttribute  Explosive resistance attributeID
     * @see #getResists(SDEData)
     */
    @SuppressWarnings("WeakerAccess")
    protected Resists getResists(SDEData sdeData, int emAttribute, int thAttribute, int kiAttribute, int exAttribute) {
        return new Resists(
            sdeData.getTypeAttributes().get(type.typeID).getOrDefault(emAttribute, 1.0),   // Default to 1.0, which is 0% resistance
            sdeData.getTypeAttributes().get(type.typeID).getOrDefault(thAttribute, 1.0),
            sdeData.getTypeAttributes().get(type.typeID).getOrDefault(kiAttribute, 1.0),
            sdeData.getTypeAttributes().get(type.typeID).getOrDefault(exAttribute, 1.0)
        );
    }

    protected record Resists(double EM, double TH, double KI, double EX) {}
}
