package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.components.GroupList;
import net.sentientturtle.nee.components.Title;
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
            new Title(group.name, getIcon(context)),
            new GroupList(group)
        );
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.GROUP;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon(HtmlContext context) { // TODO: Replace with icon of first type in group
        return group.iconID != null ? ResourceLocation.iconOfIconID(group.iconID, context) : null;
    }

}
