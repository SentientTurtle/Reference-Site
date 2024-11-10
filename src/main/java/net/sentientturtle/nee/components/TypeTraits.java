package net.sentientturtle.nee.components;

import net.sentientturtle.html.*;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.TypeTraitBonus;
import net.sentientturtle.nee.data.DataSupplier;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.pages.TypePage;
import net.sentientturtle.nee.util.EVEText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static net.sentientturtle.html.HTML.*;

/**
 * Traits of a {@link Type}, usually a Ship.
 */
public class TypeTraits extends Component {
    private final Type type;

    public TypeTraits(Type type) {
        super("type_traits colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        boolean first = true;

        Element table = TABLE("type_traits_table font_text");

        Map<Integer, List<TypeTraitBonus>> traitMap = context.data.getTypeTraits().get(type.typeID);
        ArrayList<Integer> keyList = new ArrayList<>(traitMap.keySet());
        keyList.sort(Integer::compareUnsigned);
        for (Integer skillID : keyList) {
            if (first) {
                first = false;
            } else {
                table.content(
                    TR().content(
                        TD("type_traits_blank").attribute("colspan", "2")
                    )
                );
            }
            if (skillID == -1) {
                table.content(
                    TR("font_header").content(
                        TH().attribute("colspan", "2").content(HEADER().text("Role Bonus"))
                    )
                );
            } else if (skillID == -2) {
                table.content(
                    TR("font_header").content(
                        TH().attribute("colspan", "2").content(HEADER().text("Misc Bonus"))
                    )
                );
            } else {
                table.content(
                    TR("font_header").content(
                        TH().attribute("colspan", "2").content(HEADER().content(
                            new PageLink(new TypePage(context.data.getTypes().get(skillID))),
                            TEXT(" bonuses (per skill level)")
                        ))
                    )
                );
            }

            if (skillID == -2 && type.groupID == 1305) {
                List<TypeTraitBonus> traits = traitMap.get(skillID);
                List<TypeTraitBonus> modeTraits = traits.subList(4, traits.size());
                modeTraits.stream()
                    .filter(t -> !(t.bonusText.contains("Defense Mode") || t.bonusText.contains("Propulsion Mode") || t.bonusText.contains("Sharpshooter Mode")))
                    .forEach(trait -> appendTrait(table, trait, context.data));

                // Trait 0 is the "Additional bonuses available" message, we skip it
                for (int i = 1; i < 4; i++) {
                    TypeTraitBonus mode = traits.get(i);
                    String modeString;
                    if (mode.bonusText.contains("Defense")) {
                        modeString = "Defense Mode";
                    } else if (mode.bonusText.contains("Propulsion")) {
                        modeString = "Propulsion Mode";
                    } else if (mode.bonusText.contains("Sharpshooter")) {
                        modeString = "Sharpshooter Mode";
                    } else {
                        throw new RuntimeException("Unknown T3D Mode!");
                    }

                    table.content(
                        TR("font_header").content(
                            TH().attribute("colspan", "2").content(HEADER().text(modeString))
                        )
                    );

                    modeTraits.stream()
                        .filter(trait -> trait.bonusText.contains(modeString))
                        .forEach(trait -> appendTrait(table, trait, context.data));
                }
            } else {
                for (TypeTraitBonus traitTuple : traitMap.get(skillID)) {
                    appendTrait(table, traitTuple, context.data);
                }
            }
        }

        return new HTML[]{ table };
    }

    @Override
    protected String getCSS() {
        return """
            .type_traits {
                padding: 0.5rem;
            }
            
            .type_traits_table {
                width: 100%;
            }
            
            .type_traits_blank {
                height: 1rem;
            }""";
    }

    private void appendTrait(Element table, TypeTraitBonus traitTuple, DataSupplier dataSupplier) {
        table.content(
            TR().content(
                TD().content(traitTuple.bonusAmount != null ? dataSupplier.format_with_unit(traitTuple.bonusAmount, traitTuple.unitID) : TEXT("")),
                TD().content(EVEText.escape(traitTuple.bonusText, dataSupplier))
            )
        );
    }
}
