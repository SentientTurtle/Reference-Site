package net.sentientturtle.html.context;

import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSources;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/// {@link HtmlContext} that writes directly to an OutputStream
public class OutputStreamHtmlContext extends HtmlContext {
    private final OutputStream out;

    public OutputStreamHtmlContext(int folderDepth, IDContext ids, DataSources dataSources, OutputStream out) {
        super(folderDepth, ids, dataSources);
        this.out = out;
    }

    @Override
    public HtmlContext write(String string) throws IOException {
        out.write(string.getBytes(StandardCharsets.UTF_8));
        return this;
    }
}
