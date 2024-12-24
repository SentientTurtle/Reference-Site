package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record TypeTraits(
    @NonNull List<Bonus> miscBonuses,
    @NonNull List<Bonus> roleBonuses,
    @NonNull Map<Integer, List<Bonus>> skillBonuses
) {

    /// Data object representing an individual bonus to an item's attributes
    public record Bonus(@Nullable Double bonusAmount, String bonusText, @Nullable Integer unitID) { }
}
