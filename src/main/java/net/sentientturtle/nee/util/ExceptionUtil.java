package net.sentientturtle.nee.util;

public class ExceptionUtil {
    @SuppressWarnings("unchecked")
    public static <E extends Throwable, T> T sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
