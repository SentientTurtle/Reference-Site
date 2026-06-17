package net.sentientturtle.nee.components;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.nee.data.Resource;
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

    public TypeSkills(Type type) {
        super("type_skills colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Boolean omegaRequired = context.sde.getItemOmegaMap().get(type.typeID);

        Element list = DIV("type_skills_list font_text");
        fetchSkills(type.typeID, context.sde, list, 1);
        return new HTML[]{
            HEADER("type_skills_title font_header").text("Required skills"),
            list,
            omegaRequired != null
                ? omegaRequired
                    ? DIV("type_skills_clonegrade").content(
                        IMG(Resource.fromSharedCache("res:/ui/texture/classes/clonegrade/omega_128.png", context), null, 32).className("type_skills_clonegrade_icon"),
                        TEXT_ITALICS("Omega Clone")
                    )
                    : DIV("type_skills_clonegrade").content(
                        IMG(Resource.fromSharedCache("res:/ui/texture/classes/clonegrade/alpha_128.png", context), null, 32).className("type_skills_clonegrade_icon"),
                        TEXT_ITALICS("Alpha Clone")
                    )
                : HTML.empty()
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
                flex-wrap: flex;
                flex-grow: 1;
            }
            
            .type_skills_level {
                white-space: pre;
            }
            
            .type_skills_indicator {
                user-select: none;
            }
            
            .type_skills_clonegrade {
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
            }
            
            .type_skills_clonegrade_icon {
                width: 2rem;
                height: 2rem;
            }
            """;
    }

    private void fetchSkills(int typeID, SDEData sdeData, Element parent, int indent) {
        Map<Integer, Integer> requiredSkills = sdeData.getRequiredSkillMap().get(typeID);
        if (requiredSkills == null) return;
        for (Map.Entry<Integer, Integer> entry : requiredSkills.entrySet()) {
            int skill = entry.getKey();
            int level = entry.getValue();

            Element row = DIV("type_skills_row");
            parent.content(row);
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

            fetchSkills(skill, sdeData, parent, indent + 1);
        }
    }
}
