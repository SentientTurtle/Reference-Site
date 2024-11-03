package net.sentientturtle.html.context;

import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/// {@link HtmlContext} that writes directly to an OutputStream
public class OutputStreamHtmlContext extends HtmlContext {
    private final OutputStream out;

    public OutputStreamHtmlContext(int folderDepth, IDContext ids, DataSupplier data, SharedCacheReader sharedCache, FSDData fsdData, OutputStream out) {
        super(folderDepth, ids, data, sharedCache, fsdData);
        this.out = out;
    }

    @Override
    public HtmlContext write(String string) throws IOException {
        out.write(string.getBytes(StandardCharsets.UTF_8));
        return this;
    }
}
