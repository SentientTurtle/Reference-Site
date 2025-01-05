package net.sentientturtle.nee.components;

import net.sentientturtle.nee.data.datatypes.Type;

/**
 * Fighter stats of a structure {@link Type}
 *
 * Specialization of {@link ShipFighters} for Structures, which use "Standup" fighters as opposed to the regular fighters used by capital ships; Each have different attributes
 */
public class ShipFightersStandup extends AttributeList {
    public ShipFightersStandup(Type type) {
        super(
            "Standup Fighters",
            type,
            new Entry[][]{{
                new Entry.Attribute("Light Squadron Limit", 2737),
                new Entry.Attribute("Support Squadron Limit", 2738),
                new Entry.Attribute("Heavy Squadron Limit", 2739),
            }, {
                new Entry.Attribute("Launch Tubes", 2216),
                new Entry.Attribute("Hangar Capacity", 2055),
            }}
        );
    }
}
