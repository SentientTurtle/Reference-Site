package net.sentientturtle.html;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.id.ID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

/// Top level class for HTML elements
///
/// Common element types are instantiated through factory methods in {@link HTML}, such as {@link HTML#DIV()}
///
/// Subclasses may instantiate this type directly
public class Element implements HTML {
    private final String tagName;
    private final boolean isVoid;
    private String className;
    private final HashMap<String, @Nullable HTMLAttribute> attributes;
    protected ArrayList<HTML> content;

    protected Element(@NonNull String tagName) {
        this(tagName, false);
    }

    protected Element(@NonNull String tagName, boolean isVoid) {
        this.tagName = tagName;
        this.isVoid = isVoid;
        this.className = "";
        this.attributes = new HashMap<>();
        this.content = new ArrayList<>();
    }

    /**
     * Adds an attribute to this Element. Cannot be used to set className. Attributes may only be set once.
     * @param name Attribute name
     * @param value Optional value, does not need to be escaped/encoded
     * @return This element, for chaining
     * @throws IllegalStateException If {@code name} was "class" or an already set attribute
     */
    public Element attribute(@NonNull String name, @Nullable String value) throws IllegalStateException {
        Objects.requireNonNull(name);
        name = name.toLowerCase();
        if ("class".equals(name)) throw new IllegalStateException("Element className may not be set using #attribute(), use #className()");
        if (this.attributes.containsKey(name)) throw new IllegalStateException("duplicate attribute declaration: " + name);
        this.attributes.put(name, _ -> value);
        return this;
    }

    /**
     * Adds an attribute to this Element. Cannot be used to set className. Attributes may only be set once.
     * <br>
     * Alternative for attribute values that must be resolved within a {@link HtmlContext}
     * @param name Attribute name
     * @param value Optional value, does not need to be escaped/encoded
     * @return This element, for chaining
     * @throws IllegalStateException If {@code name} was "class" or an already set attribute
     */
    public Element attribute(@NonNull String name, @Nullable HTMLAttribute value) throws IllegalStateException {
        name = name.toLowerCase();
        if ("class".equals(name)) throw new IllegalStateException("Element className may not be set using #attribute(), use #className()");
        if (this.attributes.containsKey(name)) throw new IllegalStateException("duplicate attribute declaration: " + name);
        this.attributes.put(name, value);
        return this;
    }

    /// Sets the class of this element.
    /// ClassName may only be set once
    public Element className(@NonNull String className) {
        if (this.className.isEmpty()) {
            this.className = className;
        } else {
            throw new IllegalStateException("Classname already set!");
        }
        return this;
    }

    /// Sets the html id of this element.
    /// ID may only be set once
    public Element id(@NonNull ID id) { // TODO: Prohibit the use of #attribute() to set "id"
        return this.attribute("id", id.toString());
    }

    /// "style" attribute shorthand
    public Element style(@NonNull String style) {
        return this.attribute("style", style);
    }

    /// Adds text content to this element, shorthand for {@link #content(HTML)}
    public final Element text(@NonNull String textContent) {
        this.content.add(HTML.TEXT(textContent));
        return this;
    }

    /// "title" attribute shorthand
    public Element title(@NonNull String title) {
        this.attribute("title", title);
        return this;
    }

    /// Adds content to this element
    ///
    /// @throws IllegalStateException if this is a void element
    public final Element content(@NonNull HTML content) {
        if (isVoid) throw new IllegalStateException("void element may not have content");
        if (!(content instanceof EmptyHTML)) this.content.add(content);
        return this;
    }
    /// Adds content to this element
    ///
    /// @throws IllegalStateException if this is a void element
    public final Element content(@NonNull HTML... content) {
        if (isVoid) throw new IllegalStateException("void element may not have content");
        for (@NonNull HTML html : content) {
            if (!(html instanceof EmptyHTML)) this.content.add(html);
        }
        return this;
    }
    /// Adds content to this element
    ///
    /// @throws IllegalStateException if this is a void element
    public Element content(@NonNull Stream<? extends HTML> content) {
        if (isVoid) throw new IllegalStateException("void element may not have content");
        content.forEach(html -> {
            if (!(html instanceof EmptyHTML)) this.content.add(html);
        });
        return this;
    }

    /// True if this element has no content
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public void renderTo(HtmlContext context) throws RenderingException, IOException {
        if (className.length() > 0) {
            this.attributes.put("class", _ -> className);
        }

        context.write("<");
        context.write(this.tagName);
        for (var attribute : this.attributes.entrySet()) {
            context.write(" ");
            context.write(attribute.getKey());

            HTMLAttribute value = attribute.getValue();
            if (value != null) {
                context.write("='");
                context.write(HTMLUtil.escapeAttributeValue(value.resolve(context)));
                context.write("'");
            }
        }
        context.write(">");

        if (isVoid) {
            assert content.isEmpty();
        } else {
            for (HTML html : this.content) {
                html.renderTo(context);
            }
            context.write("</").write(this.tagName).write(">");
        }
    }


    /// Elements cannot be rendered to HTML without a {@link HtmlContext} and so do not support toString();
    ///
    /// Use {@link #renderTo(HtmlContext)} instead
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Elements do not support toString; use HTML#renderTo instead");
    }

    /// Special-case element for HTML5 document roots; Provides the {@code <html>} element and a DOCTYPE header
    static class Html5Root extends Element {
        public Html5Root(String lang) {
            super("html");
            this.attribute("lang", lang);
        }

        @Override
        public void renderTo(HtmlContext context) throws RenderingException, IOException {
            context.write("<!DOCTYPE html>\n");
            super.renderTo(context);
        }
    }

    /// Interface for HTMLAttribute values which need a {@link HtmlContext} to resolve
    public interface HTMLAttribute {
         String resolve(HtmlContext context);
    }
}
