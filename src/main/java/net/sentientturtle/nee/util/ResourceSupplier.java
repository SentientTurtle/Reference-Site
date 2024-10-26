package net.sentientturtle.nee.util;

import java.io.IOException;

/// {@link java.util.function.Supplier}-like interface that yields a byte-array and throws IOException
public interface ResourceSupplier {
    byte[] get() throws IOException;
}
