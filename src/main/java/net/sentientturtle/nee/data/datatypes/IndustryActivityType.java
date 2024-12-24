package net.sentientturtle.nee.data.datatypes;

/**
 * Data object to represent EVE Online Industry Activity types. (Such as Manufacturing, Research, or Invention)
 */
public enum IndustryActivityType {
    MANUFACTURING(1, "Manufacturing"),
    RESEARCH_TIME(3, "Researching Time Efficiency"),
    RESEARCH_MATERIAL(4, "Researching Material Efficiency"),
    COPYING(5, "Copying"),
    INVENTION(8, "Invention"),
    REACTIONS(11, "Reactions");
    public final int activityID;
    public final String activityName;

    IndustryActivityType(int activityID, String activityName) {
        this.activityID = activityID;
        this.activityName = activityName;
    }
}
