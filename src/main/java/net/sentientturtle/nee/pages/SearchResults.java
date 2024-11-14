package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.Title;
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
            new Title("Search results:", null),
            TABLE().id(context.ids.tryID("search_results_table"))
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
                    function escapeHtml(unsafe) {
                        return unsafe
                            .replace(/&/g, "&amp;")
                            .replace(/</g, "&lt;")
                            .replace(/>/g, "&gt;")
                            .replace(/"/g, "&quot;")
                            .replace(/'/g, "&#039;");
                    }
                    
                    function getURLParameter(name) {
                        return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\\+/g, '%20')) || null;
                    }
                    
                    var query = (getURLParameter('search') ?? "").toLowerCase();
                    
                    document.getElementsByClassName('title_text')[0].innerHTML = 'Search results: ' + query;
                    document.getElementById('search_input').value = escapeHtml(query)
                    
                    if (query.length >= 3) {
                        var results = [];
                        for (var i = 0; i < searchindex.length; i++) {
                            if (searchindex[i].index.includes(query)) {
                                results.push(searchindex[i]);
                            }
                    
                        }
                        results.sort(function (a, b) {
                            return a.name.length - b.name.length;
                        });
                        var table = "";
                        for (var j = 0; j < results.length; j++) {
                            table += "<tr>";
                            table += "<td class='search_results_entry'>"
                            if ('icon' in results[j] && results[j].icon != null) {
                                table += "<img src='" + results[j].icon + "' height='64' width='64'>"
                            }
                            table += "</td><td class='search_results_entry font_header'><span class='search_results_page'><a href='" + results[j].path + "'>" + results[j].name + "</a></span></td>";
                            table += "</tr>";
                        }
                        document.getElementById("search_results_table").innerHTML = table;
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
