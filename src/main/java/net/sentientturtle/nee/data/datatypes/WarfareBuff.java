package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

public record WarfareBuff(
    @Nullable String displayName,
    ShowOutputValue showOutputValue
) {
    public enum ShowOutputValue {
        SHOW_NORMAL,
        SHOW_INVERTED,
        HIDE
    }
}
