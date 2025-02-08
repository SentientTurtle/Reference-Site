package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.components.PageList;
import net.sentientturtle.nee.components.TextBox;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import static net.sentientturtle.html.HTML.*;

/// Page for ToS & other legal notices
public class TermsOfServicePage extends Page {
    @Override
    public String name() {
        return "Terms of Service & Privacy Notice";
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return "tos";
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .tos_column {
                display: flex;
                flex-direction: column;
                align-items: flex-start;
                gap: 0.5rem;
                max-width: 48rem;
            }
            """;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return DIV("tos_column").content(
            new TextBox("Terms of Service", PRE().content(TEXT("Automated access to robots.txt is permitted. Automated access to all other files & resources is unauthorized and prohibited."))),
            new TextBox("Privacy Notice", PRE().content(TEXT("IP addresses and user agents are processed for bot detection. No information is retained or logged."))),
            new TextBox("Copyright notice", PRE().content(
                TEXT("EVE Online materials subject to "), A("https://developers.eveonline.com/license-agreement", TEXT("EVE Online Third Party Development License")),
                BR(),
                TEXT(
                    "EVE Online and the EVE logo are the registered trademarks of CCP hf. " +
                    "All rights are reserved worldwide. All other trademarks are the property of their respective owners. " +
                    "EVE Online, the EVE logo, EVE and all associated logos and designs are the intellectual property of CCP hf. " +
                    "All artwork, screenshots, characters, vehicles, storylines, world facts or other recognizable features of the intellectual property relating to these trademarks are likewise the intellectual property of CCP hf."
                )
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
        return null;
    }
}
