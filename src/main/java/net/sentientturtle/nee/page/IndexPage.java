package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.components.PageList;
import net.sentientturtle.nee.components.TextBox;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.sentientturtle.html.HTML.*;

/// "index.html" main page
public class IndexPage extends Page {
    @Override
    public String name() {
        return Main.WEBSITE_NAME;
    }

    @Override
    public @Nullable String description() {
        return "An automatic wiki for EVE Online.";
    }

    @Override   // Override; Remove prefix on index page
    public String title() {
        return this.name();
    }

    @Override
    public String filename() {
        return "index";
    }

    @Override
    protected List<HTML> headEntries(HtmlContext context) {
        return List.of(
            SCRIPT_MODULE("""
                fetch("./rsc/status.json")
                        .then(r => r.json())
                        .then(status => {
                            document.querySelector("#server_status_infobox > .text_box_text")
                                .replaceChildren(
                                    document.createTextNode("Tranquility"),
                                    document.createElement("br"),
                                    document.createTextNode(`${status.players} players`),
                                    document.createElement("br"),
                                    document.createTextNode(`Server version: ${status.server_version}`),
                                    document.createElement("br"),
                                    document.createTextNode(`(Status updated: ${status.updated} EVE)`)
                                );
                        });
            """)
        );
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return HTML.multi(
            new TextBox("About", HTML.RAW(
                "<pre>The " + Main.WEBSITE_NAME +
                " is an automatically updated reference site for <a href='https://en.wikipedia.org/wiki/Eve_Online'>EVE Online.<a>" +   // No link to official site as it's got login options
                "<br>Issues can be reported on the project's <a href='https://github.com/SentientTurtle/Reference-Site'>Github repository.</a>" +
                "<br><br><i>The " + Main.WEBSITE_NAME + " project is not affiliated with CCP hf.</i></pre>"
            )),
            !Main.IS_DEV_BUILD ? HTML.empty() : new TextBox("Development notes", HTML.RAW("This is a development build of the website. Some content and features may not be available or functional on all devices.\n")),
            new PageList(
                "Featured pages",
                new TypePage(context.sde.getTypes().get(35834)),
                new TypePage(context.sde.getTypes().get(648)),
                new TypePage(context.sde.getTypes().get(2464)),
                new TypePage(context.sde.getTypes().get(16213)),
                new TypePage(context.sde.getTypes().get(33474))
            ),
            new TextBox("Version", HTML.multi(
                TEXT("Version: " + context.dataSources.gameVersion()),
                BR(),
                TEXT("Updated: "), context.sde.format_with_unit((double) (System.currentTimeMillis() / 1000), -2)
            )).id(context.tryID("site_status_infobox")),
            new TextBox("EVE Server Status", HTML.multi(
                HTML.TEXT("... Loading Server Status & Dynamic Data ..."),
                HTML.RAW("<noscript><br>⚠ Server status requires JavaScript ⚠</noscript>")
            )).id(context.tryID("server_status_infobox"))
        );
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.STATIC;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.file("bookicon.png");
    }
}
