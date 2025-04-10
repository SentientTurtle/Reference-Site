package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.ItemDescription;
import net.sentientturtle.nee.components.ItemTree;
import net.sentientturtle.nee.components.ItemTitle;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.MarketGroup;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import static net.sentientturtle.html.HTML.DIV;
import static net.sentientturtle.html.HTML.TEXT;

/**
 * Page for a {@link MarketGroup}
 */
public class MarketGroupPage extends Page {
    private final MarketGroup marketGroup;

    public MarketGroupPage(MarketGroup marketGroup) {
        this.marketGroup = marketGroup;
    }

    @Override
    public String name() {
        return marketGroup.name;
    }

    @Override
    public @Nullable String description() {
        return marketGroup.description;
    }

    @Override
    public String filename() {
        return marketGroup.marketGroupID + "-" + marketGroup.name;
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.MARKET_GROUP;
    }

    @Override
    public @Nullable ResourceLocation getIcon(HtmlContext context) {
        return null;
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .market_group_page {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 1rem;
                width: 100%;
            }
            """;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        boolean hasItems;
        if (context.sde.getMarketGroupTypeMap().getOrDefault(marketGroup.marketGroupID, Set.of()).size() > 0) {
            hasItems = true;
        } else {
            hasItems = context.sde.getMarketGroupChildMap()
                .getOrDefault(marketGroup.marketGroupID, Set.of()).stream()
                .anyMatch(childGroup ->
                    context.sde.getMarketGroupTypeMap().getOrDefault(childGroup.marketGroupID, Set.of()).size() > 0
                    && context.sde.getMarketGroupChildMap().getOrDefault(childGroup.marketGroupID, Set.of()).size() > 0
                );
        }

        ItemTree.Entry[] entries = Stream.concat(
                context.sde.getMarketGroupChildMap()
                    .getOrDefault(marketGroup.marketGroupID, Set.of())
                    .stream()
                    .sorted(Comparator.comparing(m -> m.name))
                    .map(group -> entryForGroup(group, context.sde, hasItems)),
                Stream.ofNullable(groupForItems(context.sde.getMarketGroupTypeMap().get(marketGroup.marketGroupID), context.sde))
                    .map(treeGroup -> new ItemTree.Entry("Items", new ItemTree.Group(HTML.empty(), treeGroup.pages())))
            )
            .toArray(ItemTree.Entry[]::new);

        var container = DIV("market_group_page");
        if (marketGroup.parentGroupID != null) {
            container.content(new Component("market_group_page_parent colour_theme_minor") {
                @Override
                protected HTML[] getContent(HtmlContext context) {
                    var parentGroup = context.sde.getMarketGroups().get(marketGroup.parentGroupID);
                    return new HTML[]{
                        new PageLink(
                            new MarketGroupPage(parentGroup),
                            "[" + parentGroup.name + "]"
                        ).className("font_header market_group_page_parent_text"),
                    };
                }

                @Override
                protected String getCSS() {
                    return """
                        .market_group_page_parent {
                            min-height: 64px;
                            display: flex;
                            align-items: center;
                        }
                        
                        .market_group_page_parent_text {
                            font-size: 1.5em;
                            margin-inline: 10px;
                        }
                        
                        .market_group_page > .item_tree {
                            align-items: center;
                        }
                        
                        .market_group_page .item_tree_row {
                            justify-content: center;
                        }
                        """;
                }
            });
        }

        container.content(new ItemTitle(marketGroup.name, null));

        if (marketGroup.description != null && marketGroup.description.trim().length() > 0) {
            container.content(new ItemDescription(TEXT(marketGroup.description)));
        }
        container.content(new ItemTree(entries));
        return container;
    }

    private static ItemTree.@Nullable Group groupForItems(@Nullable Set<Type> types, SDEData data) {
        if (types == null || types.isEmpty()) return null;
        HasPage[] pages = types.stream()
            .sorted(Type.comparator(data))
            .toArray(HasPage[]::new);
        return new ItemTree.Group((HTML) null, pages);
    }

    private static ItemTree.Entry entryForGroup(MarketGroup group, SDEData data, boolean showItems) {
        ItemTree.Group[] treeGroups = Stream.concat(
                data.getMarketGroupChildMap()
                    .getOrDefault(group.marketGroupID, Set.of())
                    .stream()
                    .sorted(Comparator.comparing(m -> m.name))
                    .map(marketGroup -> {
                        Set<MarketGroup> childGroups = data.getMarketGroupChildMap()
                            .getOrDefault(marketGroup.marketGroupID, Set.of());
                        Set<Type> items = data.getMarketGroupTypeMap()
                            .getOrDefault(marketGroup.marketGroupID, Set.of());

                        if (childGroups.size() == 0 && !showItems) {
                            return new ItemTree.Group((HTML) null, marketGroup);
                        } else {
                            HasPage[] pages = Stream.concat(
                                childGroups
                                    .stream()
                                    .sorted(Comparator.comparing(m -> m.name)),
                                items.stream()
                                    .sorted(Type.comparator(data))
                            ).toArray(HasPage[]::new);

                            if (marketGroup.name.equals(group.name)) {
                                return new ItemTree.Group((HTML) null, pages);
                            } else {
                                return new ItemTree.Group(new PageLink(new MarketGroupPage(marketGroup)), pages);
                            }
                        }
                    }),
                Stream.ofNullable(groupForItems(data.getMarketGroupTypeMap().get(group.marketGroupID), data))
            )
            .toArray(ItemTree.Group[]::new);
        return new ItemTree.Entry(group.name, new PageLink(new MarketGroupPage(group)), treeGroups);
    }
}
