package net.sentientturtle.nee.data.sde;

import net.sentientturtle.nee.util.ExceptionUtil;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipInputStream;

/**
 * Utility class to download and update the EVE Online Static Data Export
 */
public class SDEUtils {
    private static final String YAML_SDE_URL = "https://eve-static-data-export.s3-eu-west-1.amazonaws.com/tranquility/sde.zip";   // If this breaks, check https://developers.eveonline.com/resource
    private static final String YAML_CHECKSUM_URL = "https://eve-static-data-export.s3-eu-west-1.amazonaws.com/tranquility/checksum";

    private static final String SQLITE_SDE_URL = "https://www.fuzzwork.co.uk/dump/sqlite-latest.sqlite.bz2";   // Third party provided SDE conversion
    private static final String SQLITE_CHECKSUM_URL = "https://www.fuzzwork.co.uk/dump/sqlite-latest.sqlite.bz2.md5";

    public static void updateSqlite(File file) throws IOException {
        System.out.println("Updating Sqlite SDE...");
        Path md5File = file.toPath().resolveSibling("sde_sqlite.md5");
        String mostRecent;
        try {
            mostRecent = new String(new URI(SQLITE_CHECKSUM_URL).toURL().openStream().readAllBytes(), StandardCharsets.UTF_8)
                .substring(0, 32)
                .toLowerCase();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        boolean download;
        if (file.exists()) {
            String md5String;

            if (md5File.toFile().exists()) {
                md5String = Files.readString(md5File);
            } else {
                try {
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    md5.update(Files.readAllBytes(file.toPath()));

                    md5String = String.format("%032x", new BigInteger(1, md5.digest()));
                    Files.writeString(md5File, md5String);
                } catch (NoSuchAlgorithmException e) {
                    md5String = ExceptionUtil.sneakyThrow(e);
                }
            }

            System.out.println("\tCurrent Sqlite SDE: " + md5String + "\n\tMost recent: " + mostRecent);
            download = !md5String.equals(mostRecent);
            if (download) file.delete();
        } else {
            download = true;
        }
        if (download) {
            System.out.println("\tSDE Outdated!");

            try {
                download(file, new BZip2CompressorInputStream(new URI(SQLITE_SDE_URL).toURL().openStream()));
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
            Files.writeString(md5File, mostRecent);
        } else {
            System.out.println("\tSDE Up to date!");
        }
    }

    public static void updateYAML(File file) throws IOException {
        System.out.println("Updating YAML SDE...");
        Path md5File = file.toPath().resolveSibling("sde_yaml.md5");
        String mostRecent = SDEUtils.yamlMD5();
        boolean download;
        if (file.exists()) {
            String md5String;

            if (md5File.toFile().exists()) {
                md5String = Files.readString(md5File);
            } else {
                MessageDigest md5;
                try {
                    md5 = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    md5 = ExceptionUtil.sneakyThrow(e);
                }

                // A bit clunky to read the zip header twice, but ZipFile's enumeration sucks.
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
                while (zipInputStream.getNextEntry() != null) {
                    md5.update(zipInputStream.readAllBytes());
                }

                md5String = String.format("%032x", new BigInteger(1, md5.digest()));
                Files.writeString(md5File, md5String);
            }

            System.out.println("\tCurrent YAML SDE: " + md5String + "\n\tMost recent: " + mostRecent);
            download = !md5String.equals(mostRecent);
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
            Files.writeString(md5File, mostRecent);
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

    private static String yamlMD5() throws IOException {
        try {
            URLConnection conn = new URI(YAML_CHECKSUM_URL).toURL().openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.US_ASCII))) {
                return reader.lines()
                    .filter(line -> line.endsWith("sde.zip"))
                    .findFirst()
                    .get()
                    .substring(0, 32)
                    .toLowerCase();
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
