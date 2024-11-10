package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.nee.pages.HasPage;
import net.sentientturtle.nee.pages.MarketGroupPage;
import net.sentientturtle.nee.pages.Page;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Data object to represent EVE Online Market Groups
 */
public class MarketGroup implements HasPage {
    public final int marketGroupID;
    public @Nullable Integer parentGroupID;
    public final String name;
    public final @Nullable String description;

    public MarketGroup(int marketGroupID, @Nullable Integer parentGroupID, String name, @Nullable String description) {
        this.marketGroupID = marketGroupID;
        this.parentGroupID = parentGroupID;
        this.name = name;
        this.description = description;
    }

    @Override
    public @NonNull Page getPage() {
        return new MarketGroupPage(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketGroup that)) return false;
        return marketGroupID == that.marketGroupID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(marketGroupID);
    }
}
