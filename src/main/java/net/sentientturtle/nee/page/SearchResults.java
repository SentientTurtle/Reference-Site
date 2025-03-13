package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.ItemTitle;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.sentientturtle.html.HTML.*;

/// Page for search results
public class SearchResults extends Page {
    @Override
    public String name() {
        return "Search Results";
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return "searchresults";
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .search_results {
                padding: 0.75em;
            }
            
            .search_results_page {
                font-size: 1.25em;
                margin: 5px;
            }
            
            .search_results_entry {
                padding-top: 5px;
            }""";
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        return DIV("search_results_grid").content(
            HTML.RAW("<noscript>⚠ Search requires JavaScript ⚠</noscript>"),
            new ItemTitle("Search results:", null),
            TABLE().id(context.tryID("search_results_table"))
                .content(TR().content(TD().text("Loading...")))   // This table is replaced on update
        );
    }

    @Override
    protected List<HTML> headEntries(HtmlContext context) {
        return List.of(
            HTML.SCRIPT_MODULE(
                "import searchindex from \"/" + ResourceLocation.searchIndex().getURI(context) + "\";\n"
                +
                """
                    const query = (new URLSearchParams(window.location.search).get("search") ?? "").toLowerCase().trim();
                    
                    document.getElementsByClassName('item_title_text')[0].innerHTML = 'Search results: ' + query;
                    document.getElementById('search_input').value = query;
                    
                    if (query.length >= 3) {
                        const results = [];
                        for (var i = 0; i < searchindex.length; i++) {
                            if (searchindex[i].name.toLowerCase().includes(query)) {
                                results.push(searchindex[i]);
                            }
                    
                        }
                        results.sort(function (a, b) {
                            return a.name.length - b.name.length;
                        });
                        const table = document.getElementById("search_results_table");
                        table.innerHTML = "";
                        for (var j = 0; j < results.length; j++) {
                            const row = table.appendChild(document.createElement("tr"));
        
                            const iconTD = row.appendChild(document.createElement("td"));
                            if ('icon' in results[j] && results[j].icon != null) {
                                const img = document.createElement("img");
                                img.src = results[j].icon;
                                img.height = 64;
                                img.width = 64;
                                iconTD.appendChild(img);
                            }
                            const linkElement = row.appendChild(document.createElement("td"))
                                .appendChild(document.createElement("span"))
                                .appendChild(document.createElement("a"));
                    
                            linkElement.href = results[j].path;
                            linkElement.textContent = results[j].name;
                        }
                    } else {
                        document.getElementById("search_results_table").innerHTML = "<div class='font_header'>Search query must be at least 3 characters!</div>";
                    }
                    """
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
        return null;
    }
}
