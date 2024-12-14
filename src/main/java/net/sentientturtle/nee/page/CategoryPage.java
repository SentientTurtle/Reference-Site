package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.components.GroupList;
import net.sentientturtle.nee.components.ItemTitle;
import net.sentientturtle.nee.data.datatypes.Category;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.DIV;

/**
 * Page for a {@link Category}
 */
public class CategoryPage extends Page {
    public final Category category;

    public CategoryPage(Category category) {
        this.category = category;
    }

    @Override
    public String name() {
        return category.name;
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return category.categoryID + "-" + name();
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return DIV("category_page_grid").content(
            new ItemTitle(category.name, getIcon(context)),
            new GroupList(category)
        );
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.CATEGORY;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return category.iconID != null ? ResourceLocation.ofIconID(category.iconID, context) : null;
    }

}
