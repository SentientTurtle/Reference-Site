package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.TypeList;
import net.sentientturtle.nee.page.GroupPage;
import net.sentientturtle.nee.page.TypePage;
import net.sentientturtle.nee.data.Resource;
import net.sentientturtle.nee.data.datatypes.Category;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_SET;
import static net.sentientturtle.html.HTML.*;

/**
 * List of all {@link Group} in a {@link Category}, or all {@link Type} in a {@link Group}, or all {@link Type} in a {@link TypeList}
 */
public class SimpleList extends Component {
    private final Object list;

    public SimpleList(@NonNull Category category) {
        super("simple_list");
        this.list = category;
    }

    public SimpleList(@NonNull Group group) {
        super("simple_list");
        this.list = group;
    }

    public SimpleList(@NonNull TypeList typeList) {
        super("simple_list");
        this.list = typeList;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected HTML[] getContent(HtmlContext context) {
        String listKind;
        Stream<Element> entries;
        switch (list) {
            case Category category:
                listKind = "Item groups";
                entries = context.sde.getCategoryGroupMap()
                    .getOrDefault(category.categoryID, (Set<Group>) EMPTY_SET)
                    .stream()
                    .sorted(Comparator.comparingInt(g -> g.groupID))
                    .map(group -> {
                        Resource icon = group.getIconWithFallback(context);
                        return DIV("simple_list_entry")
                            .content(
                                icon != null ? IMG(icon, null, 64).className("simple_list_icon") : DIV("simple_list_icon"),
                                SPAN("font_header")
                                    .content(new PageLink(new GroupPage(group)))
                            );
                    });
                break;
            case Group group:
                listKind = "Items";
                entries = context.sde.getGroupTypeMap()
                    .getOrDefault(group.groupID, (Set<Type>) EMPTY_SET)
                    .stream()
                    .sorted(Type.comparator(context.sde))
                    .map(type ->
                        DIV("simple_list_entry")
                            .content(
                                IMG(Resource.typeIcon(type.typeID, context), null, 64).className("simple_list_icon"),
                                SPAN("font_header")
                                    .content(new PageLink(new TypePage(type)))
                            )
                    );
                break;
            case TypeList list:
                listKind = "Items";
                entries = list.types()
                    .stream()
                    .sorted(Type.comparator(context.sde))
                    .map(type ->
                        DIV("simple_list_entry")
                            .content(
                                IMG(Resource.typeIcon(type.typeID, context), null, 64).className("simple_list_icon"),
                                SPAN("font_header")
                                    .content(new PageLink(new TypePage(type)))
                            )
                    );
                break;
            default:
                throw new RuntimeException("Unknown list type in SimpleList: " + list);
        }

        return new HTML[]{
            DIV("simple_list_title")
                .content(HEADER("font_header").text(listKind)),
            DIV("simple_list")
                .content(entries)
        };
    }

    @Override
    protected String getCSS() {
        return """
            .simple_list {
                display: flex;
                flex-direction: column;
                padding: 1rem;
            }
            
            .simple_list_title {
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 2rem;
            }
            
            .simple_list_icon {
                height: 4rem;
                width: 4rem;
            }
            
            .simple_list_entry {
                font-size: 1.5rem;
                display: flex;
                align-items: center;
                gap: 0.5rem;
            }""";
    }
}
