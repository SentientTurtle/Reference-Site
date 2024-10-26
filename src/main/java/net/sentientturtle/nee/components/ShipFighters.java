package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.datatypes.Type;

/**
 * Fighter stats of a ship {@link Type}
 */
public class ShipFighters extends AttributeList {
    public ShipFighters(Type type) {
        super(
            "Fighters",
            type,
            false,
            new Entry[][]{{
                new Entry("Light Squadron Limit", 2217),
                new Entry("Support Squadron Limit", 2218, 0.0),
                new Entry("Heavy Squadron Limit", 2219, 0.0),
            }, {
                new Entry("Launch Tubes", 2216),
                new Entry("Hangar Capacity", 2055),
            }}
        );
    }
}
