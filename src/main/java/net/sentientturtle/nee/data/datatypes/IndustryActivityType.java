package net.sentientturtle.nee.data.datatypes;

/**
 * Data object to represent EVE Online Industry Activity types. (Such as Manufacturing, Research, or Invention)
 */
public class IndustryActivityType {
    public final int activityID;
    public final String activityName;
    public final boolean published;

    public IndustryActivityType(int activityID, String activityName, boolean published) {
        this.activityID = activityID;
        this.activityName = activityName;
        this.published = published;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof IndustryActivityType that) {
            return activityID == that.activityID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return activityID;
    }

    @Override
    public String toString() {
        return "IndustryActivityType{" +
                "activityID=" + activityID +
                ", activityName='" + activityName + '\'' +
                ", published=" + published +
                '}';
    }
}
