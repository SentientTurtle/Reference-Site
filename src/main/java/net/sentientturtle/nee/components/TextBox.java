package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.HEADER;
import static net.sentientturtle.html.HTML.SPAN;

public class TextBox extends Component {
    private final @Nullable String title;
    private final HTML content;

    public TextBox(@Nullable String title, HTML content) {
        super("text_box colour_theme_minor");
        this.title = title;
        this.content = content;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            this.title != null ? HEADER("font_header text_box_header").text(this.title) : HTML.empty(),
            SPAN("text_box_text font_text").content(this.content)
        };
    }

    @Override
    protected String getCSS() {
        return """
            .text_box {
                display: flex;
                flex-direction: column;
                padding: 1rem;
            }
            
            .text_box_header {
                font-size: 1.5rem;
            }
            
            .text_box_text {
                font-style: italic;
                font-size: 0.9rem;
            }""";
    }
}
