package net.sentientturtle.nee;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import net.sentientturtle.html.RenderingException;
import net.sentientturtle.html.context.OutputStreamHtmlContext;
import net.sentientturtle.html.id.IDContext;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.SQLiteDataSupplier;
import net.sentientturtle.nee.pages.PageKind;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;
import net.sentientturtle.nee.util.ResourceSupplier;
import net.sentientturtle.nee.util.SDEUtils;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/// Entrypoint for generating website contents
public class Main {
    public static @Nullable Path SHARED_CACHE_PATH;
    public static String SDE_FILE;
    public static boolean UPDATE_SDE;
    public static int COMPRESSION;  // No compression is moderately faster

    // Website title as configurable variable in case a rename is needed; I don't feel like buying a domain name yet
    public static final String WEBSITE_NAME = "New Eden Encyclopedia";
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
            }

            SDE_FILE = properties.getProperty("SDE_FILE", "./rsc/sqlite-latest.sqlite");

            UPDATE_SDE = properties.getProperty("UPDATE_SDE").equalsIgnoreCase("TRUE");

            if (properties.getProperty("COMPRESSION").equalsIgnoreCase("TRUE")) {
                COMPRESSION = Deflater.DEFAULT_COMPRESSION;
            } else {
                COMPRESSION = Deflater.NO_COMPRESSION;
            }

        } else {
            properties.setProperty("SHARED_CACHE_PATH", "???");
            properties.setProperty("SDE_FILE", "./rsc/sqlite-latest.sqlite");
            properties.setProperty("UPDATE_SDE", "FALSE");
            properties.setProperty("COMPRESSION", "FALSE");
            properties.setProperty("DELETE_THIS_KEY", "");

            properties.store(new FileWriter(propertyPath), "NEE Generator config");

            System.out.println("Please configure properties in '" + propertyPath + "'");
            System.exit(-1);
        }

        long startTime = System.nanoTime();

        if (UPDATE_SDE) SDEUtils.updateSDE(new File(SDE_FILE));

        DataSupplier dataSupplier = new SQLiteDataSupplier(new SQLiteConnection(new File(SDE_FILE)));
        // The OS does not like creating thousands of small files, saving to an archive is significantly faster.
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(Path.of("website.zip").toFile()));
        zipOutputStream.setLevel(COMPRESSION);

        LinkedHashSet<String> css = new LinkedHashSet<>();
        LinkedHashSet<String> js = new LinkedHashSet<>();

        SharedCacheReader sharedCache;
        if (SHARED_CACHE_PATH != null) {
            sharedCache = new SharedCacheReader(SHARED_CACHE_PATH);
        } else {
            sharedCache = new SharedCacheReader();
        }

        Set<String> resourceFileSet = ConcurrentHashMap.newKeySet();
        final AtomicInteger pageCount = new AtomicInteger(0);
        PageKind.pageStream(dataSupplier)
            .forEach(page -> {
                var context = new OutputStreamHtmlContext(page.getPageKind().getFolderDepth(), new IDContext(page.toString()), dataSupplier, sharedCache, zipOutputStream);

                try {
                    zipOutputStream.putNextEntry(new ZipEntry(page.getPath()));
                    page.renderTo(context);
                    zipOutputStream.closeEntry();
                    pageCount.incrementAndGet();
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

        zipOutputStream.close();

        System.out.println("Took: " + TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS) + " seconds.");
        System.out.println("Generated: " + pageCount.get() + " pages.");
    }
}
