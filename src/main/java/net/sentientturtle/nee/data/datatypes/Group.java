package net.sentientturtle.nee.data.datatypes;

import net.sentientturtle.html.Frame;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.page.GroupPage;
import net.sentientturtle.nee.page.HasPage;
import net.sentientturtle.nee.page.TypePage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

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

    private static final ResourceLocation UNLOADED_ICON = ResourceLocation.file("bookicon.png");
    private ResourceLocation fallbackIcon = UNLOADED_ICON;   // We need a ternary state of "not loaded", "null/no icon" and "icon"
    public synchronized ResourceLocation getIconWithFallback(HtmlContext context) {
        if (fallbackIcon == UNLOADED_ICON) {
            if (iconID != null && iconID != 0) {
                fallbackIcon = ResourceLocation.ofIconID(iconID, context);
            } else {
                Set<Type> types = context.sde.getGroupTypes().getOrDefault(groupID, Set.of());
                if (types.size() > 0) {
                    Type type = types.stream()
                        .min(Type.comparator(context.sde))
                        .get();

                    fallbackIcon = new TypePage(type).getIcon(context);
                } else {
                    fallbackIcon = null;
                }
            }
        }
        return fallbackIcon;
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
    public @NonNull Frame getPage() {
        return new GroupPage(this);
    }
}
