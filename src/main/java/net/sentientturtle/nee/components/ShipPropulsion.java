package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.datatypes.Type;

/**
 * Propulsion stats of a ship {@link Type}
 */
public class ShipPropulsion extends AttributeList {
    public ShipPropulsion(Type type) {
        super(
            "Propulsion",
            type,
            true,
            new Entry[][]{{
                new Entry("Maximum Velocity", 37),
                new Entry("Warp Speed", 600)    // TODO: Inertia, derive align time
            }}
        );
    }
}
