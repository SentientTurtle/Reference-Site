package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
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
        return PageKind.DEV_RESOURCE;
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
                                    TD().content(A(context.pathTo(resource.path().toString()), TEXT(resource.name()))),
                                    TD().content(A(resource.README().getURI(context), TEXT("[README]"))))
                                )
                    )
                ))
        );
    }
}
