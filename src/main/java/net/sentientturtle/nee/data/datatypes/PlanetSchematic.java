package net.sentientturtle.nee.data.datatypes;

import java.util.Map;
import java.util.Objects;

public class PlanetSchematic {
    public final int schematicID;
    public final int cycleTime;
    public final int outputTypeID;
    public final int outputQuantity;
    public final Map<Integer, Integer> inputs;

    public PlanetSchematic(int schematicID, int cycleTime, int outputTypeID, int outputQuantity, Map<Integer, Integer> inputs) {
        this.schematicID = schematicID;
        this.cycleTime = cycleTime;
        this.outputTypeID = outputTypeID;
        this.outputQuantity = outputQuantity;
        this.inputs = inputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanetSchematic that)) return false;
        return schematicID == that.schematicID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(schematicID);
    }
}
