package net.sentientturtle.nee.page;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.HasPersistentUrl;
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
public class TypePage extends Page implements HasPersistentUrl {
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

    @Override
    public String persistentName() {
        return String.valueOf(type.typeID);
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
    public @Nullable String description() {
        return type.description;
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
        var data = context.sde;

        var left = DIV("type_page_column");
        var mid = DIV("type_page_column");
        var right = DIV("type_page_column");

        Group group = data.getGroups().get(type.groupID);
        if (group == null) {
            throw new NullPointerException("Missing group for type: " + type.name + " (" + type.typeID + ")");
        }
        int categoryID = group.categoryID;

        // Check if type is a Ship, Drone, Citadel or Fighter
        boolean hasRender;
        if (Main.GENERATE_ICONS) {
            hasRender = IconProvider.hasRender(this.type, context.dataSources);
        } else {
            hasRender = categoryID == 6         // Ship
                        || categoryID == 18     // Drone
                        || categoryID == 22     // Deployable
                        || categoryID == 24     // Starbase
                        || categoryID == 65     // Structure
                        || categoryID == 40     // Sov Structure
                        || categoryID == 87;    // Fighter
        }

        if (hasRender) {
            left.content(
                new ItemTitle(type.name, null),
                new TypeGroup(type),
                new ItemRender(type)
            );
        } else {
            left.content(
                new ItemTitle(type.name, getIcon(context)),
                new TypeGroup(type)
            );
        }

        if (type.volume > 0.0 || type.mass > 0.0) {
            left.content(new TypeVolume(type));
        }

        if (type.description != null && type.description.length() > 0)
            left.content(new ItemDescription(EVEText.escape(type.description, context.sde, false)));

        if (data.getTypeTraits().get(type.typeID) != null)
            left.content(new TypeTraitInfo(type));

        Map<Integer, Attribute> attributes = data.getAttributes();
        Map<Integer, Double> typeAttributes = data.getTypeAttributes().getOrDefault(type.typeID, Map.of());

        // Type is a ship or structure
        if (
            categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46 || categoryID == 65
            || type.groupID == 12 || type.groupID == 340 || type.groupID == 448 || type.groupID == 649
        ) {
            if (type.capacity > 0.0 || typeAttributes.keySet().stream().map(attributes::get).anyMatch(attribute -> attribute != null && Objects.equals(attribute.categoryID, 40))) {
                left.content(new ShipCargo(type));
            }
        }

        // Hide health for types that use damage attributes for other purposes, such as modules
        // Ship, Structure, Drone or Fighter
        if (categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46 || categoryID == 65 || categoryID == 18 || categoryID == 87) {
            if (typeAttributes.getOrDefault(263, 0.0) > 0) {
                mid.content(new ShipShield(type));
            }
            if (typeAttributes.getOrDefault(265, 0.0) > 0) {
                mid.content(new ShipArmor(type));
            }
            if (typeAttributes.getOrDefault(9, 0.0) > 0) {
                mid.content(new ShipHull(type));
            }

            int highSlots = typeAttributes.getOrDefault(14, 0.0).intValue();
            int medSlots = typeAttributes.getOrDefault(13, 0.0).intValue();
            int lowSlots = typeAttributes.getOrDefault(12, 0.0).intValue();

            if (highSlots != 0 || medSlots != 0 || lowSlots != 0) {
                mid.content(new ShipFitting(type));
            }

            if (typeAttributes.getOrDefault(37, 0.0) > 0 && categoryID != 18 && categoryID != 22) // Max velocity > 0 && not a drone or deployable
                mid.content(new ShipPropulsion(type));

            // Has targeting range or sensor strength
            if (typeAttributes.getOrDefault(76, 0.0) > 0
                || typeAttributes.getOrDefault(208, 0.0) > 0 || typeAttributes.getOrDefault(209, 0.0) > 0
                || typeAttributes.getOrDefault(210, 0.0) > 0 || typeAttributes.getOrDefault(211, 0.0) > 0)
                mid.content(new TypeSensors(type));

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

        if (group.categoryID == 7 || group.categoryID == 66)
            mid.content(new ModuleFitting(type));

        if (type.groupID == 1964) { // If item type is a Mutaplasmid
            mid.content(new ItemStats(type));
        } else if (categoryID == 7 || categoryID == 8 || categoryID == 18 || categoryID == 20 || categoryID == 22 || categoryID == 32 || categoryID == 66) {    // If module, subsystem, structure module, charge, implant, drone, or deployable
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
            mid.content(new UsedWith("Can be fitted to", canBeFittedToGroups, canBeFittedToTypes, Map.of()));
        }

        HashSet<Integer> usedWithGroups = new HashSet<>();
        for (int attributeID : USED_WITH_GROUP_ATTRIBUTES) {
            Double groupID = typeAttributes.get(attributeID);
            if (groupID != null) {
                usedWithGroups.add((int) (double) groupID);
            }
        }

        double targetChargeSize = typeAttributes.getOrDefault(128, 0.0);
        Map<Integer, Double> usedWithTypes = usedWithGroups.stream()
            .flatMap(g -> data.getGroupTypes().getOrDefault(g, Set.of()).stream())
            .filter(t -> {
                HashSet<Integer> targetUsedWithGroups = new HashSet<>();
                for (int attributeID : USED_WITH_GROUP_ATTRIBUTES) {
                    Double groupID = data.getTypeAttributes().getOrDefault(t.typeID, Map.of()).get(attributeID);
                    if (groupID != null) {
                        targetUsedWithGroups.add((int) (double) groupID);
                    }
                }

                // Exclude charges which cannot fit the module
                if ((categoryID == 7 || categoryID == 66) && t.volume > type.capacity) return false;
                if (categoryID == 8 && t.capacity < type.volume) return false;

                // Exclude charges which do not have the module type set as usedWith
                if (targetUsedWithGroups.contains(type.groupID)) {
                    double chargeSize = data.getTypeAttributes().get(t.typeID).getOrDefault(128, 0.0);
                    // exclude charges with a different chargeSize as this module
                    return chargeSize == targetChargeSize;
                } else {
                    return false;
                }
            })
            .collect(Collectors.toMap(
                t -> t.typeID,
                t -> {
                    if (categoryID == 7 || categoryID == 66) return type.capacity / t.volume;
                    else if (categoryID == 8) return t.capacity / type.volume;
                    else throw new IllegalStateException("Invalid category");
                }
            ));

        if (usedWithTypes.size() > 0) {
            mid.content(new UsedWith("Used with", Set.of(), Set.of(), usedWithTypes));
        }

        if (data.getBpActivities().containsKey(type.typeID) || data.getOutputSchematicMap().containsKey(type.typeID)) {
            mid.content(new TypeBlueprint(type));
        }

        // Has a skill-required-1 attribute
        if (typeAttributes.containsKey(182)) right.content(new TypeSkills(type));

        if (data.getRequiresSkillMap().containsKey(type.typeID))
            right.content(new SkillRequiredFor(type));

        if (data.getVariants().containsKey(type.typeID))  // If type has variants
            right.content(new TypeVariants(type));

        // If produced by a blueprint, used in PI, used in a blueprint, or has reprocessing output
        if (data.getProductActivityMap().containsKey(type.typeID)
            || data.getReprocessingMaterials().containsKey(type.typeID)
            || data.getMaterialActivityMap().containsKey(type.typeID)
            || data.getInputSchematicMap().containsKey(type.typeID)
            || data.getOreReprocessingMap().containsKey(type.typeID)) {
            right.content(new TypeIndustry(type));
        }

        var columns = new ArrayList<HTML>();
        if (!left.isEmpty()) columns.add(left);
        if (!mid.isEmpty()) columns.add(mid);
        if (!right.isEmpty()) columns.add(right);
        return HTML.multi(columns.toArray(HTML[]::new));
    }
}
