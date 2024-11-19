package net.sentientturtle.nee;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.html.HTMLUtil;
import net.sentientturtle.html.RenderingException;
import net.sentientturtle.html.context.NoopHtmlContext;
import net.sentientturtle.html.context.OutputStreamHtmlContext;
import net.sentientturtle.html.context.StringBuilderHtmlContext;
import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSources;
import net.sentientturtle.nee.data.SDEData;
import net.sentientturtle.nee.data.SQLiteSDEData;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.data.sharedcache.IconProvider;
import net.sentientturtle.nee.page.PageKind;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.SDEUtils;
import net.sentientturtle.util.ExceptionUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/// Entrypoint for generating website contents
public class Main {
    public static final ResourceLocation.ReferenceFormat REFERENCE_FORMAT = ResourceLocation.ReferenceFormat.EXTERNAL;
    public static Path RES_FOLDER;
    public static Path TEMP_DIR;

    public static Path SHARED_CACHE_PATH;
    public static Path SDE_FILE;
    public static Path ICON_CACHE_FILE;
    public static boolean UPDATE_SDE;
    public static int COMPRESSION;  // No compression is moderately faster
    public static boolean GENERATE_ICONS;
    public static boolean SKIP_RESOURCES;

    // Website title as configurable variable in case a rename is needed; I don't feel like buying a domain name yet
    public static final String WEBSITE_NAME = "Working Title Please Ignore";//"New Eden Encyclopedia";
    public static final String WEBSITE_ABBREVIATION = "NEE";

    static {
        System.setProperty("sqlite4java.library.path", "./native");
    }

    private static DataSources initializedData = null;
    public static DataSources initialize(boolean patch) throws IOException, SQLiteException {
        if (initializedData != null) return initializedData;
        String propertyPath = System.getProperty("net.sentientturtle.nee.properties", "./nee.properties");

        Properties properties = new Properties();
        if (Files.exists(Path.of(propertyPath))) {
            properties.load(new FileReader(propertyPath));

            if (properties.containsKey("DELETE_THIS_KEY")) {
                System.out.println("Please configure properties in '" + propertyPath + "'");
                System.exit(1);
            }

            if (properties.containsKey("SHARED_CACHE_PATH")) {
                SHARED_CACHE_PATH = Path.of(properties.getProperty("SHARED_CACHE_PATH"));
            } else {
                System.out.println("Missing SHARED_CACHE_PATH property; Please configure properties in '" + propertyPath + "'");
                System.exit(1);
            }

            RES_FOLDER = Path.of(properties.getProperty("RESOURCE_FOLDER", "./rsc/"));
            TEMP_DIR = RES_FOLDER.resolve("temp");
            SDE_FILE = RES_FOLDER.resolve("sqlite-latest.sqlite");
            ICON_CACHE_FILE = RES_FOLDER.resolve("iconcache.zip");
            UPDATE_SDE = properties.getProperty("UPDATE_SDE", "TRUE").equalsIgnoreCase("TRUE");

            if (properties.getProperty("COMPRESSION").equalsIgnoreCase("TRUE")) {
                COMPRESSION = Deflater.DEFAULT_COMPRESSION;
            } else {
                COMPRESSION = Deflater.NO_COMPRESSION;
            }

            GENERATE_ICONS = properties.getProperty("GENERATE_ICONS", "FALSE").equalsIgnoreCase("TRUE");
            SKIP_RESOURCES = properties.getProperty("SKIP_RESOURCES", "FALSE").equalsIgnoreCase("TRUE");
        } else {
            properties.setProperty("SHARED_CACHE_PATH", "???");
            properties.setProperty("RESOURCE_FOLDER", "./rsc/");
            properties.setProperty("UPDATE_SDE", "FALSE");
            properties.setProperty("COMPRESSION", "FALSE");
            properties.setProperty("GENERATE_ICONS", "FALSE");
            properties.setProperty("SKIP_RESOURCES", "FALSE");
            properties.setProperty("DELETE_THIS_KEY", "");

            properties.store(new FileWriter(propertyPath), "NEE Generator config");

            System.out.println("Please configure properties in '" + propertyPath + "'");
            System.exit(-1);
        }

        if (UPDATE_SDE) SDEUtils.updateSDE(SDE_FILE.toFile());

        System.out.println("Loading SDE...");
        SDEData SDEData = new SQLiteSDEData(new SQLiteConnection(SDE_FILE.toFile()), patch);
        System.out.println("\tSDE loaded!");
        System.out.println("Initializing shared cache...");
        SharedCacheReader sharedCache = new SharedCacheReader(SHARED_CACHE_PATH);
        System.out.println("\tConnected to shared cache!");
        System.out.println("Connecting to Python FSD data...");
        FSDData fsdData = new FSDData(sharedCache);
        System.out.println("\tFSD data loaded!");
        // Patch FSD into SDE data
        {
            Map<Integer, Set<Integer>> mutaplasmidMap = SDEData.produceMap();
            for (Type mutaplasmid : SDEData.getGroupTypes().getOrDefault(1964, Set.of())) {
                List<FSDData.IOMapping> ioMappings = fsdData.dynamicAttributes.get(mutaplasmid.typeID)
                    .inputOutputMapping();
                if (ioMappings.size() != 1) throw new IllegalStateException("Mutaplasmid IO mappings changed!");

                mutaplasmidMap.computeIfAbsent(
                    ioMappings.get(0).resultingType(),
                    SDEData::produceSet
                ).add(mutaplasmid.typeID);
            }

            for (Set<Integer> mutaplasmidGroup : mutaplasmidMap.values()) {
                for (Integer typeID : mutaplasmidGroup) {
                    SDEData.getVariants()
                        .merge(typeID, mutaplasmidGroup, (one, two) -> {
                            one.addAll(two);
                            return one;
                        });
                }
            }
        }
        System.out.println("Data initialized");
        return (initializedData = new DataSources(SDEData, sharedCache, fsdData));
    }

    public static Path OUTPUT_DIR = Path.of("./output");
    public static void main(String[] args) throws SQLiteException, IOException {
        long startTime = System.nanoTime();
        DataSources sources = Main.initialize(true);

        System.out.println("Loading icon cache...");
        IconProvider.readIconCache();
        System.out.println("\tIcon cache loaded");

        Files.createDirectories(OUTPUT_DIR);

        // The OS does not like creating thousands of small files, saving to an archive is significantly faster.
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(OUTPUT_DIR.resolve("website.zip").toFile()));
        zipOutputStream.setLevel(COMPRESSION);

        Set<String> css = Collections.synchronizedSet(new LinkedHashSet<>());
        Set<String> js = Collections.synchronizedSet(new LinkedHashSet<>());

        ConcurrentHashMap<String, ResourceLocation.ResourceData> dependencies = new ConcurrentHashMap<>();

        System.out.println("Writing pages...");
        final AtomicInteger pageCount = new AtomicInteger(0);
        PageKind.pageStream(sources.SDEData())
            .parallel()
            .forEach(page -> {
                var context = new StringBuilderHtmlContext(page.getPageKind().getFolderDepth(), new IDContext(page.toString()), sources);

                try {
                    page.renderTo(context);

                    synchronized (zipOutputStream) {
                        zipOutputStream.putNextEntry(new ZipEntry(page.getPath().replace('\\', '/')));
                        zipOutputStream.write(context.getBuffer().toString().getBytes(StandardCharsets.UTF_8));
                        zipOutputStream.closeEntry();
                    }
                    int count = pageCount.incrementAndGet();
                    if (count % 500 == 0) {
                        System.out.println("\t" + count);
                    }

                    dependencies.putAll(context.getFileDependencies());
                } catch (IOException | RenderingException e) {
                    ExceptionUtil.sneakyThrow(e);
                }

                css.addAll(context.getCSS());
                js.addAll(context.getJavascript());
            });

        System.out.println("Writing resources");
        final AtomicInteger resourceCount = new AtomicInteger(0);
        if (!SKIP_RESOURCES)
            dependencies.entrySet()
                .parallelStream()
                .forEach(entry -> {
                    try {
                        byte[] data = entry.getValue().getData(sources);

                        synchronized (zipOutputStream) {
                            zipOutputStream.putNextEntry(new ZipEntry(entry.getKey().replace('\\', '/')));
                            zipOutputStream.write(data);
                            zipOutputStream.closeEntry();
                        }

                        int count = resourceCount.incrementAndGet();
                        if (count % 500 == 0) {
                            System.out.println("\t" + count);
                        }
                    } catch (Exception e) {
                        ExceptionUtil.sneakyThrow(e);
                    }
                });

        zipOutputStream.putNextEntry(new ZipEntry("stylesheet.css"));
        for (String segment : css) {
            zipOutputStream.write(segment.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
        }

        zipOutputStream.putNextEntry(new ZipEntry("script.js"));
        for (String segment : js) {
            zipOutputStream.write(segment.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
        }

        zipOutputStream.putNextEntry(new ZipEntry(
            ResourceLocation.searchIndex().getURI(new NoopHtmlContext(0, new IDContext(""), sources)).replace('\\', '/')
        ));
        OutputStreamHtmlContext searchContext = new OutputStreamHtmlContext(0, new IDContext("searchindex"), sources, zipOutputStream);
        ObjectMapper objectMapper = new ObjectMapper();
        record IndexEntry(String index, String name, String path, String icon) {}

        List<IndexEntry> indexEntries = PageKind.pageStream(sources.SDEData())
            .map(page -> {
                ResourceLocation pageIcon = page.getIcon(searchContext);
                return new IndexEntry(
                    page.name().toLowerCase(),
                    page.name(),
                    HTMLUtil.escapeAttributeValue(page.getPath()),
                    pageIcon != null ? HTMLUtil.escapeAttributeValue(pageIcon.getURI(searchContext, true)) : null
                );
            })
            .collect(Collectors.toList());

        String searchJson = null;
        try {
            searchJson = objectMapper.writeValueAsString(indexEntries);
        } catch (JsonProcessingException e) {
            ExceptionUtil.sneakyThrow(e);
        }
        searchContext.write("const searchindex = " + searchJson + ";\nexport default searchindex;");

        zipOutputStream.close();

        System.out.println("Took: " + TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS) + " seconds.");
        System.out.println("Generated: " + pageCount.get() + " pages.");


        System.out.println("Writing icon cache...");
        IconProvider.writeIconCache();
        System.out.println("All finished.");
    }
}
