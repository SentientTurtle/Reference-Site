package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.page.Page;
import net.sentientturtle.nee.data.ResourceLocation;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static net.sentientturtle.html.HTML.*;

/// List of multiple pages
public class UsedWith extends Component {
    private final String title;
    private final Set<Integer> groups;
    private final Set<Integer> types;

    public UsedWith(String title, Set<Integer> groups, Set<Integer> types) {
        super("used_with colour_theme_minor");
        this.title = title;
        this.groups = groups;
        this.types = types;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return Stream.concat(
            Stream.of(HEADER("font_header").text(this.title)),
            Stream.concat(
                    groups.stream().map(context.sde.getGroups()::get)
                        .filter(Objects::nonNull),// Can be fitted to / Used with attributes may specify invalid groups
                    types.stream().map(context.sde.getTypes()::get)
                        .sorted(Type.comparator(context.sde))
                )
                .map(entry -> {
                    Page page = entry.getPage();
                    ResourceLocation icon = page.getIcon(context);
                    return DIV("used_with_entry").content(
                        (icon != null) ? IMG(icon, null, 32).className("used_with_icon") : DIV("used_with_icon"),
                        new PageLink(page).className("used_with_type font_header")
                    );
                })
        ).toArray(HTML[]::new);
    }

    @Override
    protected String getCSS() {
        return """
            .used_with {
                display: flex;
                flex-direction: column;
                padding: 0.5rem;
            }
            
            .used_with > header {
                margin-bottom: 0.5rem;
            }
            
            .used_with_entry {
                width: 100%;
                display: flex;
                align-items: center;
            }
            
            .used_with_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .used_with_type {
                margin-inline-start: 0.5rem;
            }""";
    }
}
