package net.sentientturtle.nee.data.datatypes;

import java.util.Objects;

public class Effect {
    public final int effectID;
    public final String effectName;

    public Effect(int effectID, String effectName) {
        this.effectID = effectID;
        this.effectName = effectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Effect effect)) return false;
        return effectID == effect.effectID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(effectID);
    }
}
