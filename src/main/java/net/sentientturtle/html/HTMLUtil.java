package net.sentientturtle.html;

import org.jspecify.annotations.NonNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/// HTML Utilities
public class HTMLUtil {
    /**
     * Performs syntax escaping for a HTML attribute value
     * <p>
     * This ensures the value is a syntactically correct HTML attribute, but does not sanitize user input.
     * e.g. does not prevent scripts being inserted
     *
     * @param value Attribute value to escape
     * @return Syntax-escaped HTML attribute value
     */
    public static String escapeAttributeValue(@NonNull String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;");
    }

    /**
     * Performs escaping for HTML text content
     *
     * @param text text
     * @return Escaped HTML text content
     */
    public static String escapeText(@NonNull String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /// Hack-y percent-encoding, correct but to be replaced with proper encoding
    public static String urlQuasiEscape(@NonNull String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Maps a name to a safe filename and URL filename; Not true "escaping"
     * <br>
     * Caution: Currently does not reject reserved filenames on windows
     *
     * @param name name
     * @return Escaped name
     */
    public static String escapeFileNameURL(@NonNull String name) {
        return name
            .replace("<", "")
            .replace(">", "")
            .replace(":", "")
            .replace("\"", "''")
            .replace("/", "-")
            .replace("\\", "-")
            .replace("|", "-")
            .replace("?", "")
            .replace("*", "");
    }
}
