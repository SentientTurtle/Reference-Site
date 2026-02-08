package net.sentientturtle.nee.data.sharedcache;

import com.almworks.sqlite4java.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.nee.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static net.sentientturtle.nee.util.ExceptionUtil.sneakyThrow;

public class SharedCache {
    private final HashMap<String, String> cacheIndex;
    private final HashMap<String, String> resourceHashes;
    private final ConcurrentHashMap<String, byte[]> dataCache;
    private final Path cacheFolder;
    public final String gameVersion;

    record GameStatus(String build) {}

    /**
     * Initializes SharedCacheReader
     * @param cacheFolder Path to the cache data folder
     * @throws IOException If an IO error occurs parsing the cache index file
     */
    public SharedCache(Path cacheFolder) throws IOException, InterruptedException {
        this.cacheFolder = cacheFolder;
        Files.createDirectories(this.cacheFolder);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        GameStatus serverVersion;
        try {
            serverVersion = mapper.readValue(new URI("https://binaries.eveonline.com/eveclient_TQ.json").toURL(), GameStatus.class);
        } catch (Exception e) {
            serverVersion = sneakyThrow(e);
        }
        this.gameVersion = serverVersion.build;

        this.cacheIndex = new HashMap<>();
        this.resourceHashes = new HashMap<>();
        this.dataCache = new ConcurrentHashMap<>();

        String indexFile = String.format("eveonline_%s.txt", serverVersion.build);
        cacheIndex.put(indexFile, indexFile);

        loadIndex(new String(getBytes(indexFile), StandardCharsets.UTF_8));
        loadIndex(new String(getBytes("app:/resfileindex.txt"), StandardCharsets.UTF_8));
    }

    private void loadIndex(String indexData) {
        try (Stream<String> lines = indexData.lines()) {
            lines.forEach(line -> {
                String[] split = line.split(",");
                if (split.length < 5) sneakyThrow(new IOException("Invalid index file format!"));

                String resource = split[0].replace('\\', '/');
                String location = split[1].replace('\\', '/');
                String fileHash = split[2];
                cacheIndex.put(resource.toLowerCase(), location);
                resourceHashes.put(resource.toLowerCase(), fileHash);
            });
        }
    }

    private URL urlFor(String resource) {
        String resourcePath = cacheIndex.get(resource.toLowerCase());
        if (resourcePath == null) throw new IllegalArgumentException("File not in shared cache: " + resource);

        try {
            return new URI((resource.startsWith("res:") ? "https://resources.eveonline.com/" : "https://binaries.eveonline.com/") + resourcePath).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            return ExceptionUtil.sneakyThrow(e);   // This should never happen
        }
    }

    public boolean containsResource(String resource) {
        return cacheIndex.containsKey(resource.toLowerCase());
    }

    public @Nullable String getResourceHash(String resource) {
        return this.resourceHashes.get(resource.toLowerCase());
    }

    public Path getPath(String resource) throws IOException {
        String resourcePath = cacheIndex.get(resource.toLowerCase());
        if (resourcePath == null) throw new IllegalArgumentException("File not in shared cache: " + resource);

        Path filePath = cacheFolder.resolve(resourcePath);
        if (Files.exists(filePath)) {
            return filePath;
        } else {
            try (InputStream inputStream = urlFor(resource).openStream()) {
                byte[] bytes = inputStream.readAllBytes();
                Path parent = filePath.getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.write(filePath, bytes);
                return filePath;
            }
        }
    }

    public byte[] getBytes(String resource) throws IOException {
        String resourcePath = cacheIndex.get(resource.toLowerCase());
        if (resourcePath == null) throw new IllegalArgumentException("File not in shared cache: " + resource);

        return dataCache.computeIfAbsent(
            resource.toLowerCase(),
            _ -> {
                try {
                    Path filePath = cacheFolder.resolve(resourcePath);
                    if (Files.exists(filePath)) {
                        return Files.readAllBytes(filePath);
                    } else {
                        try (InputStream inputStream = urlFor(resource).openStream()) {
                            byte[] bytes = inputStream.readAllBytes();
                            Path parent = filePath.getParent();
                            if (parent != null) Files.createDirectories(parent);
                            Files.write(filePath, bytes);
                            return bytes;
                        }
                    }
                } catch (IOException e) {
                    return sneakyThrow(e);
                }
            }
        );
    }
}
