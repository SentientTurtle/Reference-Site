package net.sentientturtle.html.context;

import net.sentientturtle.nee.data.DataSources;

/// {@link HtmlContext} that writes to a StringBuilder
public class StringBuilderHtmlContext extends HtmlContext {
    private final StringBuilder buffer;

    public StringBuilderHtmlContext(int folderDepth, DataSources dataSources) {
        super(folderDepth, dataSources);

        this.buffer = new StringBuilder();
    }

    @Override
    public HtmlContext write(String string) {
        this.buffer.append(string);
        return this;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }
}
