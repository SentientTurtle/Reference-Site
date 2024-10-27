package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.pages.CategoryPage;
import net.sentientturtle.nee.pages.GroupPage;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.datatypes.Category;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Type;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

import static net.sentientturtle.html.HTML.*;

/**
 * {@link Group} and {@link Category} of a {@link Type}
 */
public class TypeGroup extends Component {
    private final Type type;

    public TypeGroup(@NonNull Type type) {
        super("type_group colour_theme_minor");
        this.type = Objects.requireNonNull(type);
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Group group = context.data.getGroups().get(this.type.groupID);
        Category category = context.data.getCategories().get(group.categoryID);

        return new HTML[]{
            SPAN("type_group_span font_header").content(
                TEXT_BOLD().className("head_text type_group_text").content(new PageLink(new GroupPage(group))),
                SPAN("type_group_separator").text("|"),
                TEXT_BOLD().className("head_text type_group_text").content(new PageLink(new CategoryPage(category)))
            )
        };
    }

    @Override
    protected String getCSS() {
        return """
            .type_group, .type_group_span {
              min-height: 3rem;
              display: flex;
              align-items: center;
            }
            
            .type_group_span {
              margin-inline: 0.5rem;
            }
            
            .type_group_text, .type_category_text {
              font-size: 1.5rem;
            }
            
            .type_group_separator {
              font-size: 2rem;
              width: 1.5rem;
              text-align: center;
            }""";
    }
}
