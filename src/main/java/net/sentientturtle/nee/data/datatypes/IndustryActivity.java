package net.sentientturtle.nee.data.datatypes;

import java.util.Map;
import java.util.Objects;

/**
 * Data object to represent EVE Online Industry Activities
 */
public final class IndustryActivity {
    public final int bpTypeID;
    public final IndustryActivityType activityType;
    public final int time;
    public final Map<Integer, Integer> materialMap;
    public final Map<Integer, Integer> productMap;
    public final Map<Integer, Double> probabilityMap;
    public final Map<Integer, Integer> skillMap;

    /**
     *
     */
    public IndustryActivity(
        int bpTypeID,
        IndustryActivityType activityType,
        int time,
        Map<Integer, Integer> materialMap,
        Map<Integer, Integer> productMap,
        Map<Integer, Double> probabilityMap,
        Map<Integer, Integer> skillMap
    ) {
        this.bpTypeID = bpTypeID;
        this.activityType = activityType;
        this.time = time;
        this.materialMap = materialMap;
        this.productMap = productMap;
        this.probabilityMap = probabilityMap;
        this.skillMap = skillMap;
    }

    @Override
    public String toString() {
        return "IndustryActivity{" +
               "bpTypeID=" + bpTypeID +
               ", activity=" + activityType +
               '}';
    }

    // Override equality to match only BP and activity kind, we don't need to compare inputs/outputs
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof IndustryActivity that) {
            return bpTypeID == that.bpTypeID &&
                   activityType == that.activityType;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(bpTypeID, activityType.hashCode());
    }

}
