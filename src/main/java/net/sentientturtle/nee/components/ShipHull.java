package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.datatypes.Type;

/**
 * Hull health stats of a ship {@link Type}
 *
 * @see ShipHealth
 */
public class ShipHull extends ShipHealth {
    public ShipHull(Type type) {
        super(type);
    }

    @Override
    protected int getHpAttribute() {
        return 9;
    }

    @Override
    protected String healthKindName() {
        return "Hull";
    }

    @Override
    protected Resists getResists(DataSupplier dataSupplier) {
        return super.getResists(dataSupplier, 113, 110, 109, 111);
    }
}
