package net.sentientturtle.nee.components;

import net.sentientturtle.html.*;
import net.sentientturtle.html.Component;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.TypeTraits;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.page.TypePage;
import net.sentientturtle.nee.util.EVEText;

import java.util.List;
import java.util.Map;

import static net.sentientturtle.html.HTML.*;

/**
 * Traits of a {@link Type}, usually a Ship.
 */
public class TypeTraitInfo extends Component {
    private final Type type;

    public TypeTraitInfo(Type type) {
        super("type_traits colour_theme_minor");
        this.type = type;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        boolean first = true;

        Element table = TABLE("type_traits_table font_text");

        TypeTraits traits = context.sde.getTypeTraits().get(type.typeID);
        for (Map.Entry<Integer, List<TypeTraits.Bonus>> entry : traits.skillBonuses().entrySet()) {
            int skillID = entry.getKey();

            table.content(
                TR("font_header").content(
                    TH().attribute("colspan", "2").content(HEADER().content(
                        new PageLink(new TypePage(context.sde.getTypes().get(skillID))),
                        TEXT(" bonuses (per skill level)")
                    ))
                )
            );

            for (TypeTraits.Bonus bonus : entry.getValue()) {
                appendBonus(table, bonus, context.sde);
            }
        }

        if (traits.roleBonuses().size() > 0) {
            table.content(
                TR("font_header").content(
                    TH().attribute("colspan", "2").content(HEADER().text("Role Bonus"))
                )
            );

            for (TypeTraits.Bonus bonus : traits.roleBonuses()) {
                appendBonus(table, bonus, context.sde);
            }
        }

        if (traits.miscBonuses().size() > 0) {
            table.content(
                TR("font_header").content(
                    TH().attribute("colspan", "2").content(HEADER().text("Misc Bonus"))
                )
            );

            for (TypeTraits.Bonus bonus : traits.miscBonuses()) {
                appendBonus(table, bonus, context.sde);
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
                border-spacing: 0.25rem 0.5rem;
            }
            
            .type_traits_blank {
                height: 1rem;
            }""";
    }

    private void appendBonus(Element table, TypeTraits.Bonus bonus, SDEData SDEData) {
        table.content(
            TR().content(
                TD().content(bonus.bonusAmount() != null ? SDEData.format_with_unit(bonus.bonusAmount(), bonus.unitID()) : TEXT("")),
                TD().content(EVEText.escape(bonus.bonusText(), SDEData, true))
            )
        );
    }
}
