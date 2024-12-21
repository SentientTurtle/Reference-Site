package net.sentientturtle.nee.data.sharedcache;

import net.sentientturtle.nee.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static net.sentientturtle.nee.util.ExceptionUtil.sneakyThrow;

/// Reader for the EVE Online game files; Referred to as "Shared Cache"
///
/// Resources are specified by a shared-cache specific path; e.g. "res:/ui/texture/icons/7_64_15.png", referred to as `resource` in this project
public class SharedCacheReader {
    private final HashMap<String, Path> cacheIndex;
    private final HashMap<String, String> resourceHashes;
    private final ConcurrentHashMap<String, byte[]> dataCache;
    private final Path cacheFolder;
    private final Path resFiles;

    /**
     * Initializes SharedCacheReader
     * @param cacheFolder Path to the cache data folder; Usually the `SharedCache` folder within the game install folder
     * @throws IOException If an IO error occurs parsing the cache index file
     */
    public SharedCacheReader(Path cacheFolder) throws IOException {
        this.cacheFolder = cacheFolder;
        this.resFiles = cacheFolder.resolve("ResFiles");

        Path indexFile = cacheFolder.resolve("tq/resfileindex.txt");
        if (!Files.exists(indexFile)) throw new IOException("No cache index file in cache folder " + cacheFolder);

        this.cacheIndex = new HashMap<>();
        this.resourceHashes = new HashMap<>();
        try (Stream<String> lines = Files.lines(indexFile)) {
            lines.forEach(line -> {
                String[] split = line.split(",");
                if (split.length < 5) ExceptionUtil.sneakyThrow(new IOException("Invalid index file format!"));

                String resource = split[0].replace('\\', '/');
                String location = split[1];
                String fileHash = split[2];
                cacheIndex.put(resource.toLowerCase(), Path.of(location));
                resourceHashes.put(resource.toLowerCase(), fileHash);
            });
        }

        this.dataCache = new ConcurrentHashMap<>();
    }

    Path getCacheFolder() {
        return cacheFolder;
    }

    public boolean containsResource(String resource) {
        Path resourcePath = cacheIndex.get(resource.toLowerCase());
        return resourcePath != null && Files.exists(resFiles.resolve(resourcePath));
    }

    public @Nullable String getResourceHash(String resource) {
        return this.resourceHashes.get(resource.toLowerCase());
    }

    public Path getPath(String resource) {
        Path resourcePath = cacheIndex.get(resource.toLowerCase());
        if (resourcePath == null) throw new IllegalArgumentException("File not in shared cache: " + resource);
        return resFiles.resolve(resourcePath);
    }

    public byte[] getBytes(String resource) throws IOException {
        Path resourcePath = cacheIndex.get(resource.toLowerCase());
        if (resourcePath == null) throw new IllegalArgumentException("File not in shared cache: " + resource);

        return dataCache.computeIfAbsent(
            resource.toLowerCase(),
            _ -> {
                try {
                    return Files.readAllBytes(resFiles.resolve(resourcePath));
                } catch (IOException e) {
                    return sneakyThrow(e);
                }
            }
        );
    }

    public InputStream getInputStream(String resource) throws IOException {
        Path resourcePath = cacheIndex.get(resource.toLowerCase());
        if (resourcePath == null) throw new IllegalArgumentException("File not in shared cache: " + resource);

        return new ByteArrayInputStream(dataCache.computeIfAbsent(
            resource.toLowerCase(),
            _ -> {
                try {
                    return Files.readAllBytes(resFiles.resolve(resourcePath));
                } catch (IOException e) {
                    return sneakyThrow(e);
                }
            }
        ));
    }
}
