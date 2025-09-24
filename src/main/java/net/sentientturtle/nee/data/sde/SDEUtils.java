package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.nee.util.ExceptionUtil;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipFile;

/**
 * Utility class to download and update the EVE Online Static Data Export
 */
public class SDEUtils {

    private static final String SDE_VERSION_URL = "https://developers.eveonline.com/static-data/tranquility/latest.jsonl";
    private static final String YAML_SDE_URL = "https://developers.eveonline.com/static-data/eve-online-static-data-latest-yaml.zip";   // If this breaks, check https://developers.eveonline.com/docs/services/static-data/

    public static void updateYAML(File file) throws IOException {
        System.out.println("Updating YAML SDE...");
        int mostRecent;
        try {
            @JsonIgnoreProperties(ignoreUnknown = true)
            record SDEMetadata(int buildNumber) {};
            SDEMetadata sdeMetadata = new ObjectMapper().readValue(new URI(SDE_VERSION_URL).toURL(), SDEMetadata.class);
            mostRecent = sdeMetadata.buildNumber;
        } catch (URISyntaxException e) {
            throw new IOException(e);   // Should never happen, as the URI is hardcoded & correct.
        }


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

    private static void download(File file, InputStream inputStream) throws IOException {
        ReadableByteChannel in = Channels.newChannel(inputStream);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.getChannel().transferFrom(in, 0, Long.MAX_VALUE);
        }
        System.out.println("\tSDE Downloaded!");
    }
}
