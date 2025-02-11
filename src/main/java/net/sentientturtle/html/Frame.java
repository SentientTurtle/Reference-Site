package net.sentientturtle.html;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.page.PageKind;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.sentientturtle.html.HTML.*;

/// HTML Website "page" without header/footer/sidebar; For use in iframes
public abstract non-sealed class Frame implements Document, HTML {
    /**
     * @return The {@link PageKind} of this page
     */
    public abstract PageKind getPageKind();

    ///  @return Page description (shown in opengraph metadata)
    public abstract @Nullable String description();

    /// @return The icon for this page
    public abstract @Nullable ResourceLocation getIcon(HtmlContext context);

    protected @Nullable String getCSS(HtmlContext context) {
        return null;
    }

    protected List<HTML> headEntries(HtmlContext context) {
        return List.of();
    }

    protected abstract HTML getContent(HtmlContext context);

    /// Elements cannot be rendered to HTML without a {@link HtmlContext} and so do not support toString();
    ///
    /// Use {@link #renderTo(HtmlContext)} instead
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Elements do not support toString; use HTML#renderTo instead");
    }

    @Override
    public void renderTo(HtmlContext context) throws RenderingException {
        var head = HEAD().content(
            META().attribute("charset", "UTF-8"),
            META().attribute("name", "viewport").attribute("content", "width=device-width, initial-scale=1"),
            TITLE(this.title()),
            LINK().attribute("rel", "stylesheet").attribute("href", c -> c.pathTo("stylesheet.css")),
            LINK().attribute("rel", "icon").attribute("href", c -> ResourceLocation.file("bookicon.png").getURI(c))
        );

        head.content(META().attribute("property", "og:site_name").attribute("content", Main.WEBSITE_NAME));
        head.content(META().attribute("property", "og:title").attribute("content", this.name()));
        String page_description = this.description();
        if (page_description != null) {
            head.content(META().attribute("property", "og:description").attribute("content", page_description));
        }
        ResourceLocation icon = this.getIcon(context);
        if (icon != null) {
            head.content(META().attribute("property", "og:image").attribute("content", icon.getURI(context, true, Main.DEPLOYMENT_URL)));
        }

        for (HTML entry : headEntries(context)) {
            head.content(entry);
        }

        head.content(HTML.RAW("<script type='module' src='" + HTMLUtil.escapeAttributeValue(context.pathTo("script.js")) + "'></script>"));

        Element content = DOCUMENT_ROOT().content(
            head,
            BODY().content(getContent(context))
        );
        try {
            String head_font = "@font-face {" +
                               "   font-family: 'Electrolize';" +
                               "   src: url('" + ResourceLocation.file("font/Electrolize.woff2").getURI(context, true) + "') format('woff2');" +
                               "   font-display: swap;" +
                               "}";

            String roman_numeral_font = "@font-face {" +
                                        "   font-family: 'RomanNumeral';" +
                                        "   src: url('" + ResourceLocation.file("font/RomanNumerals.woff2").getURI(context, true) + "') format('woff2');" +
                                        "   font-display: swap;" +
                                        "}";


            context.registerCSS(
                head_font + "\n" +
                roman_numeral_font + "\n" +
                GLOBAL_CSS + PAGE_CSS
            );

            String css = this.getCSS(context);
            if (css != null) {
                context.registerCSS(css);
            }

            content.renderTo(context);
        } catch (Exception e) {
            throw new RenderingException("Error in: " + this.getClass().getName() + " " + this.name(), e);
        }
    }

    protected static final String PAGE_CSS = """
        html {
            margin: 0;
            padding: 0;
            height: 100%;
        }
        
        body {
            margin: 0;
            padding: 0;
            background-color: var(--colour-theme-minor-bg);
            color: var(--colour-text);
        
            font-family: sans-serif; /* Generic sans-serif */
        }
        
        .body_grid {
            display: grid;
            grid-template-columns: 12rem 1fr;
            grid-template-rows: 4rem 1fr auto;
            column-gap: 0;
            row-gap: 0;
        }
        
        @media (max-width: 47.5rem) {
            .body_grid {
                display: flex;
                flex-direction: column;
            }
        }
        
        #header {
            grid-area: 1 / 1 / 2 / 3;
            min-height: 4rem;
            width: 100%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: var(--border-size) solid var(--colour-theme-minor-border);
        }
        
        #header_text {
            font-size: 2.25rem;
        }
        
        @media (max-width: 47.5rem) {
            #header {
                flex-wrap: wrap;
                justify-content: space-around;
            }
        
            #header_text {
                font-size: 1rem;
            }
        }
        
        .header_span {
            display: flex;
            align-items: center;
        }
        
        .header_icon {
            width: 4rem;
            height: 4rem;
        }
        
        .header_search {
            margin: 1rem;
        }
        
        #sidebar {
            grid-area: 2 / 1 / 4 / 2;
            padding-top: 0.5rem;
        }
        
        #content {
            grid-area: 2 / 2 / 3 / 3;
            display: flex;
            flex-direction: row;
            flex-wrap: wrap;
            align-items: flex-start;
            justify-content: center;
            padding: 0.5rem;
            gap: 0.5rem;
        }
        
        #footer {
            grid-area: 3 / 2 / 4 / 3;
            padding: 1.5rem;
            display: flex;
            flex-direction: column;
            align-items: center;
            font-size: 0.7rem;
        }
        """;
    protected static final String GLOBAL_CSS = """        
        :root {
            --border-size: 1px;
        
            --colour-text: #bbbbbb;
        
            --colour-theme-bg: #191D2E;
            --colour-theme-border: #485795;
            --colour-theme-minor-bg: #0A0A0A; /* Always this colour */
            --colour-theme-minor-border: #2F375C;
            --colour-theme-major-bg: #353D5F;
            --colour-theme-major-border: #5C70C3;
            --colour-theme-highlight-bg: #48537A;
            --colour-theme-highlight-border: #6A7ED1;
        
            image-rendering: auto;
            font-size: 100%;
        }
        
        @media (min-width: 2560px) {
            :root {
                font-size: 125%;
            }
        }
        
        
        @media (min-width: 3840px) {
            :root {
                font-size: 200%;
            }
        }
        
        /* Colour classes */
        
        .colour_theme { background-color: var(--colour-theme-bg); border: 1px solid var(--colour-theme-border); }
        .colour_theme_bg { background-color: var(--colour-theme-bg); }
        .colour_theme_border { border: 1px solid var(--colour-theme-border); }
        .colour_theme_border_bg { background-color: var(--colour-theme-border); }
        .colour_theme_minor { background-color: var(--colour-theme-minor-bg); border: 1px solid var(--colour-theme-minor-border); }
        .colour_theme_minor_bg { background-color: var(--colour-theme-minor-bg); }
        .colour_theme_minor_border { border: 1px solid var(--colour-theme-minor-border); }
        .colour_theme_minor_border_bg { background-color: var(--colour-theme-minor-border); }
        .colour_theme_major { background-color: var(--colour-theme-major-bg); border: 1px solid var(--colour-theme-major-border); }
        .colour_theme_major_bg { background-color: var(--colour-theme-major-bg); }
        .colour_theme_major_border { border: 1px solid var(--colour-theme-major-border);  }
        .colour_theme_major_border_bg { background-color: var(--colour-theme-major-border); }
        .colour_theme_highlight { background-color: var(--colour-theme-highlight-bg); border: 1px solid var(--colour-theme-highlight-border); }
        .colour_theme_highlight_bg { background-color: var(--colour-theme-highlight-bg); }
        .colour_theme_highlight_border { border: 1px solid var(--colour-theme-highlight-border); }
        .colour_theme_highlight_border_bg { background-color: var(--colour-theme-highlight-border); }
        
        .colour_amarr { background-color: #35280C; border: 1px solid #9E7C36; }
        .colour_amarr_bg { background-color: #35280C; }
        .colour_amarr_border { border: 1px solid #9E7C36 }
        .colour_amarr_border_bg { background-color: #9E7C36; }
        .colour_amarr_minor { background-color: #0A0A0A; border: 1px solid #624C1E; }
        .colour_amarr_minor_bg { background-color: #0A0A0A; }
        .colour_amarr_minor_border { border: 1px solid #624C1E }
        .colour_amarr_minor_border_bg { background-color: #624C1E; }
        .colour_amarr_major { background-color: #6C5322; border: 1px solid #9A7934; }
        .colour_amarr_major_bg { background-color: #6C5322; }
        .colour_amarr_major_border { border: 1px solid #9A7934 }
        .colour_amarr_major_border_bg { background-color: #9A7934; }
        .colour_amarr_highlight { background-color: #695121; border: 1px solid #A9843A; }
        .colour_amarr_highlight_bg { background-color: #695121; }
        .colour_amarr_highlight_border { border: 1px solid #A9843A }
        .colour_amarr_highlight_border_bg { background-color: #A9843A; }
        .colour_caldari { background-color: #1F2B38; border: 1px solid #6887A9; }
        .colour_caldari_bg { background-color: #1F2B38; }
        .colour_caldari_border { border: 1px solid #6887A9 }
        .colour_caldari_border_bg { background-color: #6887A9; }
        .colour_caldari_minor { background-color: #0A0A0A; border: 1px solid #3F5469; }
        .colour_caldari_minor_bg { background-color: #0A0A0A; }
        .colour_caldari_minor_border { border: 1px solid #3F5469 }
        .colour_caldari_minor_border_bg { background-color: #3F5469; }
        .colour_caldari_major { background-color: #435970; border: 1px solid #6584A5; }
        .colour_caldari_major_bg { background-color: #435970; }
        .colour_caldari_major_border { border: 1px solid #6584A5 }
        .colour_caldari_major_border_bg { background-color: #6584A5; }
        .colour_caldari_highlight { background-color: #41576D; border: 1px solid #6F91B4; }
        .colour_caldari_highlight_bg { background-color: #41576D; }
        .colour_caldari_highlight_border { border: 1px solid #6F91B4 }
        .colour_caldari_highlight_border_bg { background-color: #6F91B4; }
        .colour_gallente { background-color: #142F28; border: 1px solid #4C9481; }
        .colour_gallente_bg { background-color: #142F28; }
        .colour_gallente_border { border: 1px solid #4C9481 }
        .colour_gallente_border_bg { background-color: #4C9481; }
        .colour_gallente_minor { background-color: #0A0A0A; border: 1px solid #2D5C50; }
        .colour_gallente_minor_bg { background-color: #0A0A0A; }
        .colour_gallente_minor_border { border: 1px solid #2D5C50 }
        .colour_gallente_minor_border_bg { background-color: #2D5C50; }
        .colour_gallente_major { background-color: #2F6053; border: 1px solid #4A907E; }
        .colour_gallente_major_bg { background-color: #2F6053; }
        .colour_gallente_major_border { border: 1px solid #4A907E }
        .colour_gallente_major_border_bg { background-color: #4A907E; }
        .colour_gallente_highlight { background-color: #2E5D51; border: 1px solid #529E8A; }
        .colour_gallente_highlight_bg { background-color: #2E5D51; }
        .colour_gallente_highlight_border { border: 1px solid #529E8A }
        .colour_gallente_highlight_border_bg { background-color: #529E8A; }
        .colour_minmatar { background-color: #40211F; border: 1px solid #AA625C; }
        .colour_minmatar_bg { background-color: #40211F; }
        .colour_minmatar_border { border: 1px solid #AA625C }
        .colour_minmatar_border_bg { background-color: #AA625C; }
        .colour_minmatar_minor { background-color: #0A0A0A; border: 1px solid #6A3B37; }
        .colour_minmatar_minor_bg { background-color: #0A0A0A; }
        .colour_minmatar_minor_border { border: 1px solid #6A3B37 }
        .colour_minmatar_minor_border_bg { background-color: #6A3B37; }
        .colour_minmatar_major { background-color: #7F4843; border: 1px solid #A66059; }
        .colour_minmatar_major_bg { background-color: #7F4843; }
        .colour_minmatar_major_border { border: 1px solid #A66059 }
        .colour_minmatar_major_border_bg { background-color: #A66059; }
        .colour_minmatar_highlight { background-color: #7B4641; border: 1px solid #B66962; }
        .colour_minmatar_highlight_bg { background-color: #7B4641; }
        .colour_minmatar_highlight_border { border: 1px solid #B66962 }
        .colour_minmatar_highlight_border_bg { background-color: #B66962; }
        .colour_ore { background-color: #2D2B06; border: 1px solid #8F8826; }
        .colour_ore_bg { background-color: #2D2B06; }
        .colour_ore_border { border: 1px solid #8F8826 }
        .colour_ore_border_bg { background-color: #8F8826; }
        .colour_ore_minor { background-color: #0A0A0A; border: 1px solid #595414; }
        .colour_ore_minor_bg { background-color: #0A0A0A; }
        .colour_ore_minor_border { border: 1px solid #595414 }
        .colour_ore_minor_border_bg { background-color: #595414; }
        .colour_ore_major { background-color: #5D5916; border: 1px solid #8C8525; }
        .colour_ore_major_bg { background-color: #5D5916; }
        .colour_ore_major_border { border: 1px solid #8C8525 }
        .colour_ore_major_border_bg { background-color: #8C8525; }
        .colour_ore_highlight { background-color: #5B5615; border: 1px solid #999229; }
        .colour_ore_highlight_bg { background-color: #5B5615; }
        .colour_ore_highlight_border { border: 1px solid #999229 }
        .colour_ore_highlight_border_bg { background-color: #999229; }
        .colour_guristas { background-color: #2F291F; border: 1px solid #93846A; }
        .colour_guristas_bg { background-color: #2F291F; }
        .colour_guristas_border { border: 1px solid #93846A }
        .colour_guristas_border_bg { background-color: #93846A; }
        .colour_guristas_minor { background-color: #0A0A0A; border: 1px solid #5B5140; }
        .colour_guristas_minor_bg { background-color: #0A0A0A; }
        .colour_guristas_minor_border { border: 1px solid #5B5140 }
        .colour_guristas_minor_border_bg { background-color: #5B5140; }
        .colour_guristas_major { background-color: #605644; border: 1px solid #8F8067; }
        .colour_guristas_major_bg { background-color: #605644; }
        .colour_guristas_major_border { border: 1px solid #8F8067 }
        .colour_guristas_major_border_bg { background-color: #8F8067; }
        .colour_guristas_highlight { background-color: #5D5342; border: 1px solid #9D8D71; }
        .colour_guristas_highlight_bg { background-color: #5D5342; }
        .colour_guristas_highlight_border { border: 1px solid #9D8D71 }
        .colour_guristas_highlight_border_bg { background-color: #9D8D71; }
        .colour_sansha { background-color: #292C13; border: 1px solid #868D4B; }
        .colour_sansha_bg { background-color: #292C13; }
        .colour_sansha_border { border: 1px solid #868D4B }
        .colour_sansha_border_bg { background-color: #868D4B; }
        .colour_sansha_minor { background-color: #0A0A0A; border: 1px solid #52572C; }
        .colour_sansha_minor_bg { background-color: #0A0A0A; }
        .colour_sansha_minor_border { border: 1px solid #52572C }
        .colour_sansha_minor_border_bg { background-color: #52572C; }
        .colour_sansha_major { background-color: #565A2E; border: 1px solid #838949; }
        .colour_sansha_major_bg { background-color: #565A2E; }
        .colour_sansha_major_border { border: 1px solid #838949 }
        .colour_sansha_major_border_bg { background-color: #838949; }
        .colour_sansha_highlight { background-color: #53582D; border: 1px solid #8F9651; }
        .colour_sansha_highlight_bg { background-color: #53582D; }
        .colour_sansha_highlight_border { border: 1px solid #8F9651 }
        .colour_sansha_highlight_border_bg { background-color: #8F9651; }
        .colour_bloodraiders { background-color: #4F1710; border: 1px solid #B03F30; }
        .colour_bloodraiders_bg { background-color: #4F1710; }
        .colour_bloodraiders_border { border: 1px solid #B03F30 }
        .colour_bloodraiders_border_bg { background-color: #B03F30; }
        .colour_bloodraiders_minor { background-color: #0A0A0A; border: 1px solid #6E241B; }
        .colour_bloodraiders_minor_bg { background-color: #0A0A0A; }
        .colour_bloodraiders_minor_border { border: 1px solid #6E241B }
        .colour_bloodraiders_minor_border_bg { background-color: #6E241B; }
        .colour_bloodraiders_major { background-color: #993529; border: 1px solid #AC3D2F; }
        .colour_bloodraiders_major_bg { background-color: #993529; }
        .colour_bloodraiders_major_border { border: 1px solid #AC3D2F }
        .colour_bloodraiders_major_border_bg { background-color: #AC3D2F; }
        .colour_bloodraiders_highlight { background-color: #953427; border: 1px solid #BC4334; }
        .colour_bloodraiders_highlight_bg { background-color: #953427; }
        .colour_bloodraiders_highlight_border { border: 1px solid #BC4334 }
        .colour_bloodraiders_highlight_border_bg { background-color: #BC4334; }
        .colour_angelcartel { background-color: #2D2927; border: 1px solid #8E847E; }
        .colour_angelcartel_bg { background-color: #2D2927; }
        .colour_angelcartel_border { border: 1px solid #8E847E }
        .colour_angelcartel_border_bg { background-color: #8E847E; }
        .colour_angelcartel_minor { background-color: #0A0A0A; border: 1px solid #58524D; }
        .colour_angelcartel_minor_bg { background-color: #0A0A0A; }
        .colour_angelcartel_minor_border { border: 1px solid #58524D }
        .colour_angelcartel_minor_border_bg { background-color: #58524D; }
        .colour_angelcartel_major { background-color: #5C5651; border: 1px solid #8B817B; }
        .colour_angelcartel_major_bg { background-color: #5C5651; }
        .colour_angelcartel_major_border { border: 1px solid #8B817B }
        .colour_angelcartel_major_border_bg { background-color: #8B817B; }
        .colour_angelcartel_highlight { background-color: #5A534F; border: 1px solid #988E86; }
        .colour_angelcartel_highlight_bg { background-color: #5A534F; }
        .colour_angelcartel_highlight_border { border: 1px solid #988E86 }
        .colour_angelcartel_highlight_border_bg { background-color: #988E86; }
        .colour_serpentis { background-color: #1D2E1E; border: 1px solid #679369; }
        .colour_serpentis_bg { background-color: #1D2E1E; }
        .colour_serpentis_border { border: 1px solid #679369 }
        .colour_serpentis_border_bg { background-color: #679369; }
        .colour_serpentis_minor { background-color: #0A0A0A; border: 1px solid #3E5B3F; }
        .colour_serpentis_minor_bg { background-color: #0A0A0A; }
        .colour_serpentis_minor_border { border: 1px solid #3E5B3F }
        .colour_serpentis_minor_border_bg { background-color: #3E5B3F; }
        .colour_serpentis_major { background-color: #405E41; border: 1px solid #659066; }
        .colour_serpentis_major_bg { background-color: #405E41; }
        .colour_serpentis_major_border { border: 1px solid #659066 }
        .colour_serpentis_major_border_bg { background-color: #659066; }
        .colour_serpentis_highlight { background-color: #3E5B40; border: 1px solid #6E9E70; }
        .colour_serpentis_highlight_bg { background-color: #3E5B40; }
        .colour_serpentis_highlight_border { border: 1px solid #6E9E70 }
        .colour_serpentis_highlight_border_bg { background-color: #6E9E70; }
        .colour_soe { background-color: #2A2A2A; border: 1px solid #878787; }
        .colour_soe_bg { background-color: #2A2A2A; }
        .colour_soe_border { border: 1px solid #878787 }
        .colour_soe_border_bg { background-color: #878787; }
        .colour_soe_minor { background-color: #0A0A0A; border: 1px solid #535353; }
        .colour_soe_minor_bg { background-color: #0A0A0A; }
        .colour_soe_minor_border { border: 1px solid #535353 }
        .colour_soe_minor_border_bg { background-color: #535353; }
        .colour_soe_major { background-color: #575757; border: 1px solid #848484; }
        .colour_soe_major_bg { background-color: #575757; }
        .colour_soe_major_border { border: 1px solid #848484 }
        .colour_soe_major_border_bg { background-color: #848484; }
        .colour_soe_highlight { background-color: #545454; border: 1px solid #909090; }
        .colour_soe_highlight_bg { background-color: #545454; }
        .colour_soe_highlight_border { border: 1px solid #909090 }
        .colour_soe_highlight_border_bg { background-color: #909090; }
        .colour_mordus { background-color: #272A32; border: 1px solid #7E869B; }
        .colour_mordus_bg { background-color: #272A32; }
        .colour_mordus_border { border: 1px solid #7E869B }
        .colour_mordus_border_bg { background-color: #7E869B; }
        .colour_mordus_minor { background-color: #0A0A0A; border: 1px solid #4D5261; }
        .colour_mordus_minor_bg { background-color: #0A0A0A; }
        .colour_mordus_minor_border { border: 1px solid #4D5261 }
        .colour_mordus_minor_border_bg { background-color: #4D5261; }
        .colour_mordus_major { background-color: #525766; border: 1px solid #7B8398; }
        .colour_mordus_major_bg { background-color: #525766; }
        .colour_mordus_major_border { border: 1px solid #7B8398 }
        .colour_mordus_major_border_bg { background-color: #7B8398; }
        .colour_mordus_highlight { background-color: #4F5463; border: 1px solid #878FA6; }
        .colour_mordus_highlight_bg { background-color: #4F5463; }
        .colour_mordus_highlight_border { border: 1px solid #878FA6 }
        .colour_mordus_highlight_border_bg { background-color: #878FA6; }
        .colour_triglavian { background-color: #5C0000; border: 1px solid #B10000; }
        .colour_triglavian_bg { background-color: #5C0000; }
        .colour_triglavian_border { border: 1px solid #B10000 }
        .colour_triglavian_border_bg { background-color: #B10000; }
        .colour_triglavian_minor { background-color: #0A0A0A; border: 1px solid #6F0000; }
        .colour_triglavian_minor_bg { background-color: #0A0A0A; }
        .colour_triglavian_minor_border { border: 1px solid #6F0000 }
        .colour_triglavian_minor_border_bg { background-color: #6F0000; }
        .colour_triglavian_major { background-color: #B20000; border: 1px solid #AD0000; }
        .colour_triglavian_major_bg { background-color: #B20000; }
        .colour_triglavian_major_border { border: 1px solid #AD0000 }
        .colour_triglavian_major_border_bg { background-color: #AD0000; }
        .colour_triglavian_highlight { background-color: #AE0000; border: 1px solid #BD0000; }
        .colour_triglavian_highlight_bg { background-color: #AE0000; }
        .colour_triglavian_highlight_border { border: 1px solid #BD0000 }
        .colour_triglavian_highlight_border_bg { background-color: #BD0000; }
        .colour_edencom { background-color: #202848; border: 1px solid #6376C4; }
        .colour_edencom_bg { background-color: #202848; }
        .colour_edencom_border { border: 1px solid #6376C4 }
        .colour_edencom_border_bg { background-color: #6376C4; }
        .colour_edencom_minor { background-color: #0A0A0A; border: 1px solid #3C487B; }
        .colour_edencom_minor_bg { background-color: #0A0A0A; }
        .colour_edencom_minor_border { border: 1px solid #3C487B }
        .colour_edencom_minor_border_bg { background-color: #3C487B; }
        .colour_edencom_major { background-color: #46548E; border: 1px solid #6173C0; }
        .colour_edencom_major_bg { background-color: #46548E; }
        .colour_edencom_major_border { border: 1px solid #6173C0 }
        .colour_edencom_major_border_bg { background-color: #6173C0; }
        .colour_edencom_highlight { background-color: #44518A; border: 1px solid #6A7ED1; }
        .colour_edencom_highlight_bg { background-color: #44518A; }
        .colour_edencom_highlight_border { border: 1px solid #6A7ED1 }
        .colour_edencom_highlight_border_bg { background-color: #6A7ED1; }
        .colour_concord { background-color: #1F2B38; border: 1px solid #6887AA; }
        .colour_concord_bg { background-color: #1F2B38; }
        .colour_concord_border { border: 1px solid #6887AA }
        .colour_concord_border_bg { background-color: #6887AA; }
        .colour_concord_minor { background-color: #0A0A0A; border: 1px solid #3F536A; }
        .colour_concord_minor_bg { background-color: #0A0A0A; }
        .colour_concord_minor_border { border: 1px solid #3F536A }
        .colour_concord_minor_border_bg { background-color: #3F536A; }
        .colour_concord_major { background-color: #435971; border: 1px solid #6584A6; }
        .colour_concord_major_bg { background-color: #435971; }
        .colour_concord_major_border { border: 1px solid #6584A6 }
        .colour_concord_major_border_bg { background-color: #6584A6; }
        .colour_concord_highlight { background-color: #41566E; border: 1px solid #6F90B5; }
        .colour_concord_highlight_bg { background-color: #41566E; }
        .colour_concord_highlight_border { border: 1px solid #6F90B5 }
        .colour_concord_highlight_border_bg { background-color: #6F90B5; }
        .colour_sotc { background-color: #292B20; border: 1px solid #868B6D; }
        .colour_sotc_bg { background-color: #292B20; }
        .colour_sotc_border { border: 1px solid #868B6D }
        .colour_sotc_border_bg { background-color: #868B6D; }
        .colour_sotc_minor { background-color: #0A0A0A; border: 1px solid #525642; }
        .colour_sotc_minor_bg { background-color: #0A0A0A; }
        .colour_sotc_minor_border { border: 1px solid #525642 }
        .colour_sotc_minor_border_bg { background-color: #525642; }
        .colour_sotc_major { background-color: #555945; border: 1px solid #82886A; }
        .colour_sotc_major_bg { background-color: #555945; }
        .colour_sotc_major_border { border: 1px solid #82886A }
        .colour_sotc_major_border_bg { background-color: #82886A; }
        .colour_sotc_highlight { background-color: #535643; border: 1px solid #8F9474; }
        .colour_sotc_highlight_bg { background-color: #535643; }
        .colour_sotc_highlight_border { border: 1px solid #8F9474 }
        .colour_sotc_highlight_border_bg { background-color: #8F9474; }
        .colour_deathless { background-color: #4D1900; border: 1px solid #B04300; }
        .colour_deathless_bg { background-color: #4D1900; }
        .colour_deathless_border { border: 1px solid #B04300 }
        .colour_deathless_border_bg { background-color: #B04300; }
        .colour_deathless_minor { background-color: #0A0A0A; border: 1px solid #6E2700; }
        .colour_deathless_minor_bg { background-color: #0A0A0A; }
        .colour_deathless_minor_border { border: 1px solid #6E2700 }
        .colour_deathless_minor_border_bg { background-color: #6E2700; }
        .colour_deathless_major { background-color: #973900; border: 1px solid #AC4200; }
        .colour_deathless_major_bg { background-color: #973900; }
        .colour_deathless_major_border { border: 1px solid #AC4200 }
        .colour_deathless_major_border_bg { background-color: #AC4200; }
        .colour_deathless_highlight { background-color: #933700; border: 1px solid #BC4900; }
        .colour_deathless_highlight_bg { background-color: #933700; }
        .colour_deathless_highlight_border { border: 1px solid #BC4900 }
        .colour_deathless_highlight_border_bg { background-color: #BC4900; }
        
        /* Global CSS */
        
        .font_header {
            font-family: 'Electrolize', sans-serif;
        }
        
        .font_text {
            font-family: sans-serif; /* Generic sans-serif */
        }
        
        .font_roman_numeral {
            font-family: 'RomanNumeral', sans-serif;
        }
        
        .no_break {
            white-space: nowrap;
        }
        
        header {
            font-size: 1.25em;
        }
        
        a:link {
            color: #B3B3D0;
            text-decoration: none;
        }
        
        a:visited {
            color: #B3B3D0;
            text-decoration: none;
        }
        
        a:hover {
            color: #80a480;
            text-decoration: none;
        }
        
        a:active {
            color: #80dc80;
            text-decoration: none;
        }
        
        button {
            border: 1px solid var(--colour-theme-border);
            background-color: var(--colour-theme-bg);
            color: var(--colour-text);
        }
        
        button:hover {
            border: 1px solid var(--colour-theme-highlight-border);
            background-color: var(--colour-theme-highlight-bg);
            cursor: pointer;
        }
        
        select {
            border: 1px solid var(--colour-theme-border);
            background-color: var(--colour-theme-bg);
            color: var(--colour-text);
        }
        
        /* Alternate pre behaviour */
        pre {
            white-space: pre-wrap;
        }
        
        table, tr, td {
            font-size: inherit;
            vertical-align: middle;
        }
        
        /* This fixes a td height issue */
        td img {
            display: block;
        }
        
        fieldset {
            border-color: var(--colour-theme-border);
        }
        
        input[type=text] {
            border: 2px solid #bbbbbb;
            border-radius: 4px;
            background-color: #2c2d2e;
            color: #bbbbbb;
            padding: 5px;
        }
        
        input[type='checkbox'] {
            accent-color: var(--colour-theme-border);
        }
        
        input[type='radio'] {
            accent-color: var(--colour-theme-border);
            margin: 0.25rem;
        }
        
        /* EVE Clip */
        .eve_clip_top {
            clip-path: polygon(0% 1em, 1em 0%, calc(100% - 1em) 0%, calc(100% - 1em) 0%, 100% 1em, 100% 100%, 0% 100%);
        }
        
        .eve_clip_top_right {
            clip-path: polygon(0% 0%, calc(100% - 1em) 0%, 100% 1em, 100% 100%, 0% 100%);
        }
        
        .eve_clip_top_left {
            clip-path: polygon(0% 1em, 1em 0%, 100% 0%, 100% 100%, 0% 100%);
        }
        
        .eve_clip_bottom {
            clip-path: polygon(0% 0%, 100% 0%, 100% calc(100% - 1em), calc(100% - 1rem) 100%, 1em 100%, 0% calc(100% - 1em));
        }
        .eve_clip_bottom_right {
            clip-path: polygon(0% 0%, 100% 0%, 100% calc(100% - 1em), calc(100% - 1em) 100%, 0% 100%);
        }
        .eve_clip_bottom_left {
            clip-path: polygon(0% 0%, 100% 0%, 100% 100%, 1em 100%, 0% calc(100% - 1em));
        }
        
        @media (max-width: 47.5rem) {
            .eve_clip_mobile_unset {
                clip-path: unset;
            }
        }
        """;
}
