package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.pages.Page;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.sentientturtle.html.HTML.*;

/// List of multiple pages
public class CanBeFittedTo extends Component {
    private final Set<Integer> groups;
    private final Set<Integer> types;

    public CanBeFittedTo(Set<Integer> groups, Set<Integer> types) {
        super("cbft colour_theme_minor");
        this.groups = groups;
        this.types = types;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return Stream.concat(
            Stream.of(HEADER("font_header").text("Can be fitted to")),
            Stream.concat(
                    groups.stream().map(context.data.getGroups()::get),
                    types.stream().map(context.data.getTypes()::get)
                )
                .map(entry -> {
                        Page page = entry.getPage();
                        return DIV("cbft_entry").content(
                            (page.getIcon() != null) ? IMG(page.getIcon(), null, 32).className("cbft_icon") : DIV("cbft_icon"),
                            new PageLink(page).className("cbft_type font_header")
                        );
                    }
                )
        ).toArray(HTML[]::new);
    }

    @Override
    protected String getCSS() {
        return """
            .cbft {
                display: flex;
                flex-direction: column;
                padding: 0.5rem;
            }
            
            .cbft > header {
                margin-bottom: 0.5rem;
            }
            
            .cbft_entry {
                width: 100%;
                font-size: 1.25rem;
                display: flex;
                align-items: center;
            }
            
            .cbft_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .cbft_type {
                margin-inline-start: 0.5rem;
            }""";
    }
}
