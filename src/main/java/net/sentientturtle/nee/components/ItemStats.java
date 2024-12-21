package net.sentientturtle.nee.components;

import net.sentientturtle.html.Component;
import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Attribute;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.data.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sentientturtle.html.HTML.*;

/**
 * Displays item stats for a {@link Type}
 */
public class ItemStats extends Component {
    public static final int[] WARFARE_BUFF_ATTRIBUTES = {2468, 2470, 2472, 2536};
    public static final int[] WARFARE_BUFF_MULTIPLIER_ATTRIBUTES = {2596, 2597, 2598, 2599};
    public static final int[] WARFARE_BUFF_VALUE_ATTRIBUTES = {2469, 2471, 2473, 2537};
    private final Type type;

    public ItemStats(Type type) {
        super("item_stats colour_theme_minor");
        this.type = type;
    }

    /// Attributes that are directly listed
    private static final int[] LISTED_ATTRIBUTES = new int[]{
        9,      // Structure HP     Special-cased to only be shown on modules with hull resistance
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
        70,     // Inertia modifier
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
        120,    // Range bonus
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
        175,    // Charisma Modifier
        176,    // Intelligence Modifier
        177,    // Memory Modifier
        178,    // Perception Modifier
        179,    // Willpower Modifier
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
        281,    // Maximum flight time
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
        330,    // Booster Duration
        331,    // Implant Slot
        335,    // Armor HP bonus
        337,    // Shield Capacity Bonus
        338,    // Recharge Rate Bonus  TODO: Patch to specify shield if only used by shield modules
        349,    // Falloff Bonus
        351,    // Optimal Range Bonus
        379,    // Refining Yield Mutator
        424,    // CPU Output Bonus
        434,    // Mining amount bonus
        435,    // Maximum Active Command Relays
        440,    // Manufacturing Time Bonus
        441,    // Rate of Fire Bonus
        447,    // Smuggling Chance Bonus
        452,    // Copy Speed Bonus
        453,    // Blueprint Manufacture Time Bonus
        459,    // Drone Control Range Bonus
        468,    // Mineral Need Research Bonus
        482,    // Capacitor Capacity
        510,    // Ship scan falloff
        517,    // Falloff Modifier
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
        600,    // Warp Speed Multiplier (scanner probes)
        612,    // Base Shield Damage   TODO: Maybe remove, if irrelevant
        613,    // Base Armor Damage
        614,    // Cargo Capacity Bonus
        616,    // Shield Booster Penalty
        619,    // Cloaking Targeting Delay Bonus
        624,    // Warp Speed Bonus
        653,    // Explosion Velocity
        654,    // Explosion Radius
        767,    // Tracking Speed Bonus
        780,    // Cycle time bonus
        782,    // Asteroid Specialization Yield Modifier
        785,    // Unfitting Capacitor Cost
        796,    // Mass Addition
        799,    // Talisman Set Bonus
        802,    // Snake Set Bonus
        803,    // Asklepian Set Bonus
        806,    // Repair bonus (Armor?)
        828,    // EW Strength Modifier
        838,    // Crystal Set Bonus
        846,    // Scan strength bonus
        847,    // Explosion Velocity Bonus
        848,    // Explosion Radius Bonus
        851,    // Capacitor Need Bonus
        863,    // Halo Set Bonus
        864,    // Amulet Set Bonus
        884,    // Mindlink Bonus
        895,    // Armor Repair Bonus
        902,    // Access difficulty bonus
        927,    // CPU Penalty Reduction
        973,    // Signature radius bonus
        983,    // Signature radius modifier
        1034,   // Cloak Reactivation Delay
        1045,   // Tractor beam velocity
        1076,   // Velocity modifier
        1079,   // Capacitor modifier
        1082,   // CPU Penalty
        1083,   // Armor Hitpoint Bonus
        1084,   // Velocity Modifier
        1087,   // Booster Slot
        1089,   // Chance of Side Effect
        1125,   // Negative Side Effect Chance Bonus
        1126,   // Side effect Modifier
        1130,   // ECM Strength Bonus
        1131,   // Mass Modifier
        1138,   // Rig drawback
        1141,   // Armor Hitpoint Penalty
        1142,   // Armor Repair Amount Penalty
        1143,   // Shield Capacity Penalty
        1144,   // Turret Optimal Range Penalty
        1145,   // Turret Tracking Penalty
        1146,   // Turret Falloff Penalty
        1147,   // Explosion Velocity Penalty
        1148,   // Missile Velocity Penalty
        1149,   // Missile Explosion Radius Penalty
        1150,   // Capacitor Capacity Penalty
        1151,   // Velocity Penalty
        1156,   // Maximum Scan Deviation Modifier
        1159,   // Armor HP bonus
        1160,   // Access Difficulty Bonus Modifier
        1164,   // AB/MWD Max Velocity Bonus
        1190,   // EW Capacitor Need Bonus
        1227,   // Modification of Signature Radius Bonus
        1245,   // Disallow Activation In Warp
        1255,   // Drone Damage Bonus
        1270,   // Afterburner and Microwarpdrive Thrust Bonus
        1271,   // Drone Bandwidth
        1282,   // Nomad Set Bonus
        1284,   // Virtue Set Bonus
        1291,   // Edge Set Bonus
        1292,   // Harvest Set Bonus
        1293,   // Centurion Set Bonus
        1296,   // Consumption quantity bonus   TODO: Rename "consumption" attributes to Fuel
        1313,   // Modification of Maximum Targeting Range Bonus
        1314,   // Modification of Scan Resolution Bonus
        1315,   // Modification of Optimal Range Bonus
        1316,   // Modification of Tracking Speed Bonus
        1327,   // Warp Scrambler Range Bonus
        1332,   // Modification of Falloff Bonus
        1368,   // Turret Hardpoint modifier
        1369,   // Launcher Hardpoint Modifier
        1370,   // Base Scan Range
        1371,   // Base Sensor Strength
        1372,   // Base Maximum Deviation
        1373,   // Scan Range Increment Factor
        1374,   // High Slot Modifier
        1375,   // Medium Slot Modifier
        1376,   // Low Slot Modifier
        1471,   // Mass multiplier
        1536,   // ECM Range Bonus
        1544,   // Max modules in group allowed
        1619,   // Drone Stasis Web Bonus
        1647,   // Maximum Pilot Age
        1795,   // Reload time TODO: Move to charge section
        1799,   // Genolution Set Bonus
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
        1932,   // Ascendancy Set bonus
        1950,   // Warp speed increase TODO: Set AU/s unit
        1978,   // Global resistance reduction  TODO: Patch name to "Damage resistance reduction"
        2023,   // Modification of Explosion Radius Bonus
        2024,   // Modification of Explosion Velocity Bonus
        2025,   // Modification of Missile Velocity Bonus
        2026,   // Modification of Flight Time Bonus
        2044,   // Effectiveness falloff
        2066,   // Jump Distance
        2067,   // Area Effect Radius
        2072,   // Modification of Gravimetric Strength Bonus
        2073,   // Modification of Ladar Strength Bonus
        2074,   // Modification of Magnetometric Strength Bonus
        2075,   // Modification of Radar Strength Bonus
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
        2282,   // Modification of Sensor Strength Bonus
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
        2402,   // Modification of EM Damage Resistance Bonus
        2403,   // Modification of Explosive Damage Resistance Bonus
        2404,   // Modification of Kinetic Damage Resistance Bonus
        2405,   // Modification of Thermal Damage Resistance Bonus
        2422,   // Expiry Date
        2427,   // Jump/Dock/Tether/Cloak restriction duration
        2428,   // Immobility Duration
        2747,   // Stasis Webifier Maximum Range Bonus
        2451,   // Neutralizer signature res TODO: Patch a more user-friendly name
        2457,   // Armor Repair Bonus
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
        2697,   // Maximum Scan Range
        2700,   // Maximum Auto-Targeting Range
        2701,   // Survey probe scan time reduction
        2733,   // Damage Multiplier Bonus Per Cycle
        2734,   // Maximum Damage Multiplier Bonus
        2746,   // Activated Damage Resistance
        2796,   // Repair multiplier bonus per cycle
        2797,   // Maximum repair multiplier
        2821,   // Rapid Torpedo Launcher Bonus
        2823,   // Maximum Damage Bonus Multiplier Modifier
        2824,   // Damage Multiplier Bonus Per Cycle Modifier
        2825,   // Implant Set Bonus
        2832,   // Max jump ships
        3015,   // Shield Hitpoint Bonus
        3017,   // Nirvana Set Bonus
        3023,   // Savior Set Bonus
        3024,   // Remote Rep Cycle Time Bonus
        3027,   // Hydra Set Bonus
        3028,   // Drone Tracking Speed Bonus
        3029,   // Drone Optimal and Falloff Range Bonus
        3030,   // Missile Flight Time Bonus
        3031,   // Missile Explosion Velocity Bonus
        3036,   // Vorton Arc Range
        3037,   // Arc Chain Targets
        3107,   // Rapture Set Bonus
        3108,   // Missile Rate of Fire Bonus
        3109,   // Turret Rate of Fire Bonus
        3113,   // Signature radius bonus
        3114,   // Active signature radius bonus
        3124,   // Drone bandwidth penalty
        3134,   // Stabilized Cloak Duration Bonus
        3148,   // Valid target types
        3153,   // Residue volume
        3154,   // Residue Probability
        3159,   // Residue Volume Multiplier Bonus
        3160,   // Residue Probability Bonus
        3161,   // Asteroid Specialization Duration Multiplier
        3206,   // Stasis Webifier Maximum Range Bonus
        3353,   // Drone Tracking Speed Bonus
        5412,   // Applied Debuff Duration
        5425,   // Disallow Cloaking While Fit
        5426,   // Requires Active Siege Module
        5686,   // Max capital ships jumped
        5687,   // Warp scramble duration
        5735,   // Damage effect duration
        5736,   // Maximum damage per second
        5737,   // Maximum percentage of target HP damaged per second TODO: Patch this to be shorter

        // Overload Bonuses
        1935, 1936, 1937, 1205, 1206, 1208, 1210, 1222, 1223, 1225, 1230, 1213,
        1074    // Banned in empire space
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
            // Hull damage resistance
            113, 110, 109, 111,
            // Passive Damage Resistance Bonus
            994, 997, 996, 995,
            // Damage resistance
            984, 987, 986, 985, // TODO: I don't like how these are "damage resistance bonuses" with negative numbers. Maybe change their name or units?
            // "ECCM" Sensor bonus
            1030, 1029, 1027, 1028,
            // Implant "ECCM" Sensor bonus
            1565, 1568, 1567, 1566,
            // Warfare buffs
            2468, 2470, 2472, 2536,
            // Crystal volatility
            783, 784, 786
        };

        INCLUDED_ATTRIBUTES = Stream.concat(Arrays.stream(LISTED_ATTRIBUTES).boxed(), Arrays.stream(additionalAttributes).boxed()).collect(Collectors.toSet());
    }

    private boolean tryRow(HtmlContext context, Element table, String title, int... attributes) {
        Map<Integer, Double> typeAttributes = context.sde.getTypeAttributes().getOrDefault(type.typeID, Map.of());
        for (int attributeID : attributes) {
            Attribute a = context.sde.getAttributes().get(attributeID);
            Double v = typeAttributes.get(attributeID);

            if (v != null && v != a.defaultValue()) {
                table.content(TR().content(TD("item_stats_multirow").attribute("colspan", "2").content(
                    DIV().text(title),
                    SPAN("item_stats_multirow_content").content(
                        Arrays.stream(attributes).mapToObj(id -> {
                            Attribute attr = context.sde.getAttributes().get(id);
                            Integer iconID = attr.iconID;
                            Double attributeValue = typeAttributes.getOrDefault(id, attr.defaultValue());

                            return SPAN("item_stats_span")
                                .title(attr.displayName != null ? attr.displayName : attr.attributeName)
                                .attribute("aria-label", attr.displayName != null ? attr.displayName : attr.attributeName)
                                .content(
                                    iconID != null ? IMG(ResourceLocation.ofIconID(iconID, context), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
                                    context.sde.format_with_unit(attributeValue, attr.unitID)
                                );
                        })
                    )
                )));

                return true;
            }
        }
        return false;
    }

    @Override
    protected HTML[] getContent(HtmlContext context) {
        Map<Integer, Attribute> attributes = context.sde.getAttributes();
        Map<Integer, Double> typeAttributes = context.sde.getTypeAttributes().getOrDefault(type.typeID, Map.of());

        var table = TABLE("item_stats_table");

        boolean showHullHP = false;
        tryRow(context, table, "Damage", 114, 118, 117, 116);
        tryRow(context, table, "Damage Resistance Bonus", 984, 987, 986, 985);
        tryRow(context, table, "Damage Resistance Bonus", 994, 997, 996, 995);  // TODO: Ingame this is named "Passive Resistance Bonus", maybe change?
        tryRow(context, table, "Shield Damage Resistance", 271, 274, 273, 272);
        showHullHP |= tryRow(context, table, "Armor Damage Resistance", 267, 270, 269, 268);
        showHullHP |= tryRow(context, table, "Hull Damage Resistance", 974, 977, 976, 975);
        showHullHP |= tryRow(context, table, "Hull Damage Resistance", 113, 110, 109, 111);
        tryRow(context, table, "ECM Jammer Strength", 241, 240, 238, 239);
        tryRow(context, table, "Sensor Strength Bonus", 1030, 1029, 1027, 1028);
        tryRow(context, table, "Sensor Strength Bonus", 1565, 1568, 1567, 1566);

        // Laser/Mining crystals
        {
            Double crystalsTakeDamage = typeAttributes.get(786);
            Double volatilityDamage = typeAttributes.get(784);
            if (crystalsTakeDamage != null && (crystalsTakeDamage != 1.0 || volatilityDamage != null)) {
                String name;
                HTML cycles;
                if (crystalsTakeDamage == 1.0) {
                    double crystalHP = typeAttributes.get(9);
                    double crystalVolatility = typeAttributes.get(783);

                    if (crystalVolatility == 1.0) {
                        name = "Crystal Lifespan";
                    } else {
                        name = "Average Crystal Lifespan";
                    }

                    double totalCycles = (crystalHP / volatilityDamage) / crystalVolatility;
                    cycles = context.sde.format_with_unit(totalCycles, -1);
                } else {
                    name = "Crystal Lifespan";
                    cycles = TEXT("∞");
                }
                table.content(TR().content(
                    TD().content(SPAN("item_stats_span").title(name).content(
                            DIV("item_stats_icon"),
                            TEXT(name + ":")
                        )
                    ),
                    TD().content(cycles, TEXT(" cycles"))
                ));
            }
        }

        double activationTime = type.getModuleActivationTime(context.sde);

        // Slightly inefficient, but we want the ordering of LISTED_ATTRIBUTES
        for (int attributeID : LISTED_ATTRIBUTES) {
            if (attributeID == 9 && !showHullHP) continue;
            Attribute attribute = attributes.get(attributeID);
            Double attributeValue = typeAttributes.get(attributeID);
            if (attributeValue != null && attributeValue != attribute.defaultValue()) {
                Integer iconID = attribute.iconID;
                String name = attribute.displayName != null ? attribute.displayName : attribute.attributeName;

                var valueTD = TD();
                if (SUSTAIN_ATTRIBUTES.contains(attributeID) && activationTime > 0.0) {
                    double reactivationDelay = typeAttributes.getOrDefault(669, 0.0);

                    valueTD.content(
                        context.sde.format_with_unit(attributeValue, attribute.unitID),
                        TEXT(" "),
                        SPAN("no_break").content(
                            TEXT("("),
                            context.sde.format_with_unit(attributeValue / ((activationTime + reactivationDelay) / 1000.0), attribute.unitID),
                            TEXT("/s)")
                        )
                    );
                } else {
                    valueTD.content(context.sde.format_with_unit(attributeValue, attribute.unitID));
                }

                table.content(TR().content(
                    TD().content(SPAN("item_stats_span").title(name).content(
                            iconID != null ? IMG(ResourceLocation.ofIconID(iconID, context), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
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

            Integer iconID = context.sde.getTypes().get((int) (double) fuelType).iconID;
            table.content(TR().content(
                TD().content(SPAN("item_stats_span").title("Fuel required").content(
                        iconID != null ? IMG(ResourceLocation.ofIconID(iconID, context), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
                        TEXT("Fuel required:")
                    )
                ),
                TD().content(
                    context.sde.format_with_unit(fuelType, attributes.get(713).unitID),
                    TEXT(" "),
                    SPAN("no_break").content(
                        TEXT("("),
                        context.sde.format_with_unit(fuelQuantity, attributes.get(714).unitID),
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
                                iconID != null ? IMG(ResourceLocation.ofIconID(iconID, context), null, 32).className("item_stats_icon") : DIV("item_stats_icon"),
                                TEXT(name + ":")
                            )
                        ),
                        TD().content(context.sde.format_with_unit(bad, 109)),
                        TD().attribute("aria-hidden", "true").content(TEXT(order)),
                        TD().content(context.sde.format_with_unit(good, 109))
                    ));
                }
            }
        }

        Element warfareBuffTable = TABLE("item_stats_table");
        for (int i = 0; i < WARFARE_BUFF_ATTRIBUTES.length; i++) {
            int warfareBuffID = (int) (double) typeAttributes.getOrDefault(WARFARE_BUFF_ATTRIBUTES[i], 0.0);
            if (warfareBuffID > 0) {
                double buffAmount;
                Double buffMultiplier = typeAttributes.get(WARFARE_BUFF_MULTIPLIER_ATTRIBUTES[i]);
                Double buffValue = typeAttributes.get(WARFARE_BUFF_VALUE_ATTRIBUTES[i]);

                if (buffMultiplier != null) {
                    buffAmount = buffMultiplier;
                } else {
                    buffAmount = buffValue;
                }

                FSDData.WarfareBuff warfareBuff = context.fsdData.warfareBuffs.get(warfareBuffID);
                Element valueTD = TD();
                switch (warfareBuff.showOutputValueInUI()) {
                    case "ShowNormal" -> valueTD.content(context.sde.format_with_unit(buffAmount, 124));
                    case "ShowInverted" -> valueTD.content(context.sde.format_with_unit(-buffAmount, 124));
                    default -> throw new RuntimeException("Unknown warfare buff display : " + warfareBuff.showOutputValueInUI() + " for buff: " + warfareBuffID);
                }

                String name = context.fsdData.localizationStrings.get(warfareBuff.displayNameID());

                warfareBuffTable.content(TR().content(
                    TD().content(SPAN("item_stats_span").title(name).content(
                            DIV("item_stats_icon"),
                            TEXT(name + ":")
                        )
                    ),
                    valueTD
                ));

            }

        }

        String itemKind = context.sde.getCategories().get(context.sde.getGroups().get(type.groupID).categoryID).name;
        return new HTML[]{
            HEADER("font_header").text(itemKind + " stats"),
            table.isEmpty() ? HTML.empty() : table,
            mutationsTable.isEmpty() ? HTML.empty() : mutationsTable,
            warfareBuffTable.isEmpty() ? HTML.empty() : warfareBuffTable
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
            }
            
            .item_stats_table tr:not(:first-child) {
                border-top: var(--border-size) solid var(--colour-theme-minor-border);
            }
            
            .item_stats > header {
                margin-bottom: 0.5rem;
            }
            
            .item_stats_table:not(:last-child) {
                border-bottom: var(--border-size) solid var(--colour-theme-minor-border);
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
