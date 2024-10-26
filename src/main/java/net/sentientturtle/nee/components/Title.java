package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.util.ResourceLocation;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static net.sentientturtle.html.HTML.IMG;

/**
 * Page title, with an optional icon
 */
public class Title extends Component {
    private final String text;
    private final ResourceLocation icon;

    public Title(@NonNull String text, @Nullable ResourceLocation icon) {
        super("title colour_theme_minor");
        this.text = Objects.requireNonNull(text);
        this.icon = icon;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        if (icon != null) {
            return new HTML[]{
                IMG(this.icon, null, 64),
                HTML.TEXT_BOLD(this.text).className("font_header title_text")
            };
        } else {
            return new HTML[]{
                HTML.TEXT_BOLD(this.text).className("font_header title_text")
            };
        }
    }

    @Override
    protected String getCSS() {
        return """
            .title {
                min-height: 64px;
                display: flex;
                align-items: center;
            }
            
            .title_text {
                font-size: 1.5em;
                margin-inline: 10px;
            }""";
    }
}
