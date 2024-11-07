package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.components.PageList;
import net.sentientturtle.nee.util.ResourceLocation;
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
            new IndexAbout("About", HTML.RAW(
                "<pre>The " + Main.WEBSITE_NAME +
                " is an automatically updated reference site for <a href='https://en.wikipedia.org/wiki/Eve_Online'>EVE Online.<a>" +   // No link to official site as it's got login options
                "<br>Issues can be reported on the project's <a href=''>Github repository.</a>" + // TODO: Set URL
                "<br><br><i>The " + Main.WEBSITE_NAME + " project is not affiliated with CCP hf.</i></pre>"
            )),
            // TODO: Put behind a flag
            new IndexAbout("Development notes", HTML.RAW(
                """
                    This is a development build of the website. Some content and features may not be available or functional on all devices.
                    <br><br>
                    Known issues:
                    <ul>
                        <li>Search is slow and unreliable (Basic implementation for testing)</li>
                    </ul>
                    Work in progress features:
                    <ul>
                        <li>Module stats</li>
                        <li>Dynamic map</li>
                    </ul>
                    Features needing feedback:
                    <ul>
                        <li>Ship/Structure stats</li>
                        <li>Ship/Structure/Module index</li>
                        <li>Item/market index</li>
                        <li>Website name ("Encyclopedia" connotations as opposed to user-edited "wiki" connotations?)</li>
                        <li>Ideas for other front-page content</li>
                    </ul>
                    """
            )),
            new PageList(
                "Featured pages",
                new TypePage(context.data.getTypes().get(35834)),
                new TypePage(context.data.getTypes().get(648)),
                new TypePage(context.data.getTypes().get(2464)),
                new TypePage(context.data.getTypes().get(16213)),
                new TypePage(context.data.getTypes().get(33474))
            )
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

    private static class IndexAbout extends Component {
        private final String title;
        private final HTML content;

        public IndexAbout(String title, HTML content) {
            super("index_about colour_theme_minor");
            this.title = title;
            this.content = content;
        }

        @Override
        protected HTML[] getContent(HtmlContext context) {
            return new HTML[]{
                HEADER("font_header index_about_header").text(this.title),
                SPAN("index_about_text font_text").content(this.content)
            };
        }

        @Override
        protected String getCSS() {
            return """
                .index_about {
                    display: flex;
                    flex-direction: column;
                    padding: 1rem;
                }
                
                .index_about_header {
                    font-size: 1.5rem;
                }
                
                .index_about_text {
                    font-style: italic;
                    font-size: 0.9rem;
                }""";
        }
    }
}
