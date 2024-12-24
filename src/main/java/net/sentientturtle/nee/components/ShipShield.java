package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Type;

import static net.sentientturtle.html.HTML.TEXT;

/**
 * Shield health stats of a ship {@link Type}
 *
 * @see ShipHealth
 */
public class ShipShield extends ShipHealth {
    public ShipShield(Type type) {
        super(type);
    }

    @Override
    protected int getHpAttribute() {
        return 263;
    }

    @Override
    protected String healthKindName() {
        return "Shield";
    }

    @Override
    protected Resists getResists(SDEData SDEData) {
        return super.getResists(SDEData, 271, 274, 273, 272);
    }

    @Override
    protected HTML[] getRechargeText(SDEData SDEData) {
        double recharge = SDEData.getTypeAttributes().get(super.type.typeID).getOrDefault(479, 0.0);
        if (recharge > 0 && recharge < 86400000) {    // Some types have "as good as infinite" recharge times
            return new HTML[]{TEXT("Shield recharge time: "), SDEData.format_with_unit(recharge, SDEData.getAttributes().get(479).unitID)};
        } else {
            return null;
        }
    }
}
