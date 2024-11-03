package net.sentientturtle.html.context;

import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;

/// {@link HtmlContext} that discards written content
public class NoopHtmlContext extends HtmlContext {
    public NoopHtmlContext(int folderDepth, IDContext ids, DataSupplier data, SharedCacheReader sharedCache, FSDData fsdData) {
        super(folderDepth, ids, data, sharedCache, fsdData);
    }

    @Override
    public HtmlContext write(String string) {
        return this;
    }
}
