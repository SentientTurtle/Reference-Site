package net.sentientturtle.nee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.html.HasPersistentUrl;
import net.sentientturtle.html.RenderingException;
import net.sentientturtle.html.context.NoopHtmlContext;
import net.sentientturtle.html.context.StringBuilderHtmlContext;
import net.sentientturtle.nee.data.*;
import net.sentientturtle.nee.data.sde.*;
import net.sentientturtle.nee.data.sharedcache.IconProvider;
import net.sentientturtle.nee.data.sharedcache.SharedCache;
import net.sentientturtle.nee.page.*;
import net.sentientturtle.nee.util.ExceptionUtil;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/// Entrypoint for generating website contents
public class Main {
    public static Path RES_FOLDER;
    public static Path TEMP_DIR;

    public static Path CACHE_PATH;
    public static Path JSONL_SDE_FILE;
    public static Path YAML_SDE_FILE;
    public static Path ICON_CACHE_FILE;
    public static boolean UPDATE_SDE;
    public static int COMPRESSION;  // No compression is moderately faster
    public static boolean GENERATE_ICONS;
    public static boolean SKIP_RESOURCES;
    public static boolean IS_DEV_BUILD;
    public static String DEPLOYMENT_URL;
    public static Set<String> PRE_COMPRESSED_FILES;

    // Website title as configurable variable in case a rename is needed
    public static final String WEBSITE_NAME = "New Eden Encyclopedia";
    public static final String WEBSITE_ABBREVIATION = "NEE";

    // Browser cache busting variable
    public static final String BUILD_NUMBER = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) % (3600 * 24 * 7));

    static {
        System.setProperty("sqlite4java.library.path", "./native");
    }

    private static DataSources initializedData = null;

    public static DataSources initialize(boolean patch) throws IOException, InterruptedException {
        if (initializedData != null) return initializedData;
        String propertyPath = System.getProperty("net.sentientturtle.nee.properties", "./nee.properties");

        Properties properties = new Properties();
        if (Files.exists(Path.of(propertyPath))) {
            properties.load(new FileReader(propertyPath));

            if (properties.containsKey("DELETE_THIS_KEY")) {
                System.out.println("Please configure properties in '" + propertyPath + "'");
                System.exit(1);
            }

            if (properties.containsKey("CACHE_PATH")) {
                CACHE_PATH = Path.of(properties.getProperty("CACHE_PATH"));
            } else {
                System.out.println("Missing CACHE_PATH property; Please configure properties in '" + propertyPath + "'");
                System.exit(1);
            }

            if (properties.containsKey("DEPLOYMENT_URL")) {
                DEPLOYMENT_URL = properties.getProperty("DEPLOYMENT_URL");
                if (!DEPLOYMENT_URL.endsWith("/")) {
                    DEPLOYMENT_URL = DEPLOYMENT_URL + "/";
                }
            } else {
                System.out.println("Missing DEPLOYMENT_URL property; Please configure properties in '" + propertyPath + "'");
                System.exit(1);
            }

            RES_FOLDER = Path.of(properties.getProperty("RESOURCE_FOLDER", "./rsc/"));
            TEMP_DIR = RES_FOLDER.resolve("temp");
            YAML_SDE_FILE = RES_FOLDER.resolve("sde_yaml.zip");
            JSONL_SDE_FILE = RES_FOLDER.resolve("sde_jsonl.zip");
            ICON_CACHE_FILE = RES_FOLDER.resolve("iconcache.zip");
            UPDATE_SDE = properties.getProperty("UPDATE_SDE", "TRUE").equalsIgnoreCase("TRUE");

            if (properties.getProperty("COMPRESSION").equalsIgnoreCase("TRUE")) {
                COMPRESSION = Deflater.DEFAULT_COMPRESSION;
            } else {
                COMPRESSION = Deflater.NO_COMPRESSION;
            }

            GENERATE_ICONS = properties.getProperty("GENERATE_ICONS", "FALSE").equalsIgnoreCase("TRUE");
            SKIP_RESOURCES = properties.getProperty("SKIP_RESOURCES", "FALSE").equalsIgnoreCase("TRUE");
            IS_DEV_BUILD = properties.getProperty("IS_DEV_BUILD", "TRUE").equalsIgnoreCase("TRUE");

            String files = properties.getProperty("PRE_COMPRESSED_FILES");
            if (files != null) {
                String[] extensions = files.split(",");
                for (int i = 0; i < extensions.length; i++) {
                    extensions[i] = extensions[i].toLowerCase();
                }
                PRE_COMPRESSED_FILES = Set.of(extensions);
            } else {
                PRE_COMPRESSED_FILES = Set.of();
            }
        } else {
            properties.setProperty("CACHE_PATH", "./rsc/cache/");
            properties.setProperty("RESOURCE_FOLDER", "./rsc/");
            properties.setProperty("UPDATE_SDE", "TRUE");
            properties.setProperty("COMPRESSION", "FALSE");
            properties.setProperty("GENERATE_ICONS", "FALSE");
            properties.setProperty("SKIP_RESOURCES", "FALSE");
            properties.setProperty("IS_DEV_BUILD", "TRUE");
            properties.setProperty("PRE_COMPRESSED_FILES", "html,css,js,json,txt");
            properties.setProperty("DELETE_THIS_KEY", "");

            properties.store(new FileWriter(propertyPath), "NEE Generator config");

            System.out.println("Please configure properties in '" + propertyPath + "'");
            System.exit(-1);
        }

        System.out.println("Initializing shared cache...");
        SharedCache sharedCache = new SharedCache(CACHE_PATH);
        System.out.println("\tLoaded shared cache!");

        if (UPDATE_SDE) {
            SDEUtils.updateJSONL(JSONL_SDE_FILE.toFile());
        }

        System.out.println("Loading SDE...");
        long sde_start = System.nanoTime();
        SDEData sdeData = new CCPSDEData(new JSONLSDEReader(JSONL_SDE_FILE), patch);
        long sdeDuration = System.nanoTime() - sde_start;
        System.out.println("\tSDE loaded! (" + (TimeUnit.NANOSECONDS.toMillis(sdeDuration) / 1000.0) +")");

        System.out.println("Loading icon cache...");
        IconProvider.readIconCache();
        Runtime.getRuntime().addShutdownHook(new Thread(IconProvider::writeIconCache));
        System.out.println("\tIcon cache loaded");

        System.out.println("Data initialized, game version: " + sharedCache.gameVersion);
        return (initializedData = new DataSources(sdeData, sharedCache, sharedCache.gameVersion));
    }

    public static Path OUTPUT_DIR = Path.of("./output");

    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        DataSources data = Main.initialize(true);

        Files.createDirectories(OUTPUT_DIR);

        // The OS does not like creating thousands of small files, saving to an archive is significantly faster.
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(OUTPUT_DIR.resolve("website.zip").toFile()));
        zipOutputStream.setLevel(COMPRESSION);

        Set<String> css = Collections.synchronizedSet(new LinkedHashSet<>());
        Set<String> js = Collections.synchronizedSet(new LinkedHashSet<>());

        ConcurrentLinkedQueue<String> redirects = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<Path, Resource.ResourceData> dependencies = new ConcurrentHashMap<>();

        System.out.println("Writing pages...");
        final AtomicInteger pageCount = new AtomicInteger(0);
        PageKind.pageStream(data.sdeData())
            .parallel()
            .forEach(page -> {
                var context = new StringBuilderHtmlContext(page.getPageKind().getFolderDepth(), data);

                try {
                    page.renderTo(context);

                    byte[] bytes = context.getBuffer().toString().getBytes(StandardCharsets.UTF_8);
                    String filePath = page.getPath().replace('\\', '/');

                    if (page instanceof HasPersistentUrl persistentUrl) {
                        redirects.add(
                            "/" + persistentUrl.getPersistentURL().replace('\\', '/')
                            + " /" + page.getURLPath().replace('\\', '/') + ";"
                        );
                    }

                    boolean gzip = PRE_COMPRESSED_FILES.contains(filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase());
                    synchronized (zipOutputStream) {
                        zipOutputStream.putNextEntry(new ZipEntry(filePath));
                        zipOutputStream.write(bytes);
                        zipOutputStream.closeEntry();

                        if (gzip) {
                            zipOutputStream.putNextEntry(new ZipEntry(filePath + ".gz"));
                            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipOutputStream);
                            gzipOutputStream.write(bytes);
                            gzipOutputStream.finish();
                            zipOutputStream.closeEntry();
                        }
                    }
                    int count = pageCount.incrementAndGet();
                    if (count % 500 == 0) {
                        System.out.println("\t" + count);
                    }

                    dependencies.putAll(context.getFileDependencies());
                } catch (RenderingException e) {
                    ExceptionUtil.sneakyThrow(e);
                } catch (Exception e) {
                    ExceptionUtil.sneakyThrow(new RenderingException("Exception in page: " + page.getPath(), e));
                }

                css.addAll(context.getCSS());
                js.addAll(context.getJavascript());
            });

        System.out.println("Writing resources");
        final AtomicInteger resourceCount = new AtomicInteger(0);
        if (!SKIP_RESOURCES) {
            dependencies.entrySet()
                .parallelStream()
                .forEach(entry -> {
                    try {
                        byte[] bytes = entry.getValue().getData(data);
                        String filename = entry.getKey().toString().replace('\\', '/');
                        boolean gzip = PRE_COMPRESSED_FILES.contains(filename.substring(filename.lastIndexOf('.') + 1).toLowerCase());

                        synchronized (zipOutputStream) {
                            zipOutputStream.putNextEntry(new ZipEntry(filename));
                            zipOutputStream.write(bytes);
                            zipOutputStream.closeEntry();

                            if (gzip) {
                                zipOutputStream.putNextEntry(new ZipEntry(filename + ".gz"));
                                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipOutputStream);
                                gzipOutputStream.write(bytes);
                                gzipOutputStream.finish();
                                zipOutputStream.closeEntry();
                            }
                        }

                        int count = resourceCount.incrementAndGet();
                        if (count % 500 == 0) {
                            System.out.println("\t" + count);
                        }
                    } catch (Exception e) {
                        ExceptionUtil.sneakyThrow(e);
                    }
                });
        }

        zipOutputStream.putNextEntry(new ZipEntry("dev_resource/"));
        zipOutputStream.closeEntry();

        zipOutputStream.putNextEntry(new ZipEntry("stylesheet.css"));
        for (String segment : css) {
            zipOutputStream.write(segment.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
        }
        if (PRE_COMPRESSED_FILES.contains("css")) {
            zipOutputStream.putNextEntry(new ZipEntry("stylesheet.css.gz"));
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipOutputStream);
            for (String segment : css) {
                gzipOutputStream.write(segment.getBytes(StandardCharsets.UTF_8));
                gzipOutputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
            }
            gzipOutputStream.finish();
            zipOutputStream.closeEntry();
        }

        try (Stream<Path> themes = Files.list(RES_FOLDER.resolve("themes"))) {
            for (Path theme: ((Iterable<Path>) themes::iterator)) {
                zipOutputStream.putNextEntry(new ZipEntry("themes/" + theme.getFileName()));
                Files.copy(theme, zipOutputStream);
                zipOutputStream.closeEntry();

                if (PRE_COMPRESSED_FILES.contains("css")) {
                    zipOutputStream.putNextEntry(new ZipEntry("themes/" + theme.getFileName() + ".gz"));
                    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipOutputStream);
                    Files.copy(theme, gzipOutputStream);
                    gzipOutputStream.finish();
                    zipOutputStream.closeEntry();
                }
            };
        }

        zipOutputStream.putNextEntry(new ZipEntry("script.js"));
        for (String segment : js) {
            zipOutputStream.write(segment.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
        }
        if (PRE_COMPRESSED_FILES.contains("js")) {
            zipOutputStream.putNextEntry(new ZipEntry("script.js.gz"));
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipOutputStream);
            for (String segment : js) {
                gzipOutputStream.write(segment.getBytes(StandardCharsets.UTF_8));
                gzipOutputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
            }
            gzipOutputStream.finish();
            zipOutputStream.closeEntry();
        }

        zipOutputStream.putNextEntry(new ZipEntry("favicon.ico"));
        zipOutputStream.write(Files.readAllBytes(RES_FOLDER.resolve("favicon.ico")));
        zipOutputStream.closeEntry();

        // TODO: Search functionality should be upgraded with something like typesense
        NoopHtmlContext searchContext = new NoopHtmlContext(0, data);
        ObjectMapper objectMapper = new ObjectMapper();
        record IndexEntry(String name, String path, String icon) { }

        String dynamicMapPagePath = new DynamicMapPage().getPath() + "?item=";
        List<IndexEntry> indexEntries = PageKind.pageStream(data.sdeData())
            .filter(page -> (page instanceof MapFrame || page instanceof TypePage))
            .map(page -> {
                String path;
                if (page instanceof MapFrame mapPage) {
                    path = dynamicMapPagePath + mapPage.mapItem.getID();
                } else {
                    path = page.getPath();
                }

                Resource pageIcon = page.getIcon(searchContext);
                return new IndexEntry(
                    page.name(),
                    path,
                    pageIcon != null ? pageIcon.getURI(searchContext, true) : null
                );
            })
            .collect(Collectors.toList());

        try {
            String searchJson = "const searchindex = " + objectMapper.writeValueAsString(indexEntries) + ";\nexport default searchindex;";
            byte[] bytes = searchJson.getBytes(StandardCharsets.UTF_8);

            zipOutputStream.putNextEntry(new ZipEntry(Resource.searchIndex().getURI(searchContext).replace('\\', '/')));
            zipOutputStream.write(bytes);
            zipOutputStream.closeEntry();

            if (PRE_COMPRESSED_FILES.contains("json")) {
                zipOutputStream.putNextEntry(new ZipEntry(
                    Resource.searchIndex().getURI(new NoopHtmlContext(0, data)).replace('\\', '/') + ".gz"
                ));
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipOutputStream);
                gzipOutputStream.write(bytes);
                gzipOutputStream.finish();
                zipOutputStream.closeEntry();
            }
        } catch (JsonProcessingException e) {
            ExceptionUtil.sneakyThrow(e);
        }

        System.out.println("Finalizing zip file...");
        zipOutputStream.close();

        System.out.println("Writing webserver files...");
        FileWriter redirectWriter = new FileWriter(OUTPUT_DIR.resolve("redirects.map").toFile());
        boolean first = true;
        for (String redirect : redirects) {
            if (first) { first = false; } else { redirectWriter.write('\n'); }
            redirectWriter.write(redirect);
        }
        redirectWriter.flush();

        System.out.println("Took: " + TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS) + " seconds.");
        System.out.println("Generated: " + pageCount.get() + " pages.");
    }
}
