package net.sentientturtle.nee;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.html.HTMLUtil;
import net.sentientturtle.html.RenderingException;
import net.sentientturtle.html.context.OutputStreamHtmlContext;
import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.SQLiteDataSupplier;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.pages.PageKind;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.util.ResourceSupplier;
import net.sentientturtle.nee.util.SDEUtils;
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
    public static boolean UPDATE_SDE;
    public static int COMPRESSION;  // No compression is moderately faster

    // Website title as configurable variable in case a rename is needed; I don't feel like buying a domain name yet
    public static final String WEBSITE_NAME = "Working Title Please Ignore";//"New Eden Encyclopedia";
    public static final String WEBSITE_ABBREVIATION = "NEE";

    static {
        System.setProperty("sqlite4java.library.path", "./native");
    }

    public static void main(String[] args) throws SQLiteException, IOException, InterruptedException {
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

            UPDATE_SDE = properties.getProperty("UPDATE_SDE").equalsIgnoreCase("TRUE");

            if (properties.getProperty("COMPRESSION").equalsIgnoreCase("TRUE")) {
                COMPRESSION = Deflater.DEFAULT_COMPRESSION;
            } else {
                COMPRESSION = Deflater.NO_COMPRESSION;
            }

        } else {
            properties.setProperty("SHARED_CACHE_PATH", "???");
            properties.setProperty("RESOURCE_FOLDER", "./rsc/");
            properties.setProperty("UPDATE_SDE", "FALSE");
            properties.setProperty("COMPRESSION", "FALSE");
            properties.setProperty("DELETE_THIS_KEY", "");

            properties.store(new FileWriter(propertyPath), "NEE Generator config");

            System.out.println("Please configure properties in '" + propertyPath + "'");
            System.exit(-1);
        }

        long startTime = System.nanoTime();

        if (UPDATE_SDE) SDEUtils.updateSDE(SDE_FILE.toFile());

        System.out.println("Loading SDE...");
        DataSupplier dataSupplier = new SQLiteDataSupplier(new SQLiteConnection(SDE_FILE.toFile()));
        // The OS does not like creating thousands of small files, saving to an archive is significantly faster.
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(Path.of("website.zip").toFile()));
        zipOutputStream.setLevel(COMPRESSION);

        LinkedHashSet<String> css = new LinkedHashSet<>();
        LinkedHashSet<String> js = new LinkedHashSet<>();

        SharedCacheReader sharedCache = new SharedCacheReader(SHARED_CACHE_PATH);
        FSDData fsdData = new FSDData(sharedCache);

        Map<Integer, Set<Integer>> mutaplasmidMap = dataSupplier.produceMap();

        for (Type mutaplasmid : dataSupplier.getGroupTypes().getOrDefault(1964, Set.of())) {
            List<FSDData.IOMapping> ioMappings = fsdData.dynamicAttributes.get(mutaplasmid.typeID)
                .inputOutputMapping();
            if (ioMappings.size() != 1) throw new IllegalStateException("Mutaplasmid IO mappings changed!");

            mutaplasmidMap.computeIfAbsent(
                ioMappings.get(0).resultingType(),
                dataSupplier::produceSet
            ).add(mutaplasmid.typeID);
        }

        for (Set<Integer> mutaplasmidGroup : mutaplasmidMap.values()) {
            for (Integer typeID : mutaplasmidGroup) {
                dataSupplier.getVariants()
                    .merge(typeID, mutaplasmidGroup, (one, two) -> {
                        one.addAll(two);
                        return one;
                    });
            }
        }

        Set<String> resourceFileSet = ConcurrentHashMap.newKeySet();
        final AtomicInteger pageCount = new AtomicInteger(0);
        PageKind.pageStream(dataSupplier)
            .forEach(page -> {
                var context = new OutputStreamHtmlContext(page.getPageKind().getFolderDepth(), new IDContext(page.toString()), dataSupplier, sharedCache, fsdData, zipOutputStream);

                try {
                    zipOutputStream.putNextEntry(new ZipEntry(page.getPath()));
                    page.renderTo(context);
                    zipOutputStream.closeEntry();
                    int count = pageCount.incrementAndGet();
                    if (count % 500 == 0) {
                        System.out.println("\tPages: " + count);
                    }

                    for (Map.Entry<String, ResourceSupplier> entry : context.getFileDependencies().entrySet()) {
                        if (resourceFileSet.add(entry.getKey())) {
                            zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                            zipOutputStream.write(entry.getValue().get());
                            zipOutputStream.closeEntry();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RenderingException e) {
                    throw new RuntimeException(e);
                }

                css.addAll(context.getCSS());
                js.addAll(context.getJavascript());
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

        zipOutputStream.putNextEntry(new ZipEntry("rsc/searchindex.js"));
        OutputStreamHtmlContext searchContext = new OutputStreamHtmlContext(0, new IDContext("searchindex"), dataSupplier, sharedCache, fsdData, zipOutputStream);
        ObjectMapper objectMapper = new ObjectMapper();
        record IndexEntry(String index, String name, String path, String icon) {}

        List<IndexEntry> indexEntries = PageKind.pageStream(dataSupplier)
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
    }
}
