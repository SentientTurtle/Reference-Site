package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.HeadEntries;
import net.sentientturtle.html.IndexSetting;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.ItemTree;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Category;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.Resource;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/// Page containing the ship tree
public class StructureTreePage extends Page {
    @Override
    public String name() {
        return "Structure Tree";
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return "structures";
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.ITEM_TREE;
    }

    @Override
    public IndexSetting getIndexSetting() {
        return IndexSetting.INDEX;
    }

    @Override
    public @Nullable Resource getIcon(HtmlContext context) {
        return null;
    }

    private static ItemTree.Entry entryFor(int categoryID, SDEData data) {
        Category category = data.getCategories().get(categoryID);
        ItemTree.Group[] groups = data.getCategoryGroupMap().get(categoryID)
            .stream()
            .filter(group -> data.getGroupTypeMap().containsKey(group.groupID))
            .map(group -> groupFor(group, data))
            .toArray(ItemTree.Group[]::new);

        return new ItemTree.Entry(
            category.name,
            groups
        );
    }

    private static ItemTree.Group groupFor(Group group, SDEData data) {
        Type[] types = data.getGroupTypeMap().get(group.groupID)
            .stream().sorted(Type.comparator(data))
            .toArray(Type[]::new);
        return new ItemTree.Group(group.name, types);
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        Map<Integer, Group> groups = context.sde.getGroups();

        return new ItemTree(
            new ItemTree.Entry(
                "Structures",
                groupFor(groups.get(1657), context.sde),
                groupFor(groups.get(1404), context.sde),
                groupFor(groups.get(1406), context.sde)
            ),
            new ItemTree.Entry(
                "FLEX Structures",
                groupFor(groups.get(1408), context.sde),
                groupFor(groups.get(2017), context.sde),
                groupFor(groups.get(2016), context.sde),
                groupFor(groups.get(4744), context.sde)
            ),
            entryFor(40, context.sde),
            entryFor(22, context.sde),
            new ItemTree.Entry(
                "Player Owned Starbase",
                groupFor(groups.get(365), context.sde),    // Control tower
                groupFor(groups.get(1212), context.sde),   // Personal hangar
                groupFor(groups.get(471), context.sde),    // Corp hangar
                groupFor(groups.get(363), context.sde),    // Ship Maintenance
                // POS Weaponry
                groupFor(groups.get(449), context.sde),
                groupFor(groups.get(430), context.sde),
                groupFor(groups.get(417), context.sde),
                groupFor(groups.get(426), context.sde),
                // POS Ewar
                groupFor(groups.get(439), context.sde),
                groupFor(groups.get(440), context.sde),
                groupFor(groups.get(441), context.sde),
                groupFor(groups.get(443), context.sde),
                groupFor(groups.get(837), context.sde),
                // POS Shield hardening
                groupFor(groups.get(444), context.sde)
            )
        );
    }
    @Override
    protected HeadEntries headEntries(HtmlContext context) {
        return super.headEntries(context).append(
            HTML.META().attribute("name", "description").attribute("content", "Structure and Deployable tree for EVE Online")
        );
    }
}
