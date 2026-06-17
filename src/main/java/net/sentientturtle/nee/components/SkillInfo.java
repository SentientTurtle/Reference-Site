package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.Resource;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sde.SDEData;
import org.jspecify.annotations.Nullable;

import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/**
 * Information about a skill, such as character attribute and training time
 */
public class SkillInfo extends Component {
    private final Type type;

    public SkillInfo(Type type) {
        super("skill_info colour_theme_minor");
        this.type = type;
    }

    private static final Map<Integer, String> charAttributeNames = Map.of(165, "Intelligence", 164, "Charisma", 167, "Perception", 166, "Memory", 168, "Willpower");
    private static final Map<Integer, Integer> charAttributeIcons = Map.of(165, 1380, 164, 1378, 167, 1382, 166, 1381, 168, 1379);

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Double> attributes = context.sde.getTypeAttributes().get(type.typeID);
        // Just throw an NPE if either of these attributes are missing, as all skills should have them
        double primaryCharAttribute = attributes.get(180);
        double secondaryCharAttribute = attributes.get(181);
        double trainingMultiplier = attributes.get(275);

        @Nullable Integer alphaLevel = context.sde.getAlphaSkills().get(type.typeID);

        return new HTML[]{
            HEADER("skill_info_title font_header").text("Skill"),
            TABLE("skill_info_table font_text").content(
                TR().content(
                    TD().content(
                        SPAN("skill_info_span font_text").content(
                            IMG(Resource.file("EVE/char_attribute_generic.png"), null, 32).className("skill_info_icon"),
                            TEXT("Primary Attribute:")
                        )
                    ),
                    TD().content(
                        SPAN("skill_info_span font_text").content(
                            IMG(Resource.ofIconID(charAttributeIcons.get((int) primaryCharAttribute), context), null, 32).className("skill_info_icon"),
                            TEXT_ITALICS(charAttributeNames.get((int) primaryCharAttribute))
                        )
                    )
                ),
                TR().content(
                    TD().content(
                        SPAN("skill_info_span font_text").content(
                            IMG(Resource.file("EVE/char_attribute_generic.png"), null, 32).className("skill_info_icon"),
                            TEXT("Secondary Attribute:")
                        )
                    ),
                    TD().content(
                        SPAN("skill_info_span font_text").content(
                            IMG(Resource.ofIconID(charAttributeIcons.get((int) secondaryCharAttribute), context), null, 32).className("skill_info_icon"),
                            TEXT_ITALICS(charAttributeNames.get((int) secondaryCharAttribute))
                        )
                    )
                ),
                TR().content(
                    TD().content(
                        SPAN("skill_info_span font_text").content(
                            IMG(Resource.ofIconID(1392, context), null, 32).className("skill_info_icon"),
                            TEXT("Training time multiplier:")
                        )
                    ),
                    TD().content(context.sde.format_with_unit(trainingMultiplier, 104))
                ),
                alphaLevel != null
                    ? TR().content(
                        TD().content(
                            SPAN("skill_info_span font_text").content(
                                IMG(Resource.fromSharedCache("res:/ui/texture/classes/clonegrade/alpha_128.png", context), null, 32).className("skill_info_icon"),
                                TEXT("Alpha Clone max level: ")
                            )
                        ),
                        TD().content(
                            SPAN("font_roman_numeral").text(String.valueOf(alphaLevel)),
                            SPAN("skill_info_indicator").text(" " + "■".repeat(alphaLevel) + "□".repeat(5 - alphaLevel))
                        )
                    )
                    : TR().content(
                        TD().attribute("colspan", "2").content(
                            SPAN("skill_info_span font_text skill_info_omega").content(
                                IMG(Resource.fromSharedCache("res:/ui/texture/classes/clonegrade/omega_128.png", context), null, 32).className("skill_info_icon"),
                                TEXT_ITALICS("Omega Clone required")
                            )
                        )
                    )
            ),
            TABLE("skill_info_table skill_info_sp font_text").content(
                TR().content(
                    TH().text("Level"),
                    TH().text("SP required"),
                    TH().text("Est. Training Time")
                ),
                levelRow(1, trainingMultiplier, context),
                levelRow(2, trainingMultiplier, context),
                levelRow(3, trainingMultiplier, context),
                levelRow(4, trainingMultiplier, context),
                levelRow(5, trainingMultiplier, context)
            )
        };
    }

    private Element levelRow(int level, double multiplier, HtmlContext context) {
        long spRequired = Math.round((Math.pow(2.0, 2.5 * (double) (level - 1)) * 250.0 * multiplier));
        return TR().content(
            TD().content(
                SPAN("font_roman_numeral").text(String.valueOf(level)),
                SPAN("skill_info_indicator").text(" " + "■".repeat(level) + "□".repeat(5 - level))
            ),
            TD().content(context.sde.format_with_unit(spRequired, -7)),
            TD().content(context.sde.format_with_unit(spRequired / (33.0/60.0), 3))
        );
    }

    @Override
    protected String getCSS() {
        return """
            .skill_info {
                padding: 0.5rem;
            }
            
            .skill_info_title {
                margin-bottom: 0.5rem;
            }
            
            .skill_info_table {
                width: 100%;
                border-collapse: collapse;
            }
            
            .skill_info_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .skill_info_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .skill_info_omega {
                justify-content: center;
            }
            
            .skill_info_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .skill_info_indicator {
                user-select: none;
            }
            
            .skill_info_sp {
                text-align: end;
            }
            """;
    }
}
