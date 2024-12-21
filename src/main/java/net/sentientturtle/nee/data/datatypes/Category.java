package net.sentientturtle.nee.data.datatypes;


import net.sentientturtle.nee.page.CategoryPage;
import net.sentientturtle.html.Frame;
import net.sentientturtle.nee.page.HasPage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Data object representing EVE Online Type Categories
 */
public class Category implements HasPage {
    public final int categoryID;
    public String name;
    /**
     * May be left null to indicate this Category has no icon.
     */
    @Nullable
    public final Integer iconID;
    public boolean published;

    public Category(int categoryID, String name, @Nullable Integer iconID, boolean published) {
        this.categoryID = categoryID;
        this.name = name;
        this.iconID = iconID;
        this.published = published;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryID=" + categoryID +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Category category) {
            return categoryID == category.categoryID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return categoryID;
    }

    @Override
    public @NonNull Frame getPage() {
        return new CategoryPage(this);
    }
}
