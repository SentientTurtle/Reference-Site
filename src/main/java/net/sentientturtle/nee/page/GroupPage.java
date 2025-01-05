package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.components.GroupList;
import net.sentientturtle.nee.components.ItemTitle;
import net.sentientturtle.nee.data.datatypes.Group;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.DIV;

/**
 * Page for a {@link Group}
 */
public class GroupPage extends Page {
    public final Group group;

    public GroupPage(Group group) {
        this.group = group;
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return group.groupID + "-" + name();
    }

    @Override
    public String name() {
        return group.name;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return DIV("group_page_grid").content(
            new ItemTitle(group.name, getIcon(context)),
            new GroupList(group)
        );
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.GROUP;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return group.getIconWithFallback(context);
    }

}
