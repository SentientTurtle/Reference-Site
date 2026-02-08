package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.components.PageList;
import net.sentientturtle.nee.components.TextBox;
import net.sentientturtle.nee.data.Resource;
import net.sentientturtle.nee.data.datatypes.MarketGroup;
import net.sentientturtle.nee.data.datatypes.Type;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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
            new PageList("Featured pages", getFeaturedPages(context)),
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


    private void addGroupsAndChildren(HashSet<Integer> allowedGroups, Map<Integer, Set<MarketGroup>> marketGroups, int marketGroupID) {
        allowedGroups.add(marketGroupID);
        for (MarketGroup marketGroup : marketGroups.getOrDefault(marketGroupID, Set.of())) {
            addGroupsAndChildren(allowedGroups, marketGroups, marketGroup.marketGroupID);
        }
    }

    // This is a bit messy and slow, but only runs once for the single IndexPage so it's fine.
    // This logic could be replaced by querying the Static Data Export's changelog
    private TypePage[] getFeaturedPages(HtmlContext context) {
        var structures = Stream.concat(
                    context.sde.getCategoryGroups().get(22).stream().map(group -> group.groupID),
                    Stream.of(
                        1657, 1404, 1406,
                        1408, 2017, 2016, 4744
                    )
                )
                .map(context.sde.getGroupTypes()::get)
                .flatMap(Set::stream)
                .filter(type -> type.metaGroupID <= 14 || type.metaGroupID >= 52)    // Exclude abyssal, temp, and cash-shop items
                .sorted(Comparator.<Type>comparingInt(t -> t.typeID).reversed())
                .limit(4);

        var ships = Arrays.stream(ShipTreePage.getCommonShips())
            .mapToObj(context.sde.getTypes()::get)
            .sorted(Comparator.<Type>comparingInt(t -> t.typeID).reversed())
            .limit(4);

        var drones = context.sde.getCategoryGroups().get(18)
            .stream()
            .flatMap(group -> context.sde.getGroupTypes().get(group.groupID).stream())
            .filter(type -> context.sde.getParentTypeMap().getOrDefault(type.typeID, type.typeID) == type.typeID)
            .filter(type -> type.metaGroupID <= 14 || type.metaGroupID >= 52)    // Exclude abyssal, temp, and cash-shop items
            .sorted(Comparator.<Type>comparingInt(t -> t.typeID).reversed())
            .limit(3);

        HashSet<Integer> allowedGroups = new HashSet<>();
        Map<Integer, Set<MarketGroup>> marketGroups = context.sde.getMarketGroupChildMap();
        addGroupsAndChildren(allowedGroups, marketGroups, 9);
        addGroupsAndChildren(allowedGroups, marketGroups, 2202);

        var modules = context.sde.getCategoryGroups().get(7)
            .stream()
            .flatMap(group -> context.sde.getGroupTypes().get(group.groupID).stream())
            .filter(type -> context.sde.getParentTypeMap().getOrDefault(type.typeID, type.typeID) == type.typeID)
            .filter(type -> type.metaGroupID <= 14 || type.metaGroupID >= 52)    // Exclude abyssal, temp, and cash-shop items
            .filter(type -> allowedGroups.contains(type.marketGroupID))
            .sorted(Comparator.<Type>comparingInt(t -> t.typeID).reversed())
            .limit(4);

        return Stream.of(structures, ships, drones, modules)
            .flatMap(Function.identity())
            .sorted(Comparator.<Type>comparingInt(t -> t.typeID).reversed())
            .limit(12)
            .sorted(Comparator.comparingInt(t -> context.sde.getGroups().get(t.groupID).categoryID))
            .map(TypePage::new)
            .toArray(TypePage[]::new);
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.STATIC;
    }

    @Nullable
    @Override
    public Resource getIcon(HtmlContext context) {
        return Resource.file("bookicon.png");
    }
}
