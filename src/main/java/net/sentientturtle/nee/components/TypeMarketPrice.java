package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.Resource;
import org.jspecify.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

import static net.sentientturtle.html.HTML.*;

/// Component for type volumes & mass, to be expanded to include ship sizes
public class TypeMarketPrice extends Component {
    private final Type type;

    public TypeMarketPrice(Type type) {
        super("type_market_price colour_theme_minor");
        this.type = type;
    }

    private static final NumberFormat bigFormat = NumberFormat.getInstance(Locale.ENGLISH);
    private static final NumberFormat summaryFormat = NumberFormat.getInstance(Locale.ENGLISH);
    static {
        bigFormat.setMaximumFractionDigits(2);
        bigFormat.setMinimumFractionDigits(2);
        summaryFormat.setMaximumFractionDigits(1);
        summaryFormat.setMinimumFractionDigits(0);
    }
    private String formatNumber(double number) {
        String string = bigFormat.format(number) + " ISK";
        double e = Math.log10(number);
        if (e < 6) {
            return string;
        } else if (e < 9) {
            return string + " (" + summaryFormat.format(number / 1_000_000.0) + "M)";
        } else if (e < 12) {
            return string + " (" + summaryFormat.format(number / 1_000_000_000.0) + "B)";
        } else {
            return string + " (" + summaryFormat.format(number / 1_000_000_000_000.0) + "T)";
        }
    }

    public static boolean appliesTo(Type type, HtmlContext context) {
        return type.marketGroupID != null || context.sde.getTypeLists().get(93).types().contains(type);
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        if (context.sde.getTypeLists().get(93).types().contains(type)) {
            assert type.basePrice != null;

            return new HTML[]{
                TABLE("type_market_price_table font_text").content(
                    TR().content(
                        TD().content(
                            SPAN("type_market_price_span font_text").content(
                                IMG(Resource.file("EVE/Jita Logo.png"), null, 32).className("type_market_price_icon"),
                                TEXT("Jita Market Price:")
                            )
                        ),
                        TD().content(TEXT_ITALICS("PRICE LOADING...").className("type_market_price_price").attribute("market_price_type_id", String.valueOf(type.typeID)))
                    ),
                    TR().content(
                        TD().content(
                            SPAN("type_market_price_span font_text").content(
                                IMG(Resource.fromSharedCache("res:/ui/texture/classes/skills/leveltrained.png", context), null, 32).className("type_market_price_icon"),
                                TEXT("Remote Skill Injection:")
                            )
                        ),
                        TD().content(TEXT_ITALICS(formatNumber(type.basePrice * 1.3)))
                    )
                )
            };
        } else {
            return new HTML[]{
                SPAN("type_market_price_span font_text").content(
                    IMG(Resource.file("EVE/Jita Logo.png"), null, 32).title("Jita 4-4 Trade Hub").className("type_market_price_icon"),
                    TEXT("Jita Market Price:")
                ),
                TEXT_ITALICS("PRICE LOADING...").className("type_market_price_price").attribute("market_price_type_id", String.valueOf(type.typeID))
            };
        }
    }

    @Override
    protected @Nullable String getScript() {
        return """
            if (document.getElementsByClassName("type_market_price_price").length > 0) {
                fetch("../rsc/prices.json")
                        .then(r => r.json())
                        .then(prices => {
                            for (const price_text of document.getElementsByClassName("type_market_price_price")) {
                                const type_id = price_text.getAttribute("market_price_type_id");
                                if (prices[type_id] == null) {
                                    price_text.innerText = "Not available";
                                } else {
                                    const type_price = prices[type_id];
                                    const e = Math.floor(Math.log10(type_price));
                                    let summary;
                                    if (e < 6) {
                                        summary = "";
                                    } else if (e < 9) {
                                        summary = " (" + (type_price / 1_000_000.0).toLocaleString('en-US', { maximumFractionDigits: 1, minimumFractionDigits: 0}) + "M)";
                                    } else if (e < 12) {
                                        summary = " (" + (type_price / 1_000_000_000.0).toLocaleString('en-US', { maximumFractionDigits: 1, minimumFractionDigits: 0}) + "B)";
                                    } else {
                                        summary = " (" + (type_price / 1_000_000_000_000.0).toLocaleString('en-US', { maximumFractionDigits: 1, minimumFractionDigits: 0}) + "T)";
                                    }
                                    price_text.innerText = (type_price.toLocaleString('en-US', { maximumFractionDigits: 2, minimumFractionDigits: 2}) + " ISK" + summary);
                                }
                            }
                        });
            }
            """;
    }

    @Override
    protected String getCSS() {
        return """
            .type_market_price {
              padding: 0.5rem;
              display: flex;
              align-items: center;
              gap: 0.25rem;
            }
            
            .type_market_price_table {
                width: 100%;
                border-collapse: collapse;
            }
            
            .type_market_price_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .type_market_price_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .type_market_price_icon {
                width: 2rem;
                height: 2rem;
            }
            """;
    }
}
