package net.sentientturtle.html;

import net.sentientturtle.html.context.HtmlContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

/// Top level class for reusable HTML "components"
///
/// Components consist of a DIV with some user-specified content
public abstract class Component extends Element {
    protected Component(String className) {
        super("div");
        this.className(className);
    }

    /// Components cannot be rendered to HTML without a {@link HtmlContext} and so do not support toString();
    ///
    /// Use {@link #renderTo(HtmlContext)} instead
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Components do not support toString; use HTML#renderTo instead");
    }

    /// Render this component to HTML
    @Override
    public final void renderTo(HtmlContext context) throws RenderingException {
        try {
            // This is a bit janky, but we want the component's own content to precede any content added through Element#content
            var componentContent = getContent(context);
            var newContent = new ArrayList<HTML>(this.content.size() + componentContent.length);

            Collections.addAll(newContent, componentContent);
            newContent.addAll(this.content);

            this.content = newContent;
            super.renderTo(context);
        } catch (Exception e) {
            throw new RenderingException("Error in " + this.getClass().getName(), e);
        }
        context.registerCSS(getCSS());
        String script = getScript();
        if (script != null) context.registerJavascript(script);
    }

    /// HTML content for this Component
    protected abstract HTML[] getContent(HtmlContext context);

    /// CSS used by this component
    protected abstract String getCSS();

    /// Optional javascript snippet for this component
    protected @Nullable String getScript() {
        return null;
    }
}
