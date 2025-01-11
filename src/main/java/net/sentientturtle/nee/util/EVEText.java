package net.sentientturtle.nee.util;

import net.sentientturtle.html.Frame;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.page.DynamicMapPage;
import net.sentientturtle.nee.page.MapPage;
import net.sentientturtle.nee.page.TypePage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.sentientturtle.html.HTML.TEXT;

/// Utility for handling EVE Online item texts, which may contain formatting and links
public class EVEText {
    // EVE Online text (item descriptions, player messages, etc) may contain HTML markup, including some custom elements.
    // The following elements are known to occur in item-type descriptions:
    // <a>, <url>, <color>, <font>, <div>, <p>, <ul>, <li>, <i>, <b>, <u>, <br>
    //
    // Attributes may be quoted or unquoted
    //
    // Links may use either <a> or <url> elements:
    //  <a href="[URL]"></a>
    //  <url=[URL]></url>
    // Besides URLs, links may also be:
    // * item types "showinfo:[TYPE_ID]"
    // * individual items "showinfo:[TYPE_ID]//[ITEM_ID]"
    // * In-Game-Browser links "evebrowser:[URL]"       (These links are defunct ingame, and all link to the now-offline official EVE wiki)
    //
    // Outside of item descriptions, the following are also known:
    // * kill reports "killReport:[KILL_MAIL_ID]:[KILL_REPORT_HASH]"
    // * war reports "warReport:[WAR_ID]"
    // * player notes "note:[NOTE_ID]"
    // * contracts "contract:[SYSTEM_ID]//[CONTRACT_ID]"
    // * ship fitting "fitting:[SHIP_DNA_STRING]"
    // * the in-game career agents menu "openCareerAgents:"
    //
    // Text styling uses the <color>, <font>, and regular HTML <i>, <b>, and <u> elements
    //
    // Colours are specified using names or 8-digit hexadecimal notation e.g. "yellow" or "#ff3399cc"
    // Sizes appear specified in pixels?
    // <color='[Hex code or color name]'></color>   <color=[Hex code or color name]></color>
    // <font size="[size in pixels]" color="[Hex code or color name]"></font>
    private static final Pattern pattern = Pattern.compile(
        // Four capturing groups:
        // 1: showinfo typeID & itemID, converted to internal link to respective item page
        // 2: (Text) content of a <a> or <url> link (must be present if group #1 is present), emitted as link text if #1 is present, emitted as plaintext otherwise
        // 3: <br> element, substituted with a pure newline `\n`
        // 4: Text style element such as <i>, included verbatim in the output HTML
        // Additionally, the <color>, <font>, <p>, <li>, <il>, <div> elements are stripped out of the text, leaving their contents as plaintext
        "<(?:a href|url)=[\"']?(?:showinfo:([\\d/]+))?[^>]*?>([^<]+?)</(?:a|url)>|</?(?:color[^'>]*?|font[^>]*?|p|li|ul)>|</?div[^>]*>|(<br>)|(</?[ibu]>)"
    );
    public static HTML[] escape(String input, SDEData sdeData, boolean retainMarkup) {
        ArrayList<HTML> htmlContent = new ArrayList<>();

        StringBuilder textBuilder = new StringBuilder(input.length());
        int start = 0;
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String showInfo = matcher.group(1);
            String textContent = matcher.group(2);
            boolean newLine = matcher.group(3) != null;
            String markup = matcher.group(4);

            textBuilder.append(input, start, matcher.start());
            start = matcher.end();

            if (showInfo != null) {
                if (textContent == null) throw new IllegalStateException("Showinfo link without content!");

                if (textBuilder.length() > 0) {
                    htmlContent.add(TEXT(textBuilder.toString()));
                    textBuilder.setLength(0);
                }

                int typeID;
                String[] splitInfo = showInfo.split("//", 2);
                typeID = Integer.parseInt(splitInfo[0]);
                if (splitInfo.length > 1) {
                    int itemID = Integer.parseInt(splitInfo[1]);
                    Frame page = switch (typeID) {
                        case 2 -> null; // Corporation: TODO link NPC corps once/if they get pages
                        case 3 -> new MapPage(Objects.requireNonNull(sdeData.getRegions().get(itemID)));
                        case 4 -> new MapPage(Objects.requireNonNull(sdeData.getConstellations().get(itemID)));
                        case 5 -> new MapPage(Objects.requireNonNull(sdeData.getSolarSystems().get(itemID)));
                        case 14 -> null;    // Moon, used only for Molea memorial site which is an unpublished type
                        case 30 -> null;    // Faction: TODO link NPC factions once/if they get pages
                        case 1373, 1374, 1375, 1376, 1377, 1378, 1379, 1380, 1381, 1382, 1383, 1384, 1385, 1386 -> null;    // Individual character
                        case 2016, 30889 -> null;  // Planet
                        case 16159 -> null; // Alliance
                        case 21646 -> null; // Now defunct station type
                        default -> throw new RuntimeException("Unknown itemID type: " + typeID);
                    };
                    if (page != null) {
                        if (page instanceof MapPage mapPage) {
                            String dynamicMapPagePath = new DynamicMapPage().getPath() + "?item=" + mapPage.mapItem.getID();
                            htmlContent.add(new PageLink(dynamicMapPagePath, textContent));
                        } else {
                            htmlContent.add(new PageLink(page, textContent));
                        }
                    } else {
                        htmlContent.add(TEXT(textContent));
                    }
                } else {
                    Type type = sdeData.getTypes().get(typeID);
                    if (type != null && type.groupID != 15) {   // Station Types are special-cased, they need to exist in loaded data but do not have pages
                        htmlContent.add(new PageLink(new TypePage(type), textContent));
                    } else {
                        htmlContent.add(TEXT(textContent));
                    }
                }
            } else if (textContent != null) {
                textBuilder.append(textContent);
            } else if (newLine) {
                textBuilder.append('\n');
            } else if (markup != null) {
                if (textBuilder.length() > 0) {
                    htmlContent.add(TEXT(textBuilder.toString()));
                    textBuilder.setLength(0);
                }
                if (retainMarkup) htmlContent.add(HTML.RAW(markup));
            }
        }
        if (!textBuilder.isEmpty()) {
            textBuilder.append(input.substring(start));
            htmlContent.add(TEXT(textBuilder.toString()));
        } else if (start < input.length()) {
            htmlContent.add(TEXT(input.substring(start)));
        }

        return htmlContent.toArray(HTML[]::new);
    }
}
