package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// Data object representing an individual bonus to an item's attributes
public final class TypeTraitBonus {
    public final @Nullable Double bonusAmount;
    public final String bonusText;
    public final @Nullable Integer unitID;

    public TypeTraitBonus(@Nullable Double bonusAmount, String bonusText, @Nullable Integer unitID) {
        this.bonusAmount = bonusAmount;
        this.bonusText = bonusText;
        this.unitID = unitID;
    }

    @Override
    public String toString() {
        return "TypeTraitBonus[" +
               "bonusAmount=" + bonusAmount + ", " +
               "bonusText=" + bonusText + ", " +
               "unitID=" + unitID + ']';
    }
}
