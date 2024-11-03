package net.sentientturtle.html.context;

import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;

/// {@link HtmlContext} that writes to a StringBuilder
public class StringBuilderHtmlContext extends HtmlContext {
    private final StringBuilder buffer;

    public StringBuilderHtmlContext(int folderDepth, IDContext ids, DataSupplier data, SharedCacheReader sharedCache, FSDData fsdData) {
        super(folderDepth, ids, data, sharedCache, fsdData);

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
