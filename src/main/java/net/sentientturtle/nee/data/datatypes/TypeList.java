package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

import java.util.Set;

public record TypeList(
    int typeListID,
    Set<Type> types,
    @Nullable String displayName,
    @Nullable String displayDescription
) { }
