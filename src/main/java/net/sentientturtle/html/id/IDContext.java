package net.sentientturtle.html.id;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/// Object for obtaining unique HTML IDs, guarantees that all {@link ID} objects provided by a single instance are unique Strings
public class IDContext {
    private final String contextName;
    private final HashSet<String> usedIDs;
    private final HashMap<String, AtomicInteger> prefixMap;

    /**
     * @param contextName Name of this ID context, for use in error messages
     */
    public IDContext(String contextName) {
        this.contextName = contextName;
        this.usedIDs = new HashSet<>();
        this.prefixMap = new HashMap<>();
    }

    /**
     * Requests an ID, providing it if it has not been given out
     * @throws IllegalStateException If the requested ID has already been given out
     */
    public ID tryID(String id) {
        if (this.usedIDs.add(id)) {
            return new ID(id);
        } else {
            throw new IllegalStateException("HTML ID already used: `" + id + "` in context: " + this.contextName);
        }
    }

    /**
     * Provides a unique ID consisting of the specified prefix and a numerical suffix
     * <br>
     * Such that <pre>{@code
     * nextWithPrefix("Hello");
     * nextWithPrefix("World");
     * nextWithPrefix("Hello");
     * nextWithPrefix("World");
     * }</pre>
     * Yields the IDs "Hello0", "World0", "Hello1", "World1", etc.
     *
     * @param idPrefix ID prefix
     * @return Next unique ID
     */
    public ID nextWithPrefix(String idPrefix) {
        AtomicInteger num = this.prefixMap.computeIfAbsent(idPrefix, _ -> new AtomicInteger());
        while (usedIDs.contains(idPrefix + num.get())) {
            num.incrementAndGet();
        }
        String id = idPrefix + num.get();
        usedIDs.add(id);
        return new ID(id);
    }

    /**
     * Provides an array of unique IDs where all share the same numerical suffix
     * See {@link #nextWithPrefix(String)}
     */
    public ID[] nextWithPrefixes(String... idPrefixes) {
        AtomicInteger num = new AtomicInteger(0);
        for (String idPrefix : idPrefixes) {
             var counter = this.prefixMap.computeIfAbsent(idPrefix, _ -> new AtomicInteger());
             if (counter.get() > num.get()) {
                 num = counter;
             }
        }

        outer:
        while (true) {
            for (String idPrefix : idPrefixes) {
                if (usedIDs.contains(idPrefix + num.get())) {
                    num.incrementAndGet();
                    continue outer;
                }
            }
            break;
        }

        ID[] ids = new ID[idPrefixes.length];
        for (int i = 0; i < idPrefixes.length; i++) {
            String id = idPrefixes[i] + num.get();
            usedIDs.add(id);
            ids[i] = new ID(id);
        }
        return ids;
    }
}
