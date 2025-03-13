package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/**
 * Required skills to use a {@link Type}
 * <br>
 * Skill level is displayed with a roman numeral and a dot indicator (conforming to in-game UI)
 */
public class TypeSkills extends Component {
    private final Type type;
    public static final int[] SKILL_ATTRIBUTES = {182, 183, 184, 1285, 1289, 1290};
    public static final int[] LEVEL_ATTRIBUTES = {277, 278, 279, 1286, 1287, 1288};

    public TypeSkills(Type type) {
        super("type_skills colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Element list = DIV("type_skills_list font_text");
        fetchSkills(type.typeID, context.sde, list, 1);
        return new HTML[]{
            HEADER("type_skills_title font_header").text("Required skills"),
            list
        };
    }

    @Override
    protected String getCSS() {
        return """
            .type_skills {
                padding: 0.5rem;
            }
            
            .type_skills_title {
                margin-bottom: 0.5rem;
            }
            
            .type_skills_row {
                width: 100%;
                display: flex;
            }
            
            .type_skills_spacer {
                padding-left: 0.5rem;
                border: 1px none #525252;
                border-left-style: solid;
            }
            
            .type_skills_text {
                display: flex;
                flex-grow: 1;
            }
            
            .type_skills_level {
                white-space: pre;
            }
            
            .type_skills_indicator {
                user-select: none;
            }
            """;
    }

    private void fetchSkills(int typeID, SDEData sdeData, Element parent, int indent) {
        Map<Integer, Map<Integer, Double>> attributeValues = sdeData.getTypeAttributes();

        for (int i = 0; i < SKILL_ATTRIBUTES.length; i++) {
            Double skill = attributeValues.get(typeID).get(SKILL_ATTRIBUTES[i]);
            if (skill != null) {
                Element row = DIV("type_skills_row");
                parent.content(row);

                int level = attributeValues.get(typeID).get(LEVEL_ATTRIBUTES[i]).intValue();
                if (level < 0 || level > 5) throw new RuntimeException("Invalid skill level: " + level);

                String levelBoxes = "■".repeat(level) + "□".repeat(5 - level);

                row.content(
                    HTML.repeat(indent, DIV("type_skills_spacer")),
                    SPAN("type_skills_text").content(
                        SPAN("type_skills_text").content(sdeData.format_with_unit(skill, 116)), // 116 = typeID unit
                        SPAN("type_skills_level font_roman_numeral").text(" " + level + " ").content(
                            SPAN("type_skills_indicator").text(levelBoxes)
                        )
                    )
                );

                if (attributeValues.get(skill.intValue()).containsKey(182)) {
                    fetchSkills(skill.intValue(), sdeData, parent, indent + 1);
                }
            }
        }
    }
}
