package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;


import static net.sentientturtle.html.HTML.*;

/**
 * Item description text block
 */
public class ItemDescription extends Component {
    private final HTML[] content;

    public ItemDescription(HTML... content) {
        super("item_description colour_theme_minor");
        this.content = content;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            HEADER("font_header").text("Description"),
            DIV("html_text item_description_text font_text").content(content)
        };
    }

    @Override
    protected String getCSS() {
        return """
            .item_description {
              padding: 0.5rem;
            }
            
            .item_description_text {
              padding: 0.5rem;
              font-style: italic;
              font-size: 0.9em;
            }""";
    }
}
