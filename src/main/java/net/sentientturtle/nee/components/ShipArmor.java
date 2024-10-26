package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.DataSupplier;
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
    protected Resists getResists(DataSupplier dataSupplier) {
        return super.getResists(dataSupplier, 267, 270, 269, 268);
    }
}
