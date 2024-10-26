package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.components.GroupList;
import net.sentientturtle.nee.components.Title;
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
    public String filename() {
        return category.categoryID + "-" + name();
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return DIV("category_page_grid").content(
            new Title(category.name, getIcon()),
            new GroupList(category)
        );
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.CATEGORY;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon() {
        return category.iconID != null ? ResourceLocation.iconOfIconID(category.iconID) : null;
    }

}
