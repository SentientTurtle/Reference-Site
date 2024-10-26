package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// Data object representing an individual bonus to an item's attributes
public final class TypeTraitBonus {
    public final double bonusAmount;
    public final String bonusText;
    public final @Nullable Integer unitID;

    public TypeTraitBonus(double bonusAmount, String bonusText, @Nullable Integer unitID) {
        this.bonusAmount = bonusAmount;
        this.bonusText = bonusText;
        this.unitID = unitID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TypeTraitBonus) obj;
        return Double.doubleToLongBits(this.bonusAmount) == Double.doubleToLongBits(that.bonusAmount) &&
               Objects.equals(this.bonusText, that.bonusText) &&
               Objects.equals(this.unitID, that.unitID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bonusAmount, bonusText, unitID);
    }

    @Override
    public String toString() {
        return "TypeTraitBonus[" +
               "bonusAmount=" + bonusAmount + ", " +
               "bonusText=" + bonusText + ", " +
               "unitID=" + unitID + ']';
    }
}
