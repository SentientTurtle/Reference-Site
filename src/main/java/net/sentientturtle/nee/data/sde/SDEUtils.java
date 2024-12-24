package net.sentientturtle.nee.data.sde;

import net.sentientturtle.nee.util.ExceptionUtil;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipInputStream;

/**
 * Utility class to download and update the EVE Online Static Data Export
 */
public class SDEUtils {
    private static final long downloadRate = 1024*1024*10;  // 10 Megabytes
    private static final String SDE_URL = "https://eve-static-data-export.s3-eu-west-1.amazonaws.com/tranquility/sde.zip";   // If this breaks, check https://developers.eveonline.com/resource
    private static final String CHECKSUM_URL = "https://eve-static-data-export.s3-eu-west-1.amazonaws.com/tranquility/checksum";

    public static void updateSDE(File file) throws IOException {
        System.out.println("Updating SDE...");
        String mostRecent = SDEUtils.downloadMD5().toLowerCase();
        boolean download;
        if (file.exists()) {

            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                md5 = ExceptionUtil.sneakyThrow(e);
            }

            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            while (zipInputStream.getNextEntry() != null) {
                md5.update(zipInputStream.readAllBytes());
            }

            String md5String = String.format("%032x", new BigInteger(1, md5.digest()));

            System.out.println("\tCurrent SDE: " + md5String + "\n\tMost recent: " + mostRecent);

            download = !md5String.equals(mostRecent);
            if (download) file.delete();
        } else {
            download = true;
        }
        if (download) {
            System.out.println("SDE Outdated!");
            downloadSDE(file);
        } else {
            System.out.println("SDE Up to date!");
        }
    }

    private static void downloadSDE(File file) throws IOException {
        try {
            ReadableByteChannel in = Channels.newChannel(new URI(SDE_URL).toURL().openStream());
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.getChannel().transferFrom(in, 0, Long.MAX_VALUE);
            }
            System.out.println("SDE Updated!");
        } catch (URISyntaxException e) {
            // There's nothing to be done, so throwing a checked exception is moot.
            throw new IOException(e);
        }
    }

    private static String downloadMD5() throws IOException {
        try {
            URLConnection conn = new URI(CHECKSUM_URL).toURL().openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.US_ASCII))) {
                return reader.lines()
                    .filter(line -> line.endsWith("sde.zip"))
                    .findFirst()
                    .get()
                    .substring(0, 32);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
