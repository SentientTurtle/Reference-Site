package net.sentientturtle.html;

import net.sentientturtle.nee.page.TypePage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;


/**
 * Specialized {@code <a>} type for links to other pages
 */
public class PageLink extends Element {
    public PageLink(Document document) {
        this(document, (String) null);
    }

    public PageLink(Document document, @Nullable String text) {
        super("a");
        this.attribute("href", context -> context.pathTo(document));
        this.text(text == null ? document.name() : text);
    }

    public PageLink(Document document, @NonNull HTML content) {
        super("a");
        this.attribute("href", context -> context.pathTo(document));
        this.content(content);
    }

    public PageLink(String absolutePath, @NonNull String text) {
        super("a");
        this.attribute("href", context -> context.pathTo(absolutePath));
        this.text(Objects.requireNonNull(text));
    }
}
