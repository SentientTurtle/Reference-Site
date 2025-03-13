package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.TextBox;
import net.sentientturtle.nee.data.DevResources;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.*;

public class DevResourcePage extends Page {
    @Override
    public String name() {
        return "3rd party dev resources";
    }

    @Override
    public @Nullable String description() {
        return "Assorted files and data to aid EVE Online 3rd party development";
    }

    @Override
    public String filename() {
        return "thirdpartydev";
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.STATIC;
    }

    @Override
    public @Nullable ResourceLocation getIcon(HtmlContext context) {
        return null;
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .dev_resource_table {
                width: 100%;
                border-collapse: collapse;
            }
            
            .dev_resource_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .dev_resource_table td {
                padding: 0.25rem;
            }
            """;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return new TextBox("3rd party development resources",
            multi(
                TEXT("Below are various resources for EVE Online third party development. These are subject to the "),
                A("https://developers.eveonline.com/license-agreement", TEXT("EVE Online Third Party Development License")),
                BR(), BR()
            )
        ).content(
            DevResources.getResources(context.dataSources)
                .stream()
                .map(resourceGroup -> DIV().content(
                    HEADER().text(resourceGroup.name()),
                    PRE().content(TEXT(resourceGroup.description())),
                    TABLE("dev_resource_table").content(
                        resourceGroup.resources()
                            .stream()
                            .map(resource ->
                                TR().content(
                                    TD().content(A(context.pathTo(resource.file().getURI(context)), TEXT(resource.name()))),
                                    TD().content(resource.README() != null ? A(resource.README().getURI(context), TEXT("[README]")) : HTML.empty())
                                )
                            )
                    )
                ))
        ).content(
            HEADER().text("Linking to this site"),
            SPAN("text_box_text font_text")
                .text("Not all pages are on \"permalinks\", the following redirects & url patterns are available to programmatically link to this site. NOTE: The absence of trailing slashes is mandatory."),
            HTML.UL(
                HTML.multi(TEXT_BOLD("Types: "), TEXT_ITALICS("/type/{type_id}"), TEXT(" e.g. `/type/648` -> "), new PageLink(new TypePage(context.sde.getTypes().get(648)))),
                HTML.multi(TEXT_BOLD("Map: "), TEXT_ITALICS("/map.html?item={item_id}"), TEXT(" e.g. `/map.html?item=30000142` -> "), new PageLink("map.html?item=30000142", "Jita"), BR(), TEXT(" (Available: RegionID, ConstellationID, SolarSystemID, -1 for the entire New Eden Cluster, -2 for the Anoikis cluster (\"WH space\"))"))
            )
        );
    }
}
