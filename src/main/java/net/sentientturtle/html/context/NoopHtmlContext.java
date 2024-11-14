package net.sentientturtle.html.context;

import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSources;

/// {@link HtmlContext} that discards written content
public class NoopHtmlContext extends HtmlContext {
    public NoopHtmlContext(int folderDepth, IDContext ids, DataSources dataSources) {
        super(folderDepth, ids, dataSources);
    }

    @Override
    public HtmlContext write(String string) {
        return this;
    }
}
