package net.sentientturtle.html.context;

import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSources;

/// {@link HtmlContext} that writes to a StringBuilder
public class StringBuilderHtmlContext extends HtmlContext {
    private final StringBuilder buffer;

    public StringBuilderHtmlContext(int folderDepth, IDContext ids, DataSources dataSources) {
        super(folderDepth, ids, dataSources);

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
