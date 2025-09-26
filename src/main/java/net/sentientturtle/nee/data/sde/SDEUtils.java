package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipFile;

/**
 * Utility class to download and update the EVE Online Static Data Export
 */
public class SDEUtils {
    // If this breaks, check https://developers.eveonline.com/docs/services/static-data/
    private static final String SDE_VERSION_URL = "https://developers.eveonline.com/static-data/tranquility/latest.jsonl";
    private static final String YAML_SDE_URL = "https://developers.eveonline.com/static-data/eve-online-static-data-latest-yaml.zip";
    private static final String JSONL_SDE_URL = "https://developers.eveonline.com/static-data/eve-online-static-data-latest-jsonl.zip";

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SDEMetadata(int buildNumber) {};

    private static int getMostRecentVersion() throws IOException {
        try {
            SDEMetadata sdeMetadata = new ObjectMapper().readValue(new URI(SDE_VERSION_URL).toURL(), SDEMetadata.class);
            return sdeMetadata.buildNumber;
        } catch (URISyntaxException e) {
            throw new IOException(e);   // Should never happen, as the URI is hardcoded & correct.
        }
    }

    public static void updateYAML(File file) throws IOException {
        System.out.println("Updating YAML SDE...");
        int mostRecent = getMostRecentVersion();

        boolean download;
        if (file.exists()) {
            int currentVersion;
            try (ZipFile sdeZip = new ZipFile(file)) {
                String yaml = new String(sdeZip.getInputStream(sdeZip.getEntry("_sde.yaml")).readAllBytes());
                currentVersion = yaml.lines()
                    .filter(line -> line.startsWith("  buildNumber"))
                    .mapToInt(line -> Integer.parseInt(line.substring("  buildNumber: ".length())))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No buildnumber in `_sde.yaml`"));
            }

            System.out.println("\tCurrent YAML SDE: " + currentVersion + "\n\tMost recent: " + mostRecent);
            download = currentVersion != mostRecent;
            if (download) file.delete();
        } else {
            download = true;
        }
        if (download) {
            System.out.println("\tSDE Outdated!");
            try {
                download(file, new URI(YAML_SDE_URL).toURL().openStream());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else {
            System.out.println("\tSDE Up to date!");
        }
    }

    public static void updateJSONL(File file) throws IOException {
        System.out.println("Updating JSONL SDE...");
        int mostRecent = getMostRecentVersion();

        boolean download;
        if (file.exists()) {
            int currentVersion;
            try (ZipFile sdeZip = new ZipFile(file)) {
                SDEMetadata sdeMetadata = new ObjectMapper().readValue(sdeZip.getInputStream(sdeZip.getEntry("_sde.jsonl")), SDEMetadata.class);
                currentVersion = sdeMetadata.buildNumber;
            }

            System.out.println("\tCurrent JSONL SDE: " + currentVersion + "\n\tMost recent: " + mostRecent);
            download = currentVersion != mostRecent;
            if (download) file.delete();
        } else {
            download = true;
        }
        if (download) {
            System.out.println("\tSDE Outdated!");
            try {
                download(file, new URI(JSONL_SDE_URL).toURL().openStream());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else {
            System.out.println("\tSDE Up to date!");
        }
    }

    private static void download(File file, InputStream inputStream) throws IOException {
        ReadableByteChannel in = Channels.newChannel(inputStream);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.getChannel().transferFrom(in, 0, Long.MAX_VALUE);
        }
        System.out.println("\tSDE Downloaded!");
    }
}
