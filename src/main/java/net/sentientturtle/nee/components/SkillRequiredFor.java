package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.page.TypePage;

import java.util.*;

import static net.sentientturtle.html.HTML.*;

/**
 * Variants of a {@link Type}
 */
public class SkillRequiredFor extends Component {
    private final Type type;

    public SkillRequiredFor(Type type) {
        super("skill_required colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Set<Integer>> requiredFor = context.sde.getRequiresSkillMap().get(type.typeID);

        var table = TABLE("skill_required_table font_header");

        requiredFor.entrySet().stream()
            .sorted(Comparator.comparingInt(Map.Entry::getKey))
            .forEach(entry -> {
                table.content(TR().content(TH().attribute("colspan", "2").text("Level " + entry.getKey())));

                entry.getValue().stream()
                    .map(context.sde.getTypes()::get)
                    .sorted(Type.comparator(context.sde))
                    .forEach(type -> {
                        table.content(TR().content(
                            TD().content(IMG(ResourceLocation.typeIcon(type.typeID, context), null, 64).className("skill_required_icon")),
                            TD().content(new PageLink(new TypePage(context.sde.getTypes().get(type.typeID))))
                        ));
                    });
            });

        return new HTML[]{
            HEADER("font_header").text("Required for"),
            table
        };
    }

    @Override
    protected String getCSS() {
        return """
            .skill_required {
                padding: 0.5rem;
            }
            
            .skill_required_table {
                margin-top: 0.5rem;
                width: 100%;
            }
            
            .skill_required_icon {
                width: 2rem;
                height: 2rem;
            }""";
    }
}
