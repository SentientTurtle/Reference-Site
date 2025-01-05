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
            new Entry[][]{{
                new Entry.Attribute("Light Squadron Limit", 2217),
                new Entry.AttributeWithDefault("Support Squadron Limit", 2218, 0.0),
                new Entry.AttributeWithDefault("Heavy Squadron Limit", 2219, 0.0),
            }, {
                new Entry.Attribute("Launch Tubes", 2216),
                new Entry.Attribute("Hangar Capacity", 2055),
            }}
        );
    }
}
