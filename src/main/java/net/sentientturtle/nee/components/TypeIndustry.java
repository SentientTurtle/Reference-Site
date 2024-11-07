package net.sentientturtle.nee.components;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.pages.TypePage;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Type;

import java.util.stream.Stream;

import static net.sentientturtle.html.HTML.*;

/**
 * Industry origin of a {@link Type}; "This type is produced from this blueprint"
 */
public class TypeIndustry extends Component {
    private final Type type;

    public TypeIndustry(Type type) {
        super("type_industry colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        assert context.data.getProductActivityMap().containsKey(type.typeID);    // Should be handled by TypePage check

        return Stream.concat(
            Stream.of(HEADER("font_header").text("Produced from")),
            context.data.getProductActivityMap()
                .get(type.typeID)
                .stream()
                .flatMap(activity -> Stream.of(
                    DIV("type_industry_blueprint font_header").content(
                        IMG(ResourceLocation.typeIcon(activity.bpTypeID, context), null, 64).className("type_industry_icon"),
                        new PageLink(new TypePage(context.data.getTypes().get(activity.bpTypeID))).className("type_industry_link"),
                        TEXT(" ("), TEXT(context.data.getIndustryActivityTypes().get(activity.activityID).activityName), TEXT(")")
                    )
                ))
        ).toArray(HTML[]::new);
    }

    @Override
    protected String getCSS() {
        return """
            .type_industry {
                padding: 0.5rem;
            }
            
            .type_industry_blueprint {
                width: 100%;
                font-size: 1.25rem;
                display: flex;
                align-items: center;
                margin-top: 0.5rem;
            }
            
            .type_industry_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .type_industry_link {
                margin: 0.5rem;
            }""";
    }
}
