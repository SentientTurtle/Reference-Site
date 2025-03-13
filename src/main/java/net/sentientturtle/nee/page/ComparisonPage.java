package net.sentientturtle.nee.page;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.components.*;
import net.sentientturtle.nee.data.ResourceLocation;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sde.SDEData;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.sentientturtle.html.HTML.*;

public class ComparisonPage extends Page {
    private final Type parentType;

    public ComparisonPage(Type parentType) {
        this.parentType = Objects.requireNonNull(parentType);
    }

    @Override
    public String name() {
        return parentType.name + ":Compare";
    }

    @Override
    public String filename() {
        return String.valueOf(parentType.typeID);
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.Comparison;
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.typeIcon(parentType.typeID, context);
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .comparison_grid {
                display: grid;
                gap: 0.5rem;
            }
            
            #content:has(.comparison_grid) {
                justify-content: space-around;
            }
            
            .comparison_grid > * {
                max-width: calc(32rem + (2 * var(--border-size)));
                box-sizing: border-box;
            }
            """;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        SDEData data = context.sde;
        var variants = data.getVariants().get(parentType.typeID);
        int NUM_ROWS = 18;   // Maximum possible amount of items added to a column
        int NUM_COLUMNS = variants.size();

        AtomicInteger currentColumn = new AtomicInteger(0);
        LinkedHashSet<String> usedRows = new LinkedHashSet<>(20);

        return DIV("comparison_grid")
            .content(
                variants.stream()
                    .sorted(Type.idComparator(data))
                    .flatMap(typeID -> {
                        // This is a lightly edited version of TypePage#getContent
                        int currentCol = currentColumn.incrementAndGet();

                        Type type = data.getTypes().get(typeID);
                        Group group = data.getGroups().get(type.groupID);
                        if (group == null) throw new NullPointerException("Missing group for type: " + type.name + " (" + type.typeID + ")");
                        int categoryID = group.categoryID;

                        Map<Integer, Attribute> attributes = data.getAttributes();
                        Map<Integer, Double> typeAttributes = data.getTypeAttributes().getOrDefault(type.typeID, Map.of());

                        ArrayList<HTML> column = new ArrayList<>(NUM_ROWS);
                        column.add(
                            new ItemTitle(new PageLink(new TypePage(type)), ResourceLocation.typeIcon(type.typeID, context))
                                .style(String.format("grid-column: %d; grid-row: title;", currentCol))
                        );
                        usedRows.add("title");

                        if (type.volume > 0.0 || type.mass > 0.0) {
                            column.add(new TypeVolume(type).style(String.format("grid-column: %d; grid-row: volume;", currentCol)));
                            usedRows.add("volume");
                        }
                        if (data.getTypeTraits().get(type.typeID) != null) {
                            column.add(new TypeTraitInfo(type).style(String.format("grid-column: %d; grid-row: traits;", currentCol)));
                            usedRows.add("traits");
                        }

                        // Type is a ship or structure
                        if (
                            categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46 || categoryID == 65
                            || type.groupID == 12 || type.groupID == 340 || type.groupID == 448 || type.groupID == 649
                        ) {
                            if (type.capacity > 0.0 || typeAttributes.keySet().stream().map(attributes::get).anyMatch(attribute -> attribute != null && Objects.equals(attribute.categoryID, 40))) {
                                column.add(new ShipCargo(type).style(String.format("grid-column: %d; grid-row: cargo;", currentCol)));
                                usedRows.add("cargo");
                            }
                        }

                        // Hide health for types that use damage attributes for other purposes, such as modules
                        // Ship, Structure, Drone or Fighter
                        if (categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46 || categoryID == 65 || categoryID == 18 || categoryID == 87) {
                            if (typeAttributes.getOrDefault(263, 0.0) > 0) {
                                column.add(new ShipShield(type).style(String.format("grid-column: %d; grid-row: shield;", currentCol)));
                                usedRows.add("shield");
                            }
                            if (typeAttributes.getOrDefault(265, 0.0) > 0) {
                                column.add(new ShipArmor(type).style(String.format("grid-column: %d; grid-row: armor;", currentCol)));
                                usedRows.add("armor");
                            }
                            if (typeAttributes.getOrDefault(9, 0.0) > 0) {
                                column.add(new ShipHull(type).style(String.format("grid-column: %d; grid-row: hull;", currentCol)));
                                usedRows.add("hull");
                            }

                            int highSlots = typeAttributes.getOrDefault(14, 0.0).intValue();
                            int medSlots = typeAttributes.getOrDefault(13, 0.0).intValue();
                            int lowSlots = typeAttributes.getOrDefault(12, 0.0).intValue();

                            if (highSlots != 0 || medSlots != 0 || lowSlots != 0) {
                                column.add(new ShipFitting(type).style(String.format("grid-column: %d; grid-row: fitting;", currentCol)));
                                usedRows.add("fitting");
                            }

                            if (typeAttributes.getOrDefault(37, 0.0) > 0 && categoryID != 18 && categoryID != 22) { // Max velocity > 0
                                column.add(new ShipPropulsion(type).style(String.format("grid-column: %d; grid-row: propulsion;", currentCol)));
                                usedRows.add("propulsion");
                            }

                            // Has targeting range or sensor strength
                            if (typeAttributes.getOrDefault(76, 0.0) > 0
                                || typeAttributes.getOrDefault(208, 0.0) > 0 || typeAttributes.getOrDefault(209, 0.0) > 0
                                || typeAttributes.getOrDefault(210, 0.0) > 0 || typeAttributes.getOrDefault(211, 0.0) > 0
                            ) {
                                column.add(new TypeSensors(type).style(String.format("grid-column: %d; grid-row: sensors;", currentCol)));
                                usedRows.add("sensors");
                            }

                            if (categoryID == 6 || categoryID == 22 || categoryID == 23 || categoryID == 40 || categoryID == 46) {
                                if (typeAttributes.getOrDefault(2217, 0.0) > 0         // Light fighter slots
                                    || typeAttributes.getOrDefault(2218, 0.0) > 0  // Support fighter slots
                                    || typeAttributes.getOrDefault(2219, 0.0) > 0  // Heavy fighter slots
                                ) {
                                    column.add(new ShipFighters(type).style(String.format("grid-column: %d; grid-row: fighters;", currentCol)));
                                    usedRows.add("fighters");
                                }

                                if (typeAttributes.getOrDefault(2737, 0.0) > 0  // Standup light fighter slots
                                    || typeAttributes.getOrDefault(2738, 0.0) > 0  // Standup support fighter slots
                                    || typeAttributes.getOrDefault(2739, 0.0) > 0 // Standup heavy fighter slots
                                ) {
                                    column.add(new ShipFightersStandup(type).style(String.format("grid-column: %d; grid-row: fighters;", currentCol)));
                                    usedRows.add("fighters");   // Mutually exclusive with other fighters
                                }

                                if (typeAttributes.getOrDefault(1271, 0.0) > 0) { // Drone bandwidth > 0
                                    column.add(new ShipDrones(type).style(String.format("grid-column: %d; grid-row: drones;", currentCol)));
                                    usedRows.add("drones");
                                }
                            }
                        }

                        if (group.categoryID == 7 || group.categoryID == 66) {
                            column.add(new ModuleFitting(type).style(String.format("grid-column: %d; grid-row: fitting;", currentCol)));
                            usedRows.add("fitting"); // mutually exclusive to ship-fitting, so use same name
                        }

                        if (type.groupID == 1964) { // If item type is a Mutaplasmid
                            column.add(new ItemStats(type).style(String.format("grid-column: %d; grid-row: stats;", currentCol)));
                            usedRows.add("stats");
                        } else if (categoryID == 7 || categoryID == 8 || categoryID == 18 || categoryID == 20 || categoryID == 22 || categoryID == 32 || categoryID == 66) {    // If module, subsystem, structure module, charge, implant, drone, or deployable
                            for (int listedAttribute : ItemStats.INCLUDED_ATTRIBUTES) {
                                if (typeAttributes.containsKey(listedAttribute) && listedAttribute != 9) {
                                    column.add(new ItemStats(type).style(String.format("grid-column: %d; grid-row: stats;", currentCol)));
                                    usedRows.add("stats");
                                    break;
                                }
                            }
                        }

                        HashSet<Integer> canBeFittedToGroups = new HashSet<>();
                        HashSet<Integer> canBeFittedToTypes = new HashSet<>();
                        for (int attributeID : TypePage.CAN_BE_FITTED_TO_GROUP_ATTRIBUTES) {
                            Double groupID = typeAttributes.get(attributeID);
                            if (groupID != null) {
                                canBeFittedToGroups.add((int) (double) groupID);
                            }
                        }

                        for (int attributeID : TypePage.CAN_BE_FITTED_TO_TYPE_ATTRIBUTES) {
                            Double targetTypeID = typeAttributes.get(attributeID);
                            if (targetTypeID != null) {
                                canBeFittedToTypes.add((int) (double) targetTypeID);
                            }
                        }

                        if (canBeFittedToGroups.size() > 0 || canBeFittedToTypes.size() > 0) {
                            column.add(new UsedWith("Can be fitted to", canBeFittedToGroups, canBeFittedToTypes, Map.of()).style(String.format("grid-column: %d; grid-row: cbft;", currentCol)));
                            usedRows.add("cbft");
                        }

                        HashSet<Integer> usedWithGroups = new HashSet<>();
                        for (int attributeID : TypePage.USED_WITH_GROUP_ATTRIBUTES) {
                            Double groupID = typeAttributes.get(attributeID);
                            if (groupID != null) {
                                usedWithGroups.add((int) (double) groupID);
                            }
                        }

                        Double targetChargeSize = typeAttributes.get(128);

                        Map<Integer, Double> usedWithTypes = usedWithGroups.stream()
                            .flatMap(g -> data.getGroupTypes().getOrDefault(g, Set.of()).stream())
                            .filter(t -> {
                                HashSet<Integer> targetUsedWithGroups = new HashSet<>();
                                for (int attributeID : TypePage.USED_WITH_GROUP_ATTRIBUTES) {
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
                                    Double chargeSize = data.getTypeAttributes().get(t.typeID).get(128);
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
                            .collect(Collectors.toMap(
                                t -> t.typeID,
                                t -> {
                                    if (categoryID == 7 || categoryID == 66) return type.capacity / t.volume;
                                    else if (categoryID == 8) return t.capacity / type.volume;
                                    else throw new IllegalStateException("Invalid category");
                                }
                            ));

                        if (usedWithTypes.size() > 0) {
                            column.add(new UsedWith("Used with", Set.of(), Set.of(), usedWithTypes).style(String.format("grid-column: %d; grid-row: usedwith;", currentCol)));
                            usedRows.add("usedwith");
                        }

                        // Has a skill-required-1 attribute
                        if (typeAttributes.containsKey(182)) {
                            column.add(new TypeSkills(type).style(String.format("grid-column: %d; grid-row: skills;", currentCol)));
                            usedRows.add("skills");
                        }

                        return column.stream();
                    })
            )
            .style(String.format("grid-template-columns: repeat(%d, auto); grid-template-rows: %s", NUM_COLUMNS, usedRows.stream().map(row -> "[" + row + "] auto").collect(Collectors.joining(" "))));
    }
}
