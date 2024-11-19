package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.sharedcache.IconProvider;
import net.sentientturtle.nee.util.EVEText;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.components.*;
import net.sentientturtle.nee.data.datatypes.Type;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.sentientturtle.html.HTML.DIV;

/**
 * Page for a {@link Type}
 */
public class TypePage extends Page {
    public static final int[] CAN_BE_FITTED_TO_GROUP_ATTRIBUTES = {1298, 1299, 1300, 1301, 1872, 1879, 1880, 1881, 2065, 2396, 2476, 2477, 2478, 2479, 2480, 2481, 2482, 2483, 2484, 2485};
    public static final int[] CAN_BE_FITTED_TO_TYPE_ATTRIBUTES = {1302, 1303, 1304, 1305, 1380, 1944, 2103, 2463, 2486, 2487, 2488, 2758};
    public static final int[] USED_WITH_GROUP_ATTRIBUTES = {137, 602, 603, 604, 605, 606, 609, 610, 2076, 2077, 2078};
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
    public ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.typeIcon(type.typeID, context);
    }

    @Override
    public String name() {
        return type.name;
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .type_page_column {
                width: calc(32rem + (2 * var(--border-size)));
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
        var dataSupplier = context.sde;

//        var grid = DIV("type_page_grid");
        var left = DIV("type_page_column");
        var mid = DIV("type_page_column");
        var right = DIV("type_page_column");

        Group group = dataSupplier.getGroups().get(type.groupID);
        if (group == null) {
            throw new NullPointerException("Missing group for type: " + type.name + " (" + type.typeID + ")");
        }
        int categoryID = group.categoryID;

        // Check if type is a Ship, Drone, Citadel or Fighter
        boolean hasRender = categoryID == 6         // Ship
                            || categoryID == 18     // Drone
                            || categoryID == 22     // Deployable
                            || categoryID == 24     // Starbase
                            || categoryID == 65     // Structure
                            || categoryID == 87;    // Fighter
        if (Main.GENERATE_ICONS) {
            hasRender = hasRender && IconProvider.hasRender(this.type.typeID, context.dataSources);
        }

        if (hasRender) {
            left.content(
                new Title(type.name, null),
                new TypeGroup(type),
                new ItemRender(type)
            );
        } else {
            left.content(
                new Title(type.name, getIcon(context)),
                new TypeGroup(type)
            );
        }

        if (type.volume > 0.0 || type.mass > 0.0) {
            left.content(new TypeVolume(type));
        }

        if (type.description != null && type.description.length() > 0)
            left.content(new ItemDescription(EVEText.escape(type.description, context.sde)));

        if (!dataSupplier.getTypeTraits().getOrDefault(type.typeID, Map.of()).isEmpty())
            left.content(new TypeTraits(type));

        Map<Integer, Attribute> attributes = dataSupplier.getAttributes();
        Map<Integer, Double> typeAttributes = dataSupplier.getTypeAttributes().getOrDefault(type.typeID, Map.of());

        // Hide health for types that use damage attributes for other purposes, such as modules
        // Ship, Structure, Drone or Fighter
        if (categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46 || categoryID == 65 || categoryID == 18 || categoryID == 87) {
            // Type is a ship or structure
            if (categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46 || categoryID == 65) {
                if (type.capacity > 0.0 || typeAttributes.keySet().stream().map(attributes::get).anyMatch(attribute -> attribute != null && Objects.equals(attribute.categoryID, 40))) {
                    left.content(new ShipCargo(type));
                }
            }

            if (typeAttributes.getOrDefault(263, 0.0) > 0) {
                mid.content(new ShipShield(type));
            }
            if (typeAttributes.getOrDefault(265, 0.0) > 0) {
                mid.content(new ShipArmor(type));
            }
            if (typeAttributes.getOrDefault(9, 0.0) > 0) {
                mid.content(new ShipHull(type));
            }

            if (categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46) {
                int highSlots = typeAttributes.getOrDefault(14, 0.0).intValue();
                int medSlots = typeAttributes.getOrDefault(13, 0.0).intValue();
                int lowSlots = typeAttributes.getOrDefault(12, 0.0).intValue();

                if (highSlots != 0 || medSlots != 0 || lowSlots != 0) {
                    mid.content(new ShipFitting(type));
                }
            }

            if (typeAttributes.getOrDefault(37, 0.0) > 0) // Max velocity > 0
                mid.content(new ShipPropulsion(type));

            // Has targeting range or sensor strength
            if (typeAttributes.getOrDefault(76, 0.0) > 0
                || typeAttributes.getOrDefault(208, 0.0) > 0 || typeAttributes.getOrDefault(209, 0.0) > 0
                || typeAttributes.getOrDefault(210, 0.0) > 0 || typeAttributes.getOrDefault(211, 0.0) > 0)
                mid.content(new ShipSensors(type));

            if (categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46) {
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
            }
        }

        if (group.categoryID == 7)
            mid.content(new ModuleFitting(type));

        if (type.groupID == 1964) { // If item type is a Mutaplasmid
            mid.content(new ItemStats(type));
        } else if (categoryID == 7 || categoryID == 8 || categoryID == 20) {    // If module, charge, or implant
            for (int listedAttribute : ItemStats.INCLUDED_ATTRIBUTES) {
                if (typeAttributes.containsKey(listedAttribute) && listedAttribute != 9) {
                    mid.content(new ItemStats(type));
                    break;
                }
            }
        }

        HashSet<Integer> canBeFittedToGroups = new HashSet<>();
        HashSet<Integer> canBeFittedToTypes = new HashSet<>();
        for (int attributeID : CAN_BE_FITTED_TO_GROUP_ATTRIBUTES) {
            Double groupID = typeAttributes.get(attributeID);
            if (groupID != null) {
                canBeFittedToGroups.add((int) (double) groupID);
            }
        }

        for (int attributeID : CAN_BE_FITTED_TO_TYPE_ATTRIBUTES) {
            Double typeID = typeAttributes.get(attributeID);
            if (typeID != null) {
                canBeFittedToTypes.add((int) (double) typeID);
            }
        }

        if (canBeFittedToGroups.size() > 0 || canBeFittedToTypes.size() > 0) {
            mid.content(new UsedWith("Can be fitted to", canBeFittedToGroups, canBeFittedToTypes));
        }

        HashSet<Integer> usedWithGroups = new HashSet<>();
        for (int attributeID : USED_WITH_GROUP_ATTRIBUTES) {
            Double groupID = typeAttributes.get(attributeID);
            if (groupID != null) {
                usedWithGroups.add((int) (double) groupID);
            }
        }

        Double targetChargeSize = typeAttributes.get(128);

        Set<Integer> usedWithTypes = usedWithGroups.stream()
            .flatMap(g -> dataSupplier.getGroupTypes().getOrDefault(g, Set.of()).stream())
            .filter(t -> {
                HashSet<Integer> targetUsedWithGroups = new HashSet<>();
                for (int attributeID : USED_WITH_GROUP_ATTRIBUTES) {
                    Double groupID = dataSupplier.getTypeAttributes().getOrDefault(t.typeID, Map.of()).get(attributeID);
                    if (groupID != null) {
                        targetUsedWithGroups.add((int) (double) groupID);
                    }
                }

                // Exclude charges which cannot fit the module
                if (categoryID == 7 && t.volume > type.capacity) return false;
                if (categoryID == 8 && t.capacity < type.volume) return false;

                // Exclude charges which do not have the module type set as usedWith
                if (targetUsedWithGroups.contains(type.groupID)) {
                    Double chargeSize = dataSupplier.getTypeAttributes().get(t.typeID).get(128);
                    // exclude charges with a different chargeSize as this module
                    if (chargeSize != null && targetChargeSize != null) {
                        return (double) chargeSize == (double) targetChargeSize;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            })
            .map(t -> t.typeID)
            .collect(Collectors.toSet());

        if (usedWithTypes.size() > 0) {
            mid.content(new UsedWith("Used with", Set.of(), usedWithTypes));
        }

        if (dataSupplier.getBpActivities().containsKey(type.typeID) || dataSupplier.getOutputSchematicMap().containsKey(type.typeID)) {
            mid.content(new TypeBlueprint(type));
        }

        if (typeAttributes.containsKey(182))  // Has a skill-required-1 attribute
            right.content(new TypeSkills(type));

        if (dataSupplier.getRequiresSkillMap().containsKey(type.typeID))
            right.content(new SkillRequiredFor(type));

        if (dataSupplier.getVariants().containsKey(type.typeID))  // If type has variants
            right.content(new TypeVariants(type));

        // If produced by a blueprint, used in PI, used in a blueprint, or has reprocessing output
        if (dataSupplier.getProductActivityMap().containsKey(type.typeID)
            || dataSupplier.getReprocessingMaterials().containsKey(type.typeID)
            || dataSupplier.getMaterialActivityMap().containsKey(type.typeID)
            || dataSupplier.getInputSchematicMap().containsKey(type.typeID)
            || dataSupplier.getOreReprocessingMap().containsKey(type.typeID)) {
            right.content(new TypeIndustry(type));
        }

        var columns = new ArrayList<HTML>();
        if (!left.isEmpty()) columns.add(left);
        if (!mid.isEmpty()) columns.add(mid);
        if (!right.isEmpty()) columns.add(right);
        return HTML.multi(columns.toArray(HTML[]::new));
    }
}
