package net.sentientturtle.nee.page;

import net.sentientturtle.html.*;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.components.Sidebar;
import net.sentientturtle.nee.data.ResourceLocation;

import static net.sentientturtle.html.HTML.*;

/**
 * Abstract class for Pages
 * Pages represent a full HTML document (i.e. A website page)
 */
@SuppressWarnings("WeakerAccess")
public abstract class Page extends Frame {
    private HTML getHeader(HtmlContext context) {
        return DIV().id(context.tryID("header")).content(
            SPAN("header_span").content(
                IMG(ResourceLocation.file("bookicon.png"), null, 64).className("header_icon"),
                TEXT_BOLD().className("font_header").id(context.tryID("header_text")).content(new PageLink(new IndexPage()))
            ),
            SPAN("header_span header_search").content(
                HTML.RAW("<form class='font_header' action='" + context.pathTo(new SearchResults()) + "'>" +
                         "<input class='font_header' id='search_input' type='text' placeholder='Search...' name='search' aria-label='Search'>" +
                         "</form>")
            )
        );
    }

    private static HTML getFooter(HtmlContext context) {
        return DIV().id(context.tryID("footer")).content(
            DIV("footer_text font_text").text(
                "EVE Online and the EVE logo are the registered trademarks of CCP hf.\n" +
                "All rights are reserved worldwide.\n" +
                "All other trademarks are the property of their respective owners.\n" +
                "EVE Online, the EVE logo, EVE and all associated logos and designs are the intellectual property of CCP hf.\n" +
                "All artwork, screenshots, characters, vehicles, storylines, world facts or other recognizable features of the intellectual property relating\n" +
                "to these trademarks are likewise the intellectual property of CCP hf.\n" +
                "CCP hf. has granted permission to the " + Main.WEBSITE_NAME + " project to use EVE Online and all associated logos and designs for promotional and\n" +
                "information purposes on its website but does not endorse, and is not in any way affiliated with, the " + Main.WEBSITE_NAME + " project.\n" +
                "CCP is in no way responsible for the content on or functioning of the " + Main.WEBSITE_NAME + ", nor can it be liable for any damage arising from the use of\n" +
                "the " + Main.WEBSITE_NAME + ".\n"
            )
        );
    }

    @Override
    public void renderTo(HtmlContext context) throws RenderingException {
        var head = HEAD().content(
            META().attribute("charset", "UTF-8"),
            META().attribute("name", "viewport").attribute("content", "width=device-width, initial-scale=1"),
            TITLE(this.title()),
            LINK().attribute("rel", "stylesheet").attribute("href", c -> c.pathTo("stylesheet.css")),
            LINK().attribute("rel", "icon").attribute("href", c -> ResourceLocation.file("bookicon.png").getURI(c))
        );

        head.content(META().attribute("property", "og:site_name").attribute("content", Main.WEBSITE_NAME));
        head.content(META().attribute("property", "og:title").attribute("content", this.name()));
        String page_description = this.description();
        if (page_description != null) {
            head.content(META().attribute("property", "og:description").attribute("content", page_description));
        }
        ResourceLocation icon = this.getIcon(context);
        if (icon != null) {
            head.content(META().attribute("property", "og:image").attribute("content", icon.getURI(context, true, Main.DEPLOYMENT_URL)));
        }

        for (HTML entry : headEntries(context)) {
            head.content(entry);
        }

        head.content(HTML.RAW("<script type='module' src='" + HTMLUtil.escapeAttributeValue(context.pathTo("script.js")) + "'></script>"));

        Element content = DOCUMENT_ROOT().content(
            head,
            BODY().className("body_grid").content(
                getHeader(context),
                new Sidebar().id(context.tryID("sidebar")),
                DIV().id(context.tryID("content"))
                    .content(getContent(context)),
                Page.getFooter(context)
            )
        );
        try {
            String head_font = "@font-face {" +
                               "   font-family: 'Electrolize';" +
                               "   src: url('" + ResourceLocation.file("font/Electrolize.woff2").getURI(context, true) + "') format('woff2');" +
                               "}";

            String roman_numeral_font = "@font-face {" +
                                        "   font-family: 'RomanNumeral';" +
                                        "   src: url('" + ResourceLocation.file("font/RomanNumerals.woff2").getURI(context, true) + "') format('woff2');" +
                                        "}";

            String css = this.getCSS(context);
            if (css != null) {
                context.registerCSS(css);
            }

            context.registerCSS(
                head_font + "\n" +
                roman_numeral_font + "\n" +
                GLOBAL_CSS + PAGE_CSS
            );

            content.renderTo(context);
        } catch (Exception e) {
            throw new RenderingException("Error in: " + this.getClass().getName() + " " + this.name(), e);
        }
    }
}
