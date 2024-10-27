package net.sentientturtle.nee.pages;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.util.EVEText;
import net.sentientturtle.nee.util.ResourceLocation;
import net.sentientturtle.nee.components.*;
import net.sentientturtle.nee.data.datatypes.Type;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static net.sentientturtle.html.HTML.DIV;

/**
 * Page for a {@link Type}
 */
public class TypePage extends Page {
    public final Type type;

    public TypePage(Type type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.TYPE;
    }

    @Override
    public String filename() {
        return type.typeID + "-" + name();
    }

    @Nullable
    @Override
    public ResourceLocation getIcon() {
        return ResourceLocation.iconOfTypeID(type.typeID);
    }

    @Override
    public String name() {
        return type.name;
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .type_page_column {
                width: calc(32rem + (2 * var(--border-size))));
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }
            
            .type_page_column > div {
                border: 1px solid var(--colour-theme-minor-border);
            }
            """;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        var dataSupplier = context.data;

//        var grid = DIV("type_page_grid");
        var left = DIV("type_page_column");
        var mid = DIV("type_page_column");
        var right = DIV("type_page_column");

        Group group = dataSupplier.getGroups().get(type.groupID);
        if (group == null) {
            throw new NullPointerException();
        }
        int categoryID = group.categoryID;
        if ((categoryID == 6 || categoryID == 18 || categoryID == 65 || categoryID == 87)) {   // Check if type is a Ship, Drone, Citadel or Fighter
            left.content(
                new Title(type.name, null),
                new TypeGroup(type),
                new ItemRender(type)
            );
        } else {
            left.content(
                new Title(type.name, getIcon()),
                new TypeGroup(type)
            );
        }
        if (type.description != null && type.description.length() > 0)
            left.content(new ItemDescription(EVEText.escape(type.description, context.data)));

        if (!dataSupplier.getTypeTraits().getOrDefault(type.typeID, Map.of()).isEmpty())
            left.content(new TypeTraits(type));

        Map<Integer, Attribute> attributes = dataSupplier.getAttributes();
        Map<Integer, Double> typeAttributes = dataSupplier.getTypeAttributes().getOrDefault(type.typeID, Map.of());

        if (type.capacity > 0.0 || typeAttributes.keySet().stream().map(attributes::get).anyMatch(attribute -> attribute != null && Objects.equals(attribute.categoryID, 40))) {
            int category = context.data.getGroups().get(type.groupID).categoryID;
            if (category == 6 || category == 7 || category == 46 || category == 23 || category == 40 || category == 22 || category == 66 || category == 32) {
                left.content(new ShipCargo(type));
            }
        }

        // Hide Hull health for types that do not have shield or armour (Such as modules, which use Hull HP for overheating damage)
        boolean showHull = false;
        if (typeAttributes.getOrDefault(263, 0.0) > 0) {
            mid.content(new ShipShield(type));
            showHull = true;
        }
        if (typeAttributes.getOrDefault(265, 0.0) > 0) {
            mid.content(new ShipArmor(type));
            showHull = true;
        }
        if (typeAttributes.getOrDefault(9, 0.0) > 0 && showHull) {
            mid.content(new ShipHull(type));
        }


        int highSlots = typeAttributes.getOrDefault(14, 0.0).intValue();
        int medSlots = typeAttributes.getOrDefault(13, 0.0).intValue();
        int lowSlots = typeAttributes.getOrDefault(12, 0.0).intValue();

        if (highSlots != 0 || medSlots != 0 || lowSlots != 0) {
            mid.content(new ShipFitting(type));
        }

        if (typeAttributes.getOrDefault(37, 0.0) > 0) // Max velocity > 0
            mid.content(new ShipPropulsion(type));

        // Has targeting range or sensor strength
        if (typeAttributes.getOrDefault(76, 0.0) > 0
            || typeAttributes.getOrDefault(208, 0.0) > 0 || typeAttributes.getOrDefault(209, 0.0) > 0
            || typeAttributes.getOrDefault(210, 0.0) > 0 || typeAttributes.getOrDefault(211, 0.0) > 0)
            mid.content(new ShipSensors(type));

        if (typeAttributes.getOrDefault(2217, 0.0) > 0         // Light fighter slots
            || typeAttributes.getOrDefault(2218, 0.0) > 0  // Support fighter slots
            || typeAttributes.getOrDefault(2219, 0.0) > 0)  // Heavy fighter slots
            mid.content(new ShipFighters(type));

        if (typeAttributes.getOrDefault(2737, 0.0) > 0  // Standup light fighter slots
            || typeAttributes.getOrDefault(2738, 0.0) > 0  // Standup support fighter slots
            || typeAttributes.getOrDefault(2739, 0.0) > 0) // Standup heavy fighter slots
            mid.content(new ShipFightersStandup(type));

        if (typeAttributes.getOrDefault(1271, 0.0) > 0) // Drone bandwidth > 0
            mid.content(new ShipDrones(type));

        // TODO: Mass/Volume

        if (typeAttributes.containsKey(182))  // Has a skill-required-1 attribute
            right.content(new TypeSkills(type));

        if (dataSupplier.getVariants().containsKey(type.typeID))  // Check if type has variants
            right.content(new TypeVariants(type));

        if (dataSupplier.getProductActivityMap().containsKey(type.typeID))
            right.content(new TypeOrigin(type));

        var columns = new ArrayList<HTML>();
        if (!left.isEmpty()) columns.add(left);
        if (!mid.isEmpty()) columns.add(mid);
        if (!right.isEmpty()) columns.add(right);
        return HTML.multi(columns.toArray(HTML[]::new));
    }
}
