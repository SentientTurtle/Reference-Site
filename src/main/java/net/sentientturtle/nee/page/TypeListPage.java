package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.HeadEntries;
import net.sentientturtle.html.IndexSetting;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.ItemDescription;
import net.sentientturtle.nee.components.SimpleList;
import net.sentientturtle.nee.components.ItemTitle;
import net.sentientturtle.nee.data.Resource;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.TypeList;
import net.sentientturtle.nee.util.EVEText;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.DIV;

/**
 * Page for a {@link Group}
 */
public class TypeListPage extends Page {
    public final TypeList typeList;

    public TypeListPage(TypeList typeList) {
        this.typeList = typeList;
        if (typeList.displayName() == null) throw new IllegalArgumentException("Attempt to create page for TypeList without display name: #" + typeList.typeListID());
    }

    @Override
    public @Nullable String description() {
        return typeList.displayDescription();
    }

    @Override
    public String filename() {
        return typeList.typeListID() + "-" + name();
    }

    @Override
    public String name() {
        return typeList.displayName();
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return DIV("typelist_page_grid").content(
            new ItemTitle(typeList.displayName(), getIcon(context)),
            typeList.displayDescription() != null ? new ItemDescription(EVEText.escape(typeList.displayDescription(), context.sde, false)) : HTML.empty(),
            new SimpleList(typeList)
        );
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.TYPELIST;
    }

    @Override
    public IndexSetting getIndexSetting() {
        // Disabled indexing on "list" pages; No notable information, probably won't be searched for directly.
        return IndexSetting.NO_INDEX;
    }

    @Nullable
    @Override
    public Resource getIcon(HtmlContext context) {
        return new TypePage(typeList.types().iterator().next()).getIcon(context);
    }

    @Override
    protected HeadEntries headEntries(HtmlContext context) {
        return super.headEntries(context).append(
            HTML.META().attribute("name", "description").attribute("content", "TypeList: " + typeList.displayName())
        );
    }
}
