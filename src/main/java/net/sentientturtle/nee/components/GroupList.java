package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.page.GroupPage;
import net.sentientturtle.nee.page.TypePage;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Category;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_SET;
import static net.sentientturtle.html.HTML.*;

/**
 * List of all {@link Group} in a {@link Category}, or all {@link Type} in a {@link Group}
 */
public class GroupList extends Component {
    private final @Nullable Category category;
    private final @Nullable Group group;

    public GroupList(@NonNull Category category) {
        super("group_list");
        this.category = category;
        this.group = null;
    }

    public GroupList(@NonNull Group group) {
        super("group_list");
        this.category = null;
        this.group = group;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected HTML[] getContent(HtmlContext context) {
        Stream<Element> entries;
        if (category != null && group == null) {
            entries = context.sde.getCategoryGroups()
                .getOrDefault(category.categoryID, (Set<Group>) EMPTY_SET)
                .stream()
                .sorted(Comparator.comparingInt(g -> g.groupID))
                .map(group -> {
                    ResourceLocation icon = group.getIconWithFallback(context);
                    return DIV("group_list_entry")
                        .content(
                            icon != null ? IMG(icon, null, 64).className("group_list_icon") : DIV("group_list_icon"),
                            SPAN("font_header")
                                .content(new PageLink(new GroupPage(group)))
                        );
                });
        } else if (category == null && group != null) {
            entries = context.sde.getGroupTypes()
                .getOrDefault(group.groupID, (Set<Type>) EMPTY_SET)
                .stream()
                .sorted(Type.comparator(context.sde))
                .map(type ->
                    DIV("group_list_entry")
                        .content(
                            IMG(ResourceLocation.typeIcon(type.typeID, context), null, 64).className("group_list_icon"),
                            SPAN("font_header")
                                .content(new PageLink(new TypePage(type)))
                        )
                );
        } else {
            throw new IllegalStateException("Either one or category or group must be non-null");
        }

        return new HTML[]{
            DIV("group_list_title")
                .content(HEADER("font_header").text(category != null ? "Item groups" : "Items")),
            DIV("group_list")
                .content(entries)
        };
    }

    @Override
    protected String getCSS() {
        return """
            .group_list {
                display: flex;
                flex-direction: column;
                padding: 1rem;
            }
            
            .group_list_title {
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 2rem;
            }
            
            .group_list_icon {
                height: 4rem;
                width: 4rem;
            }
            
            .group_list_entry {
                font-size: 1.5rem;
                display: flex;
                align-items: center;
                gap: 0.5rem;
            }""";
    }
}
