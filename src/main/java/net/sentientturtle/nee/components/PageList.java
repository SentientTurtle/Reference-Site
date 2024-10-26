package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.pages.Page;
import net.sentientturtle.html.PageLink;

import java.util.Arrays;

import static net.sentientturtle.html.HTML.*;

/// List of multiple pages
public class PageList extends Component {
    private final Page[] targetPages;
    private final String groupName;

    public PageList(String groupName, Page... targetPages) {
        super("page_list colour_theme_minor");
        this.groupName = groupName;
        this.targetPages = targetPages;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            SPAN("page_list_title font_header").text(groupName),
            DIV("page_list_list font_text").content(
                Arrays.stream(targetPages).map(page ->
                    DIV("page_list_entry").content(
                        (page.getIcon() != null) ? IMG(page.getIcon(), null, 32) : DIV().attribute("style", "width: 32px;"),
                        new PageLink(page).className("page_list_type")
                    )
                )
            )
        };
    }

    @Override
    protected String getCSS() {
        return """
            .page_list {
                padding: 0.5rem;
            }
            
            .page_list_title {
                font-size: 1.75em;
                margin-left: 10px;
            }
            
            .page_list_list {
            
            }
            
            .page_list_entry {
                display: flex;
                height: 32px;
            }
            
            .page_list_type {
                font-size: 1.25em;
                margin: 5px;
            }""";
    }
}
