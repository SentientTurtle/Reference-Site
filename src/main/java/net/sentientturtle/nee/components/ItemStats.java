package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays item stats for a {@link Type}
 */
public class ItemStats extends Component {
    private final Type type;

    public ItemStats(Type type) {
        super("item_stats colour_theme_minor");
        this.type = type;
    }

    /// Attributes that are directly listed
    private static final int[] LISTED_ATTRIBUTES = new int[]{
        9,      // Structure HP
        11,     // Powergrid Output
        20,     // Maximum Velocity Bonus (Used by prop-mods, drone upgrades, and stasis webs)
        37,     // Maximum Velocity
        39,     // Damage Repaired Bonus
        48,     // CPU Output
        54,     // Optimal range
        56,     // Charges Per Cycle
        64,     // Damage Modifier
        66,     // Duration bonus
        67,     // Capacitor capacity bonus
        68,     // Shield (boost) Bonus
        72,     // Shield HP bonus
        76,     // Maximum Targeting Range
        77,     // Mining amount
        80,     // Speed Bonus
        83,     // Hull HP repaired
        84,     // Armor HP repaired
        90,     // Energy nosferatu/leech amount
        97,     // Energy neutralization amount // TODO: Sustained neut pressure (amount / module activation time), also same with boosters?
        99,     // AOE Radius
        103,    // Warp Disruption Range
        105,    // Warp Scramble Strength
        125,    // Ship scanning range
        126,    // Cargo scan range
        134,    // Shield recharge rate bonus
        142,    // ECM Burst Radius
        143,    // Targeting range
        144,    // Capacitor recharge bonus
        145,    // Power grid bonus
        146,    // Shield HP bonus
        147,    // Capacitor capacity bonus
        148,    // Armor HP bonus
        149,    // Cargo capacity bonus
        150,    // Structure HP bonus
        151,    // Inertia modifier (bonus?)
        158,    // Accuracy falloff
        160,    // Turret tracking
        169,    // Inertia modifier
        188,    // Cargo Scan Resistance
        197,    // Survey Scan Range
        202,    // CPU Output bonus
        204,    // ROF bonus
        213,    // Missile Damage Bonus
        216,    // Capacitor Need Multiplier
        235,    // Max targets bonus
        243,    // Maximum Range Multiplier
        244,    // Tracking Speed Multiplier
        263,    // Shield Capacity  // TODO: Patch to "Bonus" if only used by subsystems and ShipHealth
        265,    // Armor HP
        283,    // Drone Capacity
        292,    // Damage Multiplier Bonus
        293,    // Rate of Fire Bonus
        294,    // Optimal Range Modifier
        306,    // Maximum velocity modifier
        308,    // Scan Speed Bonus
        309,    // Targeting range bonus
        310,    // CPU Need Bonus
        312,    // Duration bonus
        313,    // Power Output Bonus
        314,    // Cap Recharge Time Reduction
        315,    // Velocity Modifier
        317,    // Capacitor need bonus
        318,    // Speed Bonus
        319,    // Warp Capacitor Need Bonus
        323,    // Power Need Bonus
        327,    // HP bonus
        335,    // Armor HP bonus
        337,    // Shield Capacity Bonus
        338,    // Recharge Rate Bonus  TODO: Patch to specify shield if only used by shield modules
        349,    // Falloff Bonus
        351,    // Optimal Range Bonus
        424,    // CPU Output Bonus
        434,    // Mining amount bonus
        435,    // Maximum Active Command Relays
        459,    // Drone Control Range Bonus
        482,    // Capacitor Capacity
        510,    // Ship scan falloff
        547,    // Missile Velocity Bonus
        548,    // Shield boost bonus
        549,    // Powergrid bonus
        552,    // Signature Radius
        557,    // Flight Time Bonus
        566,    // Scan resolution bonus
        567,    // Thrust (Prop mods)
        554,    // Signature radius modifier
        560,    // Sensor Recalibration Time
        565,    // Scan Resolution Bonus
        591,    // Drone Velocity Bonus
        596,    // Flight Time Bonus (Missiles)
        614,    // Cargo Capacity Bonus
        619,    // Cloaking Targeting Delay Bonus
        624,    // Warp Speed Bonus
        653,    // Explosion Velocity
        654,    // Explosion Radius
        767,    // Tracking Speed Bonus
        780,    // Cycle time bonus
        796,    // Mass Addition
        806,    // Repair bonus (Armor?)
        828,    // EW Strength Modifier
        846,    // Scan strength bonus
        847,    // Explosion Velocity Bonus
        848,    // Explosion Radius Bonus
        851,    // Capacitor Need Bonus
        895,    // Armor Repair Bonus
        902,    // Access difficulty bonus
        973,    // Signature radius bonus
        983,    // Signature radius modifier
        1034,   // Cloak Reactivation Delay
        1045,   // Tractor beam velocity
        1076,   // Velocity modifier
        1079,   // Capacitor modifier
        1082,   // CPU Penalty
        1130,   // ECM Strength Bonus
        1131,   // Mass Modifier
        1138,   // Rig drawback
        1159,   // Armor HP bonus
        1160,   // Access Difficulty Bonus Modifier
        1164,   // AB/MWD Max Velocity Bonus
        1190,   // EW Capacitor Need Bonus
        1245,   // Disallow Activation In Warp
        1255,   // Drone Damage Bonus
        1270,   // Afterburner and Microwarpdrive Thrust Bonus
        1271,   // Drone Bandwidth
        1296,   // Consumption quantity bonus   TODO: Rename "consumption" attributes to Fuel
        1368,   // Turret Hardpoint modifier
        1369,   // Launcher Hardpoint Modifier
        1374,   // High Slot Modifier
        1375,   // Medium Slot Modifier
        1376,   // Low Slot Modifier
        1471,   // Mass multiplier
        1536,   // ECM Range Bonus
        1544,   // Max modules in group allowed
        1619,   // Drone Stasis Web Bonus
        1795,   // Reload time TODO: Move to charge section
        1839,   // Damage Delay
        1886,   // Boosted repair multiplier (?)
        1905,   // Scan deviation bonus
        1906,   // Scan duration bonus  (TODO: Rename to include "scan"? Check uses by other types)
        1907,   // Scan strength bonus
        1910,   // Virus Coherence
        1911,   // Virus utility slots
        1915,   // Virus Coherence Bonus
        1918,   // Analyzer Virus Strength Bonus
        1920,   // Virus Strength
        1950,   // Warp speed increase TODO: Set AU/s unit
        1978,   // Global resistance reduction  TODO: Patch name to "Damage resistance reduction"
        2044,   // Effectiveness falloff
        2066,   // Jump Distance
        2067,   // Area Effect Radius
        2253,   // ECM Resistance
        2259,   // Warm-up Neutralization Radius
        2260,   // Warm-up Neutralization Amount
        2261,   // Warm-up Neutralization Signature Radius
        2262,   // Warm up duration
        2263,   // Beam Radius
        2264,   // Beam Duration
        2265,   // Beam Damage Cycle
        2267,   // Neutralizer resistance bonus
        2279,   // AOE Range
        2280,   // AOE Duration
        2281,   // AOE Signature Radius
        2304,   // Torpedo Velocity Bonus
        2305,   // XL Launcher ROF Bonus
        2306,   // Siege Missile Damage Bonus
        2307,   // Turret Damage Bonus
        2335,   // Fighter Shield Bonus
        2336,   // Fighter Velocity Bonus
        2337,   // Fighter ROF Bonus
        2338,   // Fighter Shield Recharge Bonus
        2342,   // Remote Repair Impedance Bonus
        2344,   // Capital Remote Logistics Duration Bonus (Shield / Armor / Hull / Energy)
        2345,   // Capital Remote Logistics Amount Bonus (Shield / Armor / Hull / Energy)
        2346,   // Armor Repairer / Shield Booster Duration Bonus
        2347,   // Armor Repairer / Shield Booster Amount Bonus
        2348,   // Capital Remote Logistics Range Bonus (Shield / Armor / Hull / Energy)
        2351,   // Sensor Dampener Resistance Bonus
        2352,   // Remote Assistance Impedance Bonus
        2353,   // Weapon Disruption Resistance Bonus
        2427,   // Jump/Dock/Tether/Cloak restriction duration
        2428,   // Immobility Duration
        2451,   // Neutralizer signature res TODO: Patch a more user-friendly name
        2535,   // Modifier duration
        2574,   // Command Burst Range Bonus
        2583,   // Drone Damage and Hitpoints Bonus
        2584,   // Drone Maximum Velocity Bonus
        2585,   // Drone Ore Mining Yield Bonus
        2586,   // Drone Ice Harvesting Speed Bonus
        2587,   // Mining Foreman Burst Strength Bonus
        2588,   // Command and Mining Foreman Burst Range Bonus
        2603,   // Maximum Velocity Bonus
        2604,   // Capital Remote Shield Booster Range Bonus
        2605,   // Capital Remote Shield Booster Duration and Capacitor Use Bonus
        2606,   // Shield Booster Duration Bonus
        2607,   // Shield Booster Amount Bonus
        2608,   // Minimum Velocity Limitation
        2665,   // Neutralizer fitting reduction
        2666,   // Medium Hybrid Turret fitting reduction
        2667,   // Medium Projectile Turret Fitting reduction
        2668,   // Medium Energy Turret fitting reduction
        2669,   // Medium Missile Launcher fitting reduction
        2670,   // Medium remote shield rep fitting reduction
        2671,   // Medium remote armor rep fitting reduction
        2688,   // Structure HP Bonus
        2689,   // Cargo Capacity Bonus
        2690,   // Additional Inertia Modifier
        2692,   // Command burst fitting reduction
        2693,   // Remote shield rep falloff bonus
        2694,   // Remote armor repair falloff bonus
        2695,   // Remote armor repair range bonus
        2701,   // Survey probe scan time reduction
        2733,   // Damage Multiplier Bonus Per Cycle
        2734,   // Maximum Damage Multiplier Bonus
        2746,   // Activated Damage Resistance
        2796,   // Repair multiplier bonus per cycle
        2797,   // Maximum repair multiplier
        2821,   // Rapid Torpedo Launcher Bonus
        2832,   // Max jump ships
        3036,   // Vorton Arc Range
        3037,   // Arc Chain Targets
        3108,   // Missile Rate of Fire Bonus
        3109,   // Turret Rate of Fire Bonus
        3113,   // Signature radius bonus
        3114,   // Active signature radius bonus
        3124,   // Drone bandwidth penalty
        3153,   // Residue volume
        3154,   // Residue Probability
        5412,   // Applied Debuff Duration
        5425,   // Disallow Cloaking While Fit
        5426,   // Requires Active Siege Module
        5686,   // Max capital ships jumped
        5687,   // Warp scramble duration

        // Overload Bonuses
        1935, 1936, 1937, 1205, 1206, 1208, 1210, 1222, 1223, 1225, 1230, 1213,
        1074    // Banned in empire space (TODO: Change to special-case with no value, as it's boolean?)
    };

    private static final Set<Integer> SUSTAIN_ATTRIBUTES = Set.of(68, 77, 83, 84, 90, 97);

    /// Attributes that trigger ModuleStats being visible
    /// Consists of LISTED_ATTRIBUTES + special case attributes
    public static final Set<Integer> INCLUDED_ATTRIBUTES;

    static {
        int[] additionalAttributes = new int[]{
            // Module Fuel
            713, 714,
            // Damage
            114, 116, 117, 118,
            // ECM
            241, 240, 238, 239,
            // Shield damage resistance
            271, 274, 273, 272,
            // Armor damage resistance
            267, 270, 269, 268,
            // Hull damage resistance
            974, 977, 976, 975,
            // Damage resistance
            984, 987, 986, 985, // TODO: I don't like how these are "damage resistance bonuses" with negative numbers. Maybe change their name or units?
            // "ECCM" Sensor bonus
            1030, 1029, 1027, 1028,
        };

        INCLUDED_ATTRIBUTES = Stream.concat(Arrays.stream(LISTED_ATTRIBUTES).boxed(), Arrays.stream(additionalAttributes).boxed()).collect(Collectors.toSet());
    }

    private void tryRow(HtmlContext context, Element table, String title, int... attributes) {
        Map<Integer, Double> typeAttributes = context.data.getTypeAttributes().getOrDefault(type.typeID, Map.of());
        for (int attributeID : attributes) {
            Attribute a = context.data.getAttributes().get(attributeID);
            Double v = typeAttributes.get(attributeID);

            if (v != null && v != a.defaultValue()) {
                table.content(TR().content(TD("item_stats_multirow").attribute("colspan", "2").content(
                    DIV().text(title),
                    SPAN("item_stats_multirow_content").content(
                        Arrays.stream(attributes).mapToObj(id -> {
                            Attribute attr = context.data.getAttributes().get(id);
                            Integer iconID = attr.iconID;
                            Double attributeValue = typeAttributes.getOrDefault(id, attr.defaultValue());   // TODO: Replace all getOrDefault(attributeID, 0.0) with getOrDefault attribute.defaultValue()!!!!

                            return SPAN("item_stats_span")
                                .title(attr.displayName != null ? attr.displayName : attr.attributeName)
                                .attribute("aria-label", attr.displayName != null ? attr.displayName : attr.attributeName)
                                .content(
                                    iconID != null ? IMG(ResourceLocation.iconOfIconID(iconID), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
                                    context.data.format_with_unit(attributeValue, attr.unitID)
                                );
                        })
                    )
                )));

                break;
            }
        }
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Attribute> attributes = context.data.getAttributes();
        Map<Integer, Double> typeAttributes = context.data.getTypeAttributes().getOrDefault(type.typeID, Map.of());

        var table = TABLE("item_stats_table");

        tryRow(context, table, "Damage", 114, 118, 117, 116);
        tryRow(context, table, "Damage Resistance Bonus", 984, 987, 986, 985);
        tryRow(context, table, "Shield Damage Resistance Bonus", 271, 274, 273, 272);
        tryRow(context, table, "Armor Damage Resistance Bonus", 267, 270, 269, 268);
        tryRow(context, table, "Hull Damage Resistance Bonus", 974, 977, 976, 975);
        tryRow(context, table, "ECM Jammer Strength", 241, 240, 238, 239);
        tryRow(context, table, "Sensor Strength Bonus", 1030, 1029, 1027, 1028);

        double activationTime = type.getModuleActivationTime(context.data);

        // Slightly inefficient, but we want the ordering of LISTED_ATTRIBUTES
        for (int attributeID : LISTED_ATTRIBUTES) {
            Attribute attribute = attributes.get(attributeID);
            Double attributeValue = typeAttributes.get(attributeID);
            if (attributeValue != null && attributeValue != attribute.defaultValue()) {
                Integer iconID = attribute.iconID;
                String name = attribute.displayName != null ? attribute.displayName : attribute.attributeName;

                var valueTD = TD();
                if (SUSTAIN_ATTRIBUTES.contains(attributeID) && activationTime > 0.0) {
                    double reactivationDelay = typeAttributes.getOrDefault(669, 0.0);

                    valueTD.content(
                        context.data.format_with_unit(attributeValue, attribute.unitID),
                        TEXT(" "),
                        SPAN("no_break").content(
                            TEXT("("),
                            context.data.format_with_unit(attributeValue / ((activationTime + reactivationDelay) / 1000.0), attribute.unitID),
                            TEXT("/s)")
                        )
                    );
                } else {
                    valueTD.content(context.data.format_with_unit(attributeValue, attribute.unitID));
                }

                table.content(TR().content(
                    TD().content(SPAN("item_stats_span").title(name).content(
                            iconID != null ? IMG(ResourceLocation.iconOfIconID(iconID), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
                            TEXT(name + ":")
                        )
                    ),
                    valueTD
                ));
            }
        }

        Double fuelType;
        if ((fuelType = typeAttributes.get(713)) != null) {
            double fuelQuantity = typeAttributes.getOrDefault(714, 0.0);

            Integer iconID = context.data.getTypes().get((int) (double) fuelType).iconID;
            table.content(TR().content(
                TD().content(SPAN("item_stats_span").title("Fuel required").content(
                        iconID != null ? IMG(ResourceLocation.iconOfIconID(iconID), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
                        TEXT("Fuel required:")
                    )
                ),
                TD().content(
                    context.data.format_with_unit(fuelType, attributes.get(713).unitID),
                    TEXT(" "),
                    SPAN("no_break").content(
                        TEXT("("),
                        context.data.format_with_unit(fuelQuantity, attributes.get(714).unitID),
                        TEXT(")")
                    )
                )
            ));
        }

        Element mutationsTable = TABLE("item_stats_table");
        FSDData.DynamicAttributes dynamicAttributes = context.fsdData.dynamicAttributes.get(type.typeID);
        if (dynamicAttributes != null) {
            mutationsTable.content(TR().content(TH().attribute("colspan", "2").text("Mutations")));
            for (Map.Entry<Integer, FSDData.DyAttribute> entry : dynamicAttributes.attributeIDs().entrySet()) {
                int attributeID = entry.getKey();

                if (INCLUDED_ATTRIBUTES.contains(attributeID) || ModuleFitting.INCLUDED_ATTRIBUTES.contains(attributeID)) {
                    Attribute attribute = attributes.get(attributeID);
                    Integer iconID = attribute.iconID;
                    String name = attribute.displayName != null ? attribute.displayName : attribute.attributeName;

                    double bad;
                    double good;
                    String order;

                    Boolean highIsGoodOverride = entry.getValue().highIsGood();
                    if (highIsGoodOverride != null ? highIsGoodOverride : attribute.highIsGood) {
                        bad = entry.getValue().min();
                        good = entry.getValue().max();
                        order = " ≤ \uD83C\uDFB2 ≤ ";
                    } else {
                        good = entry.getValue().min();
                        bad = entry.getValue().max();
                        order = " ≥ \uD83C\uDFB2 ≥ ";
                    }

                    mutationsTable.content(TR().content(
                        TD().content(SPAN("item_stats_span").title(name).content(
                                iconID != null ? IMG(ResourceLocation.iconOfIconID(iconID), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
                                TEXT(name + ":")
                            )
                        ),
                        TD().content(context.data.format_with_unit(bad, 109)),
                        TD().attribute("aria-hidden", "true").content(TEXT(order)),
                        TD().content(context.data.format_with_unit(good, 109))
                    ));
                }
            }
        }


        String itemKind = context.data.getCategories().get(context.data.getGroups().get(type.groupID).categoryID).name;
        return new HTML[]{
            HEADER("font_header").text(itemKind + " stats"),
            table.isEmpty() ? HTML.empty() : table,
            mutationsTable.isEmpty() ? HTML.empty() : mutationsTable,
        };
    }

    @Override
    protected String getCSS() {
        return """
            .item_stats {
                padding: 0.5rem;
            }
            
            .item_stats_table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 0.5rem;
            }
            
            .item_stats_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .item_stats_multirow_content {
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: space-between;
            }
            
            .item_stats_span {
                display: flex;
                align-items: center;
                gap: 0.25rem;
            }
            
            .item_stats_icon {
                display: inline;
                width: 2rem;
                height: 2rem;
            }""";
    }
}
