package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.nee.pages.GroupPage;
import net.sentientturtle.nee.pages.HasPage;
import net.sentientturtle.nee.pages.Page;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Data object to represent EVE Online Type Groups
 */
public class Group implements HasPage {
    public final int groupID;
    public final int categoryID;
    public final String name;
    /**
     * May be left null to indicate this group has no icon
     */
    @Nullable
    public final Integer iconID;
    public boolean published;

    public Group(int groupID, int categoryID, String name, @Nullable Integer iconID, boolean published) {
        this.groupID = groupID;
        this.categoryID = categoryID;
        this.name = name;
        this.iconID = iconID;
        this.published = published;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupID=" + groupID +
                ", categoryID=" + categoryID +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Group group) {
            return groupID == group.groupID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return groupID;
    }

    @Override
    public @NonNull Page getPage() {
        return new GroupPage(this);
    }
}
