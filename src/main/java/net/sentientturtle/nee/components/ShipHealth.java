package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.DataSupplier;
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
                TD("ship_health_icon").content(IMG(ResourceLocation.iconOfIconID(context.data.getAttributes().get(getHpAttribute()).iconID), null, 32)),
                TD().content(TEXT(healthKindName() + " Hitpoints: "), getHp(context.data, getHpAttribute())),
                TD()    // Intentionally blank column
            )
        );


        HTML[] rechargeText = getRechargeText(context.data);
        if (rechargeText != null) {
            table.content(
                TR().content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.iconOfIconID(1392), null, 32)),
                    TD().content(rechargeText),
                    TD()    // Intentionally blank column
                )
            );
        }


        Resists resists = getResists(context.data);
        if (resists != null) {
            table.content(
                TR().title("Electromagnetic Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.iconOfIconID(1388), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").content(
                        DIV("ship_health_em_resist").style("width: " + (1 - resists.EM) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.data.format_with_unit(resists.EM, context.data.getAttributes().get(267).unitID))
                ),
                TR().title("Thermal Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.iconOfIconID(1386), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").content(
                        DIV("ship_health_th_resist").style("width: " + (1 - resists.TH) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.data.format_with_unit(resists.TH, context.data.getAttributes().get(270).unitID))
                ),
                TR().title("Kinetic Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.iconOfIconID(1385), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").content(
                        DIV("ship_health_ki_resist").style("width: " + (1 - resists.KI) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.data.format_with_unit(resists.KI, context.data.getAttributes().get(269).unitID))
                ),
                TR().title("Explosive Damage Resistance").content(
                    TD("ship_health_icon").content(IMG(ResourceLocation.iconOfIconID(1387), null, 32)),
                    TD("ship_health_bar").content(DIV("ship_health_resist_bg").content(
                        DIV("ship_health_ex_resist").style("width: " + (1 - resists.EX) * 100 + "%;")
                    )),
                    TD("ship_health_text").content(context.data.format_with_unit(resists.EX, context.data.getAttributes().get(268).unitID))
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
            }
            
            .ship_health_icon {
                width: 2rem;
                margin-top: 0.5rem;
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
     * @param dataSupplier Data supplier to use
     * @return Damage resistance values, as damage multiplier value (i.e. 60% resistance = 0.4)
     */
    protected abstract Resists getResists(DataSupplier dataSupplier);

    /**
     * Specifies text to be shown for HP regeneration for the health type implemented by subclass
     *
     * @param dataSupplier Data supplier to use
     * @return Text to be shown for HP regeneration, or null if no such text should be shown
     */
    protected HTML[] getRechargeText(DataSupplier dataSupplier) {
        return null;
    }

    // Helper method for fetching hitpoints string
    private HTML getHp(DataSupplier dataSupplier, int hpAttributeID) {
        return dataSupplier.format_with_unit(dataSupplier.getTypeAttributes().get(type.typeID).get(hpAttributeID), dataSupplier.getAttributes().get(hpAttributeID).unitID);
    }


    /**
     * Helper method for subclasses, fetches resists from a given data supplier using attributes for the resists of the health type implemented by subclass
     *
     * @param dataSupplier Data supplier to use
     * @param emAttribute  Electromagnetic resistance attributeID
     * @param thAttribute  Thermal resistance attributeID
     * @param kiAttribute  Kinetic resistance attributeID
     * @param exAttribute  Explosive resistance attributeID
     * @see #getResists(DataSupplier)
     */
    @SuppressWarnings("WeakerAccess")
    protected Resists getResists(DataSupplier dataSupplier, int emAttribute, int thAttribute, int kiAttribute, int exAttribute) {
        return new Resists(
            dataSupplier.getTypeAttributes().get(type.typeID).getOrDefault(emAttribute, 0.0),
            dataSupplier.getTypeAttributes().get(type.typeID).getOrDefault(thAttribute, 0.0),
            dataSupplier.getTypeAttributes().get(type.typeID).getOrDefault(kiAttribute, 0.0),
            dataSupplier.getTypeAttributes().get(type.typeID).getOrDefault(exAttribute, 0.0)
        );
    }

    protected record Resists(double EM, double TH, double KI, double EX) {}
}
