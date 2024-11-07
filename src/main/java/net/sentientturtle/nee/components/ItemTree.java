package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.pages.HasPage;
import net.sentientturtle.nee.pages.Page;
import net.sentientturtle.nee.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

import static net.sentientturtle.html.HTML.*;

/// 'Tree' display of items, usually {@link Type} or groups thereof.
public class ItemTree extends Component {
    private final Entry[] entries;

    public ItemTree(Entry... entries) {
        super("item_tree");
        this.entries = entries;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        var html = new ArrayList<HTML>();
        if (this.entries.length > 3) {
            var index = DIV("item_tree_index font_header");
            for (Entry entry : this.entries) {
                index.content(A("#item-tree-" + entry.name, TEXT(entry.name)).className("item_tree_entry colour_theme_major"));
            }
            html.add(index);
        }
        for (Entry entry : this.entries) {
            html.add(
                DIV("item_tree_header_border eve_clip_top colour_theme_border_bg")
                    .content(DIV("item_tree_header font_header eve_clip_top colour_theme_bg", context.ids.tryID("item-tree-" + entry.name)).content(entry.header))
            );

            var entryContainer = DIV("item_tree_row");
            for (Group group : entry.groups) {
                var groupContainer = DIV("item_tree_group colour_theme_minor");
                if (group.groupName != null) {
                    groupContainer.content(HEADER("font_header").content(group.groupName));
                }

                Element shipContainer = DIV("item_tree_row");
                for (HasPage hasPage : group.pages) {
                    Page page = hasPage.getPage();
                    ResourceLocation icon = page.getIcon(context);
                    if (icon != null) {
                        shipContainer.content(DIV("item_tree_entry colour_theme").content(
                            IMG(icon, null, 32).className("item_tree_icon"),
                            new PageLink(page)
                        ));
                    } else {
                        shipContainer.content(DIV("item_tree_entry colour_theme").content(new PageLink(page)));
                    }
                }
                groupContainer.content(shipContainer);
                entryContainer.content(groupContainer);
            }
            html.add(entryContainer);
        }
        return html.toArray(HTML[]::new);
    }

    @Override
    protected String getCSS() {
        return """
            .item_tree {
                width: 100%;
                display: flex;
                flex-direction: column;
                align-items: flex-start;
                gap: 0.5rem;
            }
            
            .item_tree_index {
                display: flex;
                gap: 1rem;
                margin-bottom: 2rem;
                justify-content: center;
                flex-wrap: wrap;
                align-self: center;
            }
            
            .item_tree_header {
                padding-block: 0.5rem;
                padding-inline: 2rem;
            }
            
            .item_tree_header_border {
                padding: var(--border-size);
                font-size: 1.5rem;
                margin-top: 1rem;
            }
            
            .item_tree_row {
                display: flex;
                flex-wrap: wrap;
                align-items: stretch;
                flex-grow: 1;
                gap: 0.5rem;
            }
            
            .item_tree_group {
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
                padding: 0.5rem;
            }
            
            .ship_tree_group_row {
                display: flex;
                flex-direction: row;
                flex-wrap: wrap;
                gap: 0.5rem;
            }
            
            .item_tree_column {
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }
            
            .item_tree_entry {
                display: flex;
                flex-direction: row;
                align-items: center;
                gap: 0.5rem;
                padding: 0.5rem;
            }
            
            .item_tree_icon {
                width: 2rem;
                height: 2rem;
            }
            """;
    }

    public record Entry(String name, @NonNull HTML header, Group... groups) {
        public Entry(@NonNull String header, Group... groups) {
            this(header, TEXT(header), groups);
        }
    }

    public record Group(@Nullable HTML groupName, HasPage... pages) {
        public Group(@Nullable String groupName, HasPage... pages) {
            this(groupName != null ? TEXT(groupName) : null, pages);
        }
    }
}
