package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static net.sentientturtle.html.HTML.*;

/// Page for search results
public class SettingsPage extends Page {
    @Override
    public String name() {
        return "Settings";
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return "settings";
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .settings_entry {
                display: flex;
                align-items: center;
            }""";
    }

    private static final String[] THEME_ORDER = {"default", "amarr", "caldari", "gallente", "minmatar"};
    @Override
    protected HTML getContent(HtmlContext context) {

        record Entry(int sortIndex, String name, HTML input) {};
        try (Stream<Path> themes = Files.list(Main.RES_FOLDER.resolve("themes"))) {


            return DIV().content(
                HTML.FIELDSET("settings_theme", "Website Theme")
                    .content(
                        themes
                            .map(themeFile -> {
                                String themeName = themeFile.getFileName().toString().split("\\.")[0];
                                String themeDisplayName = themeName.substring(0, 1).toUpperCase() + themeName.substring(1);

                                HTML input = DIV("settings_entry").content(HTML.RAW(String.format(
                                    "<input type='radio' name='site_theme' value='%s' id='site_theme_%s' disabled><label for='site_theme_%s'>%s</label>",
                                    themeName,
                                    themeName,
                                    themeName,
                                    themeDisplayName
                                )));

                                int sortIndex = THEME_ORDER.length;
                                for (int i = 0; i < THEME_ORDER.length; i++) {
                                    if (THEME_ORDER[i].equals(themeName)) {
                                        sortIndex = i;
                                        break;
                                    }
                                }

                                return new Entry(sortIndex, themeName, input);
                            })
                            .sorted(Comparator.comparing(Entry::sortIndex).thenComparing(Entry::name))
                            .map(Entry::input)
                    )
            );
        } catch (IOException e) {
            return ExceptionUtil.sneakyThrow(e);
        }
    }

    @Override
    protected List<HTML> headEntries(HtmlContext context) {
        return List.of(
            HTML.SCRIPT_MODULE("""
                let [_, current_theme] = document.cookie.match(/(?:^|;)\\s*theme\\s*=([^;]+)/) ?? [];
                current_theme = (current_theme ?? "").trim();
                for (const option of document.querySelectorAll("input[name='site_theme']")) {
                    if (option.value === current_theme || (option.value === "default" && current_theme === "")) {
                        option.checked = true;
                    }
                    option.onchange = (e) => {
                        document.cookie = "theme=" + e.currentTarget.value;
                        window.location.replace("./settings_refresh.html");
                    }
                    option.disabled = false;
                }
                """)
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
