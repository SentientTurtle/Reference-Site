package net.sentientturtle.nee.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.util.ExceptionUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/// Component for type volumes & mass, to be expanded to include ship sizes
public class TypeVolume extends Component {
    private final Type type;

    public TypeVolume(Type type) {
        super("type_volume colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        double volume = this.type.volume;
        double mass;
        if (context.sde.getGroups().get(type.groupID).categoryID == 6) {
            mass = this.type.mass;
        } else {
            mass = 0.0;
        }

        if (PACKAGED_VOLUMES.containsKey(type.typeID)) {
            return new HTML[]{
                TABLE("type_volume_table font_text").content(
                    TR().content(
                        TD().content(SPAN("type_volume_span").title("Volume").content(
                            IMG(ResourceLocation.ofIconID(67, context), null, 32).className("type_volume_icon"),
                            TEXT("Volume:")
                        )),
                        TD().content(context.sde.format_with_unit(volume, 9))
                    ),
                    TR().content(
                        TD().content(SPAN("type_volume_span").title("Packaged").content(
                            IMG(ResourceLocation.ofIconID(67, context), null, 32).className("type_volume_icon"),
                            TEXT("Packaged Volume:")
                        )),
                        TD().content(context.sde.format_with_unit(PACKAGED_VOLUMES.get(type.typeID), 9))
                    ),
                    mass > 0.0 ?
                        TR().content(
                            TD().content(SPAN("type_volume_span").content(
                                IMG(ResourceLocation.ofIconID(76, context), null, 32).title("Mass").className("type_volume_icon"),
                                TEXT("Mass:")
                            )),
                            TD().content(context.sde.format_with_unit(mass, 2))
                        )
                        : HTML.empty()
                )
            };
        } else {
            return new HTML[]{
                TABLE("type_volume_table font_text").content(
                    TR().content(
                        TD().content(SPAN("type_volume_span").content(
                            IMG(ResourceLocation.ofIconID(67, context), null, 32).title("Volume").className("type_volume_icon"),
                            TEXT("Volume: "),
                            context.sde.format_with_unit(volume, 9)
                        )),
                        mass > 0.0 ?
                            TD().content(SPAN("type_volume_span").title("Mass").content(
                                IMG(ResourceLocation.ofIconID(76, context), null, 32).className("type_volume_icon"),
                                TEXT("Mass: "),
                                context.sde.format_with_unit(mass, 2)
                            ))
                            : HTML.empty()
                    )
                )
            };
        }

    }

    @Override
    protected String getCSS() {
        return """
            .type_volume {
              padding: 0.5rem;
            }
            
            .type_volume_table {
                width: 100%;
                border-collapse: collapse;
            }
            
            .type_volume_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .type_volume_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .type_volume_icon {
                width: 2rem;
                height: 2rem;
            }
            """;
    }

    private static final Map<Integer, Double> PACKAGED_VOLUMES;
    static {
        // Clumsy hack; Packaged volumes are not handled by "proper" data access, this is to be replaced with the original data source
        try {
            PACKAGED_VOLUMES = new ObjectMapper().readValue(
                new URI("https://sde.hoboleaks.space/tq/repackagedvolumes.json").toURL(),
                new TypeReference<HashMap<Integer, Double>>() {}
            );
            System.out.println("TEMP: package volumes loaded!");
        } catch (IOException | URISyntaxException e) {
            throw ExceptionUtil.<RuntimeException, RuntimeException>sneakyThrow(e);
        }
    }
}
