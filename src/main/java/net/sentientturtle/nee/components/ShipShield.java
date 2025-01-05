package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.Map;
import java.util.Objects;

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
    protected Resists getResists(SDEData sdeData) {
        return super.getResists(sdeData, 271, 274, 273, 272);
    }

    @Override
    protected HTML[] getRechargeText(SDEData sdeData) {
        double recharge = sdeData.getTypeAttributes().get(super.type.typeID).getOrDefault(479, 0.0);
        if (recharge > 0 && recharge < 86400000) {    // Some types have "as good as infinite" recharge times
            return new HTML[]{TEXT("Shield recharge time: "), sdeData.format_with_unit(recharge, sdeData.getAttributes().get(479).unitID)};
        } else {
            return null;
        }
    }

    @Override
    protected HTML[] getSustainText(SDEData sdeData) {
        assert Objects.equals(101, sdeData.getAttributes().get(479).unitID);
        Map<Integer, Double> attributes = sdeData.getTypeAttributes().get(super.type.typeID);
        double HP = attributes.getOrDefault(getHpAttribute(), 0.0);
        double recharge = attributes.getOrDefault(479, 0.0);
        double sustain = 2500.0 * HP / recharge;

        if (recharge > 0 && (recharge < 86400000 || sustain > 10.0)) {        // Some types have "as good as infinite" recharge times; If they have relevant sustain show regenerated HP
            return new HTML[]{TEXT("Shield peak recharge: "), sdeData.format_with_unit(sustain, -4)};
        } else {
            return null;
        }
    }
}
