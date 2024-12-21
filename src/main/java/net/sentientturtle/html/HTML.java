package net.sentientturtle.html;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.context.ID;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.util.ExceptionUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/// Top level interface for HTML elements
///
/// Also serves as container for common HTML element factory methods
///
/// All (direct) implementations should override toString() and direct users to {@link #renderTo(HtmlContext)}
public sealed interface HTML permits Element, HTML.EmptyHTML, HTML.MultiHTML, HTML.RawHTML, HTML.RepeatHTML, HTML.TextHTML, Frame {
    /// Empty HTML object, equivalent to a zero-length string in the generated HTML
    static HTML empty() {
        return new EmptyHTML();
    }

    /// Multiple sequential HTML items, escape hatch
    static HTML multi(HTML... items) {
        if (items.length == 0) return HTML.empty();
        return new MultiHTML(items);
    }

    /// Repeats the given HTML item {@code count} times
    static HTML repeat(int count, HTML item) {
        if (count < 0) throw new IllegalArgumentException("HTML Repeat count must be >= 0");
        if (count == 0) return empty();
        return new RepeatHTML(count, item);
    }

    /// Escape hatch for raw html
    ///
    /// CAUTION: HTML String must be escaped and valid!
    static HTML RAW(String html) {
        return new RawHTML(html);
    }

    /// Creates a new document root, returning the {@code <html>} element
    static Element DOCUMENT_ROOT() {
        return new Element.Html5Root("EN");
    }

    /// Wraps {@code textContent} as an HTML object such that it can be set as content for other Elements
    /// Provides HTML text escaping
    static HTML TEXT(@NonNull String textContent) {
        Objects.requireNonNull(textContent);
        return new TextHTML(textContent);
    }

    /// {@code <b></b>}
    static Element TEXT_BOLD() {
        return new Element("b");
    }

    /// {@code <b>[textContent]</b>}
    ///
    /// Equivalent to {@code TEXT_BOLD().content(TEXT(textContent));}
    static Element TEXT_BOLD(String textContent) {
        return new Element("b")
                .text(textContent);
    }

    /// {@code <a href='[href]'>[content]</a>}
    static Element A(String href, HTML content) {
        return new Element("a")
                .attribute("href", href)
                .content(content);
    }

    /// {@code <body></body>}
    static Element BODY() {
        return new Element("body");
    }

    /// {@code <br>}; void element
    static Element BR() {
        return new Element("br", true);
    }

    /// {@code <button class='[className]' id='[id]'>[text]</button>}
    static Element BUTTON(String classname, ID id, String text) {
        return new Element("button")
                .className(classname)
                .text(text)
                .id(id);
    }

    /// {@code <button class='[className]' id='[id]'></button>}
    static Element BUTTON(String classname, ID id) {
        return new Element("button")
                .className(classname)
                .id(id);
    }

    /// {@code <div></div>}
    static Element DIV() {
        return new Element("div");
    }
    /// {@code <div class='[className]'></div>}
    static Element DIV(String className) {
        return new Element("div")
                .className(className);
    }
    /// {@code <div class='[className]' id='[id]'></div>}
    static Element DIV(String className, ID id) {
        return new Element("div")
                .className(className)
                .id(id);
    }

    /// {@code <head></head>}
    static Element HEAD() {
        return new Element("head");
    }

    /// {@code <header></header>}
    static Element HEADER() {
        return new Element("header");
    }

    /// {@code <header class='[className]'></header>}
    static Element HEADER(String className) {
        return new Element("header")
            .className(className);
    }

    /// {@code <img>}
    ///
    /// 'Placeholder' img tag
    static Element IMG() {
        return new Element("img", true);
    }

    /// {@code <img src='[src]' alt='[alt]'>}
    ///
    /// Alt nullable; Icons paired right next to the text name can leave this null
    static Element IMG(ResourceLocation src, @Nullable String alt) {
        return new Element("img", true)
                .attribute("src", src::getURI)
                .attribute("alt", alt != null ? alt : "");
    }

    /// {@code <img src='[src]' alt='[alt]' width='[size]' height='[size]'>}
    ///
    /// {@link #IMG(ResourceLocation, String, int, int)} override for square images
    static Element IMG(ResourceLocation src, @Nullable String alt, int size) {
        return IMG(src, alt, size, size);
    }

    /// {@code <img src='[src]' alt='[alt]' width='[width]' height='[height]'>}
    ///
    /// Alt nullable; Icons paired right next to the text name can leave this null
    static Element IMG(ResourceLocation src, @Nullable String alt, int width, int height) {
        return new Element("img", true)
                .attribute("src", src::getURI)
                .attribute("width", String.valueOf(width))
                .attribute("height", String.valueOf(height))
                .attribute("alt", alt != null ? alt : "");
    }

    /// {@code <link> }
    static Element LINK() {
        return new Element("link", true);
    }

    /// {@code <meta> }
    static Element META() {
        return new Element("meta", true);
    }

    /// {@code <script type='module' src='[location]'></script> }
    static Element SCRIPT_EXTERNAL(ResourceLocation location) {
        return new Element("script")
            .attribute("type", "module")
            .attribute("src", location::getURI);
    }

    /// {@code <script type='module'>[scriptContent]</script> }
    static Element SCRIPT_MODULE(String scriptContent) {
        return new Element("script")
            .attribute("type", "module")
            .content(HTML.RAW(scriptContent));
    }

    record ImportMap(Map<String, String> imports) {}

    /// {@code <script type='importMap'>[importMap]</script> }
    static Element SCRIPT_IMPORTMAP(ImportMap importMap) {
        try {
            return new Element("script")
                .attribute("type", "importmap")
                .content(HTML.RAW(new ObjectMapper().writeValueAsString(importMap)));
        } catch (JsonProcessingException e) {
            return ExceptionUtil.sneakyThrow(e);
        }
    }

    /// {@code <span></span> }
    static Element SPAN() {
        return new Element("span");
    }

    /// {@code <span class='[className]'></span> }
    static Element SPAN(String className) {
        return new Element("span")
                .className(className);
    }

    /// {@code <table></table> }
    static Element TABLE() {
        return new Element("table");
    }

    /// {@code <table class='[className]'></table> }
    static Element TABLE(String className) {
        return new Element("table")
            .className(className);
    }

    /// {@code <td></td> }
    static Element TD() {
        return new Element("td");
    }
    /// {@code <td class='[className]'></td> }
    static Element TD(String className) {
        return new Element("td").className(className);
    }

    /// {@code <th></th> }
    static Element TH() {
        return new Element("th");
    }
    /// {@code <th class='[className]'></th> }
    static Element TH(String className) {
        return new Element("th").className(className);
    }

    /// {@code <tr></tr> }
    static Element TR() {
        return new Element("tr");
    }
    /// {@code <tr class='[className]'></tr> }
    static Element TR(String className) {
        return new Element("tr").className(className);
    }

    /// {@code <title>[title]</title> }
    static HTML TITLE(String title) {
        return new Element("title")
                .text(title);
    }

    /// {@code <ul></ul> }
    static Element UL() {
        return new Element("ul");
    }
    /// {@code <li></li> }
    static Element LI() {
        return new Element("li");
    }


    // Methods

    /// Render this HTML item
    void renderTo(HtmlContext context) throws RenderingException, IOException;

    /// Class for zero-length "non-item"
    final class EmptyHTML implements HTML {
        @Override
        public String toString() {
            throw new UnsupportedOperationException("Elements do not support toString; use HTML#renderTo instead");
        }

        @Override
        public void renderTo(HtmlContext context) {
            // Intentionally nothing
        }
    }

    // Single-purpose implementations, not inlined so that HTML can be sealed
    final class MultiHTML implements HTML {
        private final HTML[] items;

        public MultiHTML(HTML... items) {
            this.items = items;
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("Elements do not support toString; use HTML#renderTo instead");
        }

        @Override
        public void renderTo(HtmlContext context) throws RenderingException, IOException {
            for (HTML items : items) {
                items.renderTo(context);
            }
        }
    }

    final class RepeatHTML implements HTML {
        private final int count;
        private final HTML item;

        public RepeatHTML(int count, HTML item) {
            this.count = count;
            this.item = item;
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("Elements do not support toString; use HTML#renderTo instead");
        }

        @Override
        public void renderTo(HtmlContext context) throws RenderingException, IOException {
            for (int i = 0; i < count; i++) {
                item.renderTo(context);
            }
        }
    }

    final class RawHTML implements HTML {
        private final String html;

        public RawHTML(String html) {
            this.html = html;
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("Elements do not support toString; use HTML#renderTo instead");
        }

        @Override
        public void renderTo(HtmlContext ctx) throws IOException {
            ctx.write(html);
        }
    }

    final class TextHTML implements HTML {
        private final @NonNull String textContent;

        public TextHTML(@NonNull String textContent) {
            this.textContent = textContent;
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("Elements do not support toString; use HTML#renderTo instead");
        }

        @Override
        public void renderTo(HtmlContext ctx) throws IOException {
            ctx.write(HTMLUtil.escapeText(textContent));
        }
    }
}
