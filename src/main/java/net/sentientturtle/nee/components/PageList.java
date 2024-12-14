package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.page.Frame;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.ResourceLocation;

import java.util.Arrays;

import static net.sentientturtle.html.HTML.*;

/// List of multiple pages
public class PageList extends Component {
    private final Frame[] targetPages;
    private final String groupName;

    public PageList(String groupName, Frame... targetPages) {
        super("page_list colour_theme_minor");
        this.groupName = groupName;
        this.targetPages = targetPages;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            SPAN("page_list_title font_header").text(groupName),
            DIV("page_list_list font_text").content(
                Arrays.stream(targetPages).map(page -> {
                    ResourceLocation icon = page.getIcon(context);
                    return DIV("page_list_entry").content(
                        (icon != null) ? IMG(icon, null, 32).className("page_list_icon") : DIV("page_list_icon"),
                        new PageLink(page).className("page_list_type")
                    );
                })
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
                font-size: 1.75rem;
                margin-left: 1rem;
            }
            
            .page_list_entry {
                display: flex;
                height: 2rem;
            }
            
            .page_list_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .page_list_type {
                font-size: 1.25rem;
                margin: 0.5rem;
            }""";
    }
}
