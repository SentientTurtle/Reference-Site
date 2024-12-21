package net.sentientturtle.html.context;

import net.sentientturtle.nee.data.DataSources;

/// {@link HtmlContext} that discards written content
public class NoopHtmlContext extends HtmlContext {
    public NoopHtmlContext(int folderDepth, DataSources dataSources) {
        super(folderDepth, dataSources);
    }

    @Override
    public HtmlContext write(String string) {
        return this;
    }
}
