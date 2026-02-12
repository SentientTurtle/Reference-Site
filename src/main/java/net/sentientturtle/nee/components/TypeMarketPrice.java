package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.Resource;
import org.jspecify.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

import static net.sentientturtle.html.HTML.*;

/// Component for type volumes & mass, to be expanded to include ship sizes
public class TypeMarketPrice extends Component {
    private final Type type;

    public static final Set<Integer> remoteInjectionSkills = Set.of(
        2403, 2406, 2495, 2505, 3184, 3300, 3301, 3302, 3303, 3304, 3305, 3306, 3307, 3308, 3309, 3310, 3311, 3312, 3315, 3316, 3317, 3318, 3319, 3320, 3321, 3322, 3323, 3324,
        3325, 3326, 3327, 3328, 3329, 3330, 3331, 3332, 3333, 3334, 3335, 3336, 3337, 3338, 3339, 3340, 3341, 3342, 3343, 3344, 3345, 3346, 3347, 3348, 3349, 3350, 3351, 3352,
        3354, 3355, 3356, 3357, 3358, 3359, 3361, 3363, 3368, 3373, 3380, 3385, 3386, 3387, 3388, 3389, 3392, 3393, 3394, 3395, 3396, 3397, 3398, 3400, 3402, 3403, 3405, 3406,
        3408, 3409, 3410, 3411, 3412, 3413, 3416, 3417, 3418, 3419, 3420, 3421, 3422, 3423, 3424, 3425, 3426, 3427, 3428, 3429, 3430, 3431, 3432, 3433, 3434, 3435, 3436, 3437,
        3438, 3439, 3440, 3441, 3442, 3443, 3444, 3446, 3447, 3449, 3450, 3451, 3452, 3453, 3454, 3455, 3456, 3551, 3731, 3732, 4385, 4411, 11082, 11083, 11084, 11207, 11395,
        11433, 11441, 11442, 11443, 11444, 11445, 11446, 11447, 11448, 11449, 11450, 11451, 11452, 11453, 11454, 11455, 11529, 11566, 11569, 11572, 11574, 11579, 11584, 12092,
        12093, 12095, 12096, 12098, 12179, 12189, 12196, 12201, 12202, 12203, 12204, 12205, 12206, 12207, 12208, 12209, 12210, 12211, 12212, 12213, 12214, 12215, 12241, 12305,
        12365, 12366, 12367, 12441, 12442, 12484, 12485, 12486, 12487, 13278, 13279, 16069, 16281, 16591, 16594, 16595, 16596, 16597, 16598, 16622, 17940, 18025, 18580, 19719,
        19759, 19760, 19761, 19766, 19767, 19921, 19922, 20209, 20210, 20211, 20212, 20213, 20312, 20314, 20315, 20327, 20342, 20494, 20495, 20524, 20525, 20526, 20527, 20528,
        20530, 20531, 20532, 20533, 21059, 21071, 21603, 21610, 21611, 21666, 21667, 21668, 21718, 21802, 21803, 22043, 22242, 22536, 22541, 22551, 22552, 22578, 22761, 22806,
        22807, 22808, 22809, 23069, 23566, 23594, 23606, 23618, 23950, 24241, 24242, 24268, 24270, 24311, 24312, 24313, 24314, 24562, 24563, 24568, 24571, 24572, 24606, 24613,
        24624, 24625, 24764, 25233, 25235, 25544, 25718, 25719, 25739, 25810, 25811, 25863, 26224, 26252, 26253, 26254, 26255, 26256, 26257, 26258, 26259, 26260, 26261, 27902,
        27906, 27911, 27936, 28073, 28164, 28374, 28585, 28609, 28615, 28656, 28667, 28879, 28880, 29029, 29637, 30324, 30325, 30327, 30532, 30537, 30538, 30539, 30540, 30544,
        30545, 30546, 30547, 30548, 30549, 30550, 30551, 30552, 30553, 30554, 30650, 30651, 30652, 30653, 30788, 32339, 32435, 32797, 32918, 32999, 33000, 33001, 33002, 33078,
        33091, 33092, 33093, 33094, 33095, 33096, 33097, 33098, 33399, 33407, 33467, 33699, 33856, 34327, 34390, 34533, 35680, 35685, 37615, 37796, 37797, 37798, 37799, 40328,
        40572, 40573, 41403, 41404, 41405, 41406, 41407, 41408, 41409, 41410, 41537, 43702, 43703, 43728, 44067, 45746, 45748, 45749, 45750, 46152, 46153, 46154, 46155, 46156,
        47445, 54794, 54826, 54840, 54841, 55031, 55032, 55033, 55034, 55035, 55511, 57164, 57317, 57318, 58956, 60377, 60378, 60379, 60380, 60515, 62450, 62451, 62452, 62453,
        73910, 73912, 77725, 77738, 77739, 81032, 81044, 81363, 81364, 81365, 81366, 81367, 81368, 81369, 81370, 81371, 81372, 81373, 81374, 81375, 81376, 81377, 81896, 83094,
        83464, 84217, 84218, 84220, 85233, 88377, 89241, 89609, 89610, 89611, 89689, 90040, 90398, 90727, 90728, 91017
    );

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

    @Override
    protected HTML[] getContent(HtmlContext context) {
        if (remoteInjectionSkills.contains(type.typeID) && type.basePrice != null) {
            return new HTML[]{
                TABLE("type_market_price_table font_text").content(
                    TR().content(
                        TD().content(
                            SPAN("type_market_price_span font_text").content(
                                IMG(Resource.file("EVE/Jita Logo.png"), null, 32).title("Jita 4-4 Trade Hub").className("type_market_price_icon"),
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
