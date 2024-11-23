package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.components.PageList;
import net.sentientturtle.nee.components.TextBox;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.*;

/// "index.html" main page
public class IndexPage extends Page {
    @Override
    public String name() {
        return Main.WEBSITE_NAME;
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
    protected HTML getContent(HtmlContext context) {
        return HTML.multi(
            new TextBox("About", HTML.RAW(
                "<pre>The " + Main.WEBSITE_NAME +
                " is an automatically updated reference site for <a href='https://en.wikipedia.org/wiki/Eve_Online'>EVE Online.<a>" +   // No link to official site as it's got login options
                "<br>Issues can be reported on the project's <a href=''>Github repository.</a>" + // TODO: Set URL
                "<br><br><i>The " + Main.WEBSITE_NAME + " project is not affiliated with CCP hf.</i></pre>"
            )),
            // TODO: Put behind a flag
            new TextBox("Development notes", HTML.RAW(
                """
                    This is a development build of the website. Some content and features may not be available or functional on all devices.
                    <br><br>
                    Known issues:
                    <ul>
                        <li>Search is slow and unreliable (Basic implementation for testing)</li>
                    </ul>
                    Work in progress features:
                    <ul>
                        <li>Dynamic map</li>
                    </ul>
                    Features needing feedback:
                    <ul>
                        <li>Ship/Structure/Module stats</li>
                        <li>Ship/Structure/Module index</li>
                        <li>Item/market index</li>
                        <li>Website name ("Encyclopedia" connotations as opposed to user-edited "wiki" connotations?)</li>
                        <li>Ideas for other front-page content</li>
                    </ul>
                    """
            )),
            new PageList(
                "Featured pages",
                new TypePage(context.sde.getTypes().get(35834)),
                new TypePage(context.sde.getTypes().get(648)),
                new TypePage(context.sde.getTypes().get(2464)),
                new TypePage(context.sde.getTypes().get(16213)),
                new TypePage(context.sde.getTypes().get(33474))
            ),
            new TextBox(null, HTML.multi(
                TEXT("Updated: "), context.sde.format_with_unit((double) (System.currentTimeMillis() / 1000), -2),
                BR(), TEXT("Game version: " + context.dataSources.gameVersion())
            ))
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
