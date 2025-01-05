package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Type;

/**
 * Armor health stats of a ship {@link Type}
 *
 * @see ShipHealth
 */
public class ShipArmor extends ShipHealth {
    public ShipArmor(Type type) {
        super(type);
    }

    @Override
    protected int getHpAttribute() {
        return 265;
    }

    @Override
    protected String healthKindName() {
        return "Armor";
    }

    @Override
    protected Resists getResists(SDEData sdeData) {
        return super.getResists(sdeData, 267, 270, 269, 268);
    }
}
