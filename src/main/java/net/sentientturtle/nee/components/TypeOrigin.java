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
public class TypeOrigin extends Component {
    private final Type type;

    public TypeOrigin(Type type) {
        super("type_origin colour_theme_minor");
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
                    DIV("type_origin_blueprint font_header").content(
                        IMG(ResourceLocation.iconOfTypeID(activity.bpTypeID), null, 64).className("type_origin_icon"),
                        new PageLink(new TypePage(context.data.getTypes().get(activity.bpTypeID))).className("type_origin_link"),
                        TEXT(" ("), TEXT(context.data.getIndustryActivityTypes().get(activity.activityID).activityName), TEXT(")")
                    )
                ))
        ).toArray(HTML[]::new);
    }

    @Override
    protected String getCSS() {
        return """
            .type_origin {
                padding: 0.5rem;
            }
            
            .type_origin_blueprint {
                width: 100%;
                font-size: 1.25rem;
                display: flex;
                align-items: center;
                margin-top: 0.5rem;
            }
            
            .type_origin_icon {
                width: 2rem;
                height: 2rem;
            }
            
            .type_origin_link {
                margin: 0.5rem;
            }""";
    }
}
