package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.context.ID;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static net.sentientturtle.html.HTML.DIV;
import static net.sentientturtle.html.HTML.BUTTON;

/// 'Tabbed' content, allowing the user to select between multiple different sections of content on the same page
public class TabBox extends Component {
    private final List<Tab> content;

    public TabBox() {
        this(new ArrayList<>());
    }

    public TabBox(List<Tab> content) {
        super("tab_box");
        this.content = content;
    }

    public TabBox entry(HTML button, HTML content) {
        this.content.add(new Tab(button, content));
        return this;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        ID[][] ids = new ID[this.content.size()][];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = context.nextIDsWithPrefixes("tab_box_button:", "tab_box_container:");
        }

        var bar = DIV("tab_box_bar");
        var tabContents = DIV("tab_box_content");
        List<Tab> tabs = this.content;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            bar.content(BUTTON(i == 0 ? "tab_box_button_selected tab_box_button" : "tab_box_button", ids[i][0]).content(tab.button));

            tabContents.content(
                DIV(i == 0 ? " tab_box_container_selected tab_box_container" : "tab_box_container", ids[i][1])
                    .content(tab.content)
            );
        }

        return new HTML[]{bar, tabContents};
    }

    @Override
    protected String getCSS() {
        return """
            .tab_box {
                display: flex;
                flex-direction: column;
            }
            
            .tab_box_bar {
                display: flex;
                flex-direction: row;
                flex-wrap: wrap;
                justify-content: center;
                gap: 0.25rem;
            }
            
            .tab_box_button {
                font-size: 1rem; /* Button has lower font size */
                background: var(--colour-theme-bg-primary);
                border: var(--border-size) solid var(--colour-theme-border);
                padding: 0;
            }
            
            .tab_box_button:hover {
                background: var(--colour-theme-highlight-bg);
                border: var(--border-size) solid var(--colour-theme-highlight-border);
                cursor: pointer;
            }
            
            .tab_box_button_selected, .tab_box_button_selected:hover, .tab_box_button_selected:active {
                background: var(--colour-theme-major-bg) !important;
                border: var(--border-size) solid var(--colour-theme-major-border) !important;
                cursor: default;
            }
            
            .tab_box_button:active {
                background: var(--colour-theme-minor-bg);
                border: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .tab_box_container {
                visibility: hidden;
                display: none;
            }
            
            .tab_box_container_selected {
                visibility: visible !important;
                display: block !important;
            }
            """;
    }

    @Override
    protected @Nullable String getScript() {
        return """
            for (const button of document.getElementsByClassName("tab_box_button")) {
                button.onclick = function (event) {
                    const button = event.currentTarget;
                    // Do nothing if clicked on an already-selected button
                    if (!button.classList.contains("tab_box_button_selected")) {
                        const id = button.id.split(":")[1];
            
                        for (const selected_button of button.parentElement.parentElement.querySelectorAll(".tab_box_button_selected")) {
                            selected_button.classList.remove("tab_box_button_selected");
                            selected_button.classList.remove("border_2");
                            selected_button.classList.add("border_3");
                        }
            
                        for (const selected_container of button.parentElement.parentElement.querySelectorAll(".tab_box_container_selected")) {
                            selected_container.classList.remove("tab_box_container_selected")
                        }
            
                        const new_button = document.getElementById("tab_box_button:" + id);
                        new_button.classList.add("tab_box_button_selected");
                        document.getElementById("tab_box_container:" + id).classList.add("tab_box_container_selected");
                    }
                }
            }
            """;
    }

    public record Tab(HTML button, HTML content) {
    }
}
