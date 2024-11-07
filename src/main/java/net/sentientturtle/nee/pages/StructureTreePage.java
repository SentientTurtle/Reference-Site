package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.ItemTree;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.datatypes.Category;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.util.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;

/// Page containing the ship tree
public class StructureTreePage extends Page {
    @Override
    public String name() {
        return "Structures";
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
    public @Nullable ResourceLocation getIcon(HtmlContext context) {
        return null;
    }

    private static ItemTree.Entry entryFor(int categoryID, DataSupplier data) {
        Category category = data.getCategories().get(categoryID);
        ItemTree.Group[] groups = data.getCategoryGroups().get(categoryID)
            .stream()
            .filter(group -> data.getGroupTypes().containsKey(group.groupID))
            .map(group -> groupFor(group, data))
            .toArray(ItemTree.Group[]::new);

        return new ItemTree.Entry(
            category.name,
            groups
        );
    }

    private static ItemTree.Group groupFor(Group group, DataSupplier data) {
        Type[] types = data.getGroupTypes().get(group.groupID)
            .stream().sorted(Type.comparator(data))
            .toArray(Type[]::new);
        return new ItemTree.Group(group.name, types);
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        Map<Integer, Group> groups = context.data.getGroups();

        return new ItemTree(
            new ItemTree.Entry(
                "Structures",
                groupFor(groups.get(1657), context.data),
                groupFor(groups.get(1404), context.data),
                groupFor(groups.get(1406), context.data)
            ),
            new ItemTree.Entry(
                "FLEX Structures",
                groupFor(groups.get(1408), context.data),
                groupFor(groups.get(2017), context.data),
                groupFor(groups.get(2016), context.data),
                groupFor(groups.get(4744), context.data)
            ),
            entryFor(40, context.data),
            entryFor(22, context.data),
            new ItemTree.Entry(
                "Player Owned Starbase",
                groupFor(groups.get(365), context.data),    // Control tower
                groupFor(groups.get(1212), context.data),   // Personal hangar
                groupFor(groups.get(471), context.data),    // Corp hangar
                groupFor(groups.get(363), context.data),    // Ship Maintenance
                // POS Weaponry
                groupFor(groups.get(449), context.data),
                groupFor(groups.get(430), context.data),
                groupFor(groups.get(417), context.data),
                groupFor(groups.get(426), context.data),
                // POS Ewar
                groupFor(groups.get(439), context.data),
                groupFor(groups.get(440), context.data),
                groupFor(groups.get(441), context.data),
                groupFor(groups.get(443), context.data),
                groupFor(groups.get(837), context.data),
                // POS Shield hardening
                groupFor(groups.get(444), context.data)
            )
        );
    }
}
