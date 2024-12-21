package net.sentientturtle.nee.util;

/**
 * Small utility class to get MIME types from file extensions. Incomplete, mime types to be added as needed
 */
public class MIME { // Maybe integrate into ResourceLocation.DataSource, but not every data source has it's file-type known statically
    public static String getType(String extension) {
        return switch (extension) {
            case ".css" -> "text/css";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            default -> throw new RuntimeException("MIME type for extension " + extension + " not known!");
        };
    }
}
