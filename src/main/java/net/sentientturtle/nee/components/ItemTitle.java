package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.ResourceLocation;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static net.sentientturtle.html.HTML.IMG;

/**
 * Page title, with an optional icon
 */
public class ItemTitle extends Component {
    private final HTML text;
    private final ResourceLocation icon;

    public ItemTitle(@NonNull String text, @Nullable ResourceLocation icon) {
        super("item_title colour_theme_minor");
        this.text = HTML.TEXT(Objects.requireNonNull(text));
        this.icon = icon;
    }

    public ItemTitle(@NonNull HTML text, @Nullable ResourceLocation icon) {
        super("item_title colour_theme_minor");
        this.text = Objects.requireNonNull(text);
        this.icon = icon;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        if (icon != null) {
            return new HTML[]{
                IMG(this.icon, null, 64).className("item_title_icon"),
                HTML.TEXT_BOLD().content(this.text).className("font_header item_title_text")
            };
        } else {
            return new HTML[]{
                HTML.TEXT_BOLD().content(this.text).className("font_header item_title_text")
            };
        }
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
            }""";
    }
}
