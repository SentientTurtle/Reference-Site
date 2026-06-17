package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.Resource;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static net.sentientturtle.html.HTML.IMG;

/**
 * Page title, with an optional icon
 */
public class ItemTitle extends Component {
    private final HTML text;
    private final Resource icon;

    public ItemTitle(@NonNull String text, @Nullable Resource icon) {
        super("item_title colour_theme_minor");
        this.text = HTML.TEXT(Objects.requireNonNull(text));
        this.icon = icon;
    }

    public ItemTitle(@NonNull HTML text, @Nullable Resource icon) {
        super("item_title colour_theme_minor");
        this.text = Objects.requireNonNull(text);
        this.icon = icon;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        return new HTML[]{
            this.icon != null ? IMG(this.icon, null, 64).className("item_title_icon") : HTML.empty(),
            HTML.HN(1, "font_header item_title_text").content(this.text)
        };
    }

    @Override
    protected String getCSS() {
        return """
            .item_title {
                min-height: 4rem;
                display: flex;
                align-items: center;
            }
            
            .item_title:has(> *:only-child) {
                justify-content: center;
            }
            
            .item_title_icon {
                width: 4rem;
                height: 4rem;
            }
            
            .item_title_text {
                font-size: 1.5rem;
                margin-inline: 0.5rem;
                margin-block: 0;
            }""";
    }
}
