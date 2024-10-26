package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.datatypes.Type;

/**
 * Drone stats of a ship {@link Type}
 */
public class ShipDrones extends AttributeList {
    public ShipDrones(Type type) {
        super(
            "Drones",
            type,
            false,
            new
                Entry[][]{{
                new Entry("Drone Capacity", 283),
                new Entry("Drone Bandwidth", 1271),
            }}
        );
    }
}
