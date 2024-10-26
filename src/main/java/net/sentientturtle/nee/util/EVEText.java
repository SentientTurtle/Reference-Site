package net.sentientturtle.nee.util;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.pages.TypePage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.sentientturtle.html.HTML.TEXT;

/// Utility for handling EVE Online item texts, which may contain formatting and links
public class EVEText {
    private static final Pattern linkHref = Pattern.compile("<a href=\".+?\">(.+?)</a>|<url=.+?>(.+?)</url>");
    private static final Pattern showInfoHref = Pattern.compile("<a href=showinfo:(\\d+?)>(.+?)</a>");

    public static HTML[] escape(String text, DataSupplier dataSupplier) {
        // First regex pass -> write to a string builder
        StringBuilder textBuilder = new StringBuilder();
        int start = 0;
        Matcher matcher = linkHref.matcher(text);
        while (matcher.find()) {
            textBuilder.append(text, start, matcher.start());
            textBuilder.append(matcher.group(1));
            start = matcher.end();
        }
        textBuilder.append(text.substring(start));

        text = textBuilder.toString()
            .replaceAll("<color='.*?'>", "")  // Clear markup
            .replaceAll("<font .*?=\".*?\">", "")
            .replace("</color>", "")
            .replace("</font>", "")
            .replace("<i>", "").replace("</i>", "")
            .replace("<b>", "").replace("</b>", "")
            .replace("<u>", "").replace("</u>", "")
            .replace("<p>", "").replace("</p>", "")
            .replace("<li>", "").replace("</li>", "")
            .replace("<ul>", "").replace("</ul>", "")
            .replace("<br>", "\n")
            .replaceAll("<a href=showinfo:\\d+?//\\d+?>", "") // Clear links with itemIDs
            .trim();

        ArrayList<HTML> descriptionContent = new ArrayList<>();

        // Second regex pass -> write to HTML list
        start = 0;
        matcher = showInfoHref.matcher(text);
        while (matcher.find()) {
            String typeID = matcher.group(1);

            descriptionContent.add(TEXT(text.substring(start, matcher.start())));
            start = matcher.end();

            @Nullable   // Only allow links to existing types
            Type type = dataSupplier.getTypes().get(Integer.valueOf(typeID));
            if (type != null) {
                descriptionContent.add(new PageLink(new TypePage(type), matcher.group(2)));
            } else {
                descriptionContent.add(TEXT(matcher.group(2)));
            }
        }
        descriptionContent.add(TEXT(text.substring(start)));

        return descriptionContent.toArray(HTML[]::new);
    }
}
