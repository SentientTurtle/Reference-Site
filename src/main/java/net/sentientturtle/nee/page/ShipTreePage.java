package net.sentientturtle.nee.page;

import net.sentientturtle.html.Element;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.components.TabBox;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.sentientturtle.html.HTML.*;

/// Page containing the ship tree
public class ShipTreePage extends Page {
    @Override
    public String name() {
        return "Ships";
    }

    @Override
    public @Nullable String description() {
        return SHIP_COUNT + " ships";
    }

    @Override
    public String filename() {
        return "shiptree";
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.ITEM_TREE;
    }

    @Override
    public @Nullable ResourceLocation getIcon(HtmlContext context) {
        return ResourceLocation.ofIconID(1443, context);
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            .ship_tree {
                width: 100%;
            }
            .ship_tree .tab_box {
                width: 100%;
            }
            .ship_tree .tab_box_button {
                width: 64px;
                height: 64px;
                box-sizing: content-box;
            }
            
            .ship_tree_tree {
                width: 100%;
                display: flex;
                flex-direction: column;
                align-items: flex-start;
                gap: 0.5rem;
            }
            
            .ship_tree_faction {
                align-self: center;
                margin-bottom: 1rem;
            }
            
            .ship_tree_header {
                padding-block: 0.5rem;
                padding-inline: 2rem;
            }
            
            .ship_tree_header_border {
                padding: 1px;
                font-size: 1.5rem;
                margin-top: 1rem;
            }
            
            .ship_tree_row {
                display: flex;
                flex-wrap: wrap;
                align-items: flex-start;
                gap: 0.5rem;
            }
            
            .ship_tree_group {
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
                padding: 0.5rem;
            }
            
            .ship_tree_group_row {
                display: flex;
                flex-direction: row;
                flex-wrap: wrap;
                gap: 0.5rem;
            }
            
            .ship_tree_column {
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }
            
            .ship_tree_entry {
                display: flex;
                flex-direction: row;
                align-items: center;
                gap: 0.5rem;
                padding: 0.5rem;
            }
            
            .ship_tree_icon {
                width: 4rem;
                height: 4rem;
            }
            """;
    }

    // Array with each entry (line) being a list of groupIDs representing a ship class
    private static final int[][] GROUP_ORDER = new int[][]{
        {237, 31, 1022},                              // Corvette, Shuttle, Prototype Exploration Ship
        {25, 831, 324, 830, 834, 893, 1527, 1283},    // Frigates
        {420, 541, 1534, 1305},                       // Destroyers
        {26, 906, 833, 358, 894, 832, 963, 1972},     // Cruisers
        {419, 1201, 540},                             // Battlecruisers
        {27, 898, 900},                               // Battleships
        {485, 4594},                                  // Dreadnought
        {1538, 547, 659},                             // Carriers
        {30},                                         // Titan
        {28, 1202, 380},                              // Industrial
        {463, 543},                                   // Mining Barges
        {941},                                        // Industrial Command Ships
        {513, 902},                                   // Freighter
        {883}                                         // Capital Industrial Ships
    };

    private static final String[] GROUP_NAMES = new String[]{
        "Sub-frigate",
        "Frigate",
        "Destroyer",
        "Cruiser",
        "Battlecruiser",
        "Battleship",
        "Capital Ship",
        "Capital Ship",
        "Capital Ship",
        "Industrial",
        "Mining Barge",
        "Industrial Command Ship",
        "Freighter",
        "Capital Ship"
    };

    static {
        assert GROUP_ORDER.length == GROUP_NAMES.length;
    }

    private Element getTree(HtmlContext context, Set<Integer> shipTypes, String factionName, String themeName, int... ships) {
        // Same structure as GROUP_ORDER, with each group being a list of ships in that group
        ArrayList<ArrayList<ArrayList<Type>>> orderedShips = new ArrayList<>();
        for (int[] groups : GROUP_ORDER) {
            ArrayList<ArrayList<Type>> map = new ArrayList<>();
            for (int i = 0; i < groups.length; i++) {
                map.add(new ArrayList<>());
            }
            orderedShips.add(map);
        }

        shipLoop:
        for (int ship : ships) {
            if (!shipTypes.remove(ship)) throw new RuntimeException("Ship typeID not in data: " + ship);

            Type type = context.sde.getTypes().get(ship);
            for (int i = 0; i < GROUP_ORDER.length; i++) {
                for (int j = 0; j < GROUP_ORDER[i].length; j++) {
                    if (!context.sde.getGroups().containsKey(GROUP_ORDER[i][j])) throw new RuntimeException("Unknown groupID: " + GROUP_ORDER[i][j]);
                    if (type.groupID == GROUP_ORDER[i][j]) {
                        orderedShips.get(i).get(j).add(type);
                        continue shipLoop;
                    }
                }
            }
            throw new RuntimeException("No ship tree location for groupID: " + type.groupID);
        }

        var tree = DIV("ship_tree_tree");


        tree.content(
            DIV("ship_tree_faction ship_tree_header_border eve_clip_bottom colour_" + themeName + "_border_bg")
                .content(DIV("ship_tree_header eve_clip_bottom colour_" + themeName + "_bg").text(factionName))
        );

        String prevTitle = null;
        for (int i = 0; i < orderedShips.size(); i++) {
            var row = DIV("ship_tree_row");

            ArrayList<ArrayList<Type>> shipClass = orderedShips.get(i);
            if (shipClass.size() > 0 && !shipClass.stream().allMatch(ArrayList::isEmpty)) {
                if (!GROUP_NAMES[i].equals(prevTitle)) {
                    tree.content(
                        DIV("ship_tree_header_border eve_clip_top colour_" + themeName + "_border_bg")
                            .content(DIV("ship_tree_header eve_clip_top colour_" + themeName + "_bg").text(GROUP_NAMES[i]))
                    );
                    prevTitle = GROUP_NAMES[i];
                }
                tree.content(row);

                for (int j = 0; j < shipClass.size(); j++) {
                    int groupID = GROUP_ORDER[i][j];
                    var types = shipClass.get(j);

                    if (types.size() > 0) {
                        var groupContainer = DIV("ship_tree_group colour_" + themeName + "_minor_border");
                        groupContainer.content(HEADER().text(context.sde.getGroups().get(groupID).name));
                        var groupRow = DIV("ship_tree_group_row");
                        groupContainer.content(groupRow);

                        Type[] typeArray = types.stream()
                            .sorted(Type.comparator(context.sde))
                            .toArray(Type[]::new);

                        Element shipContainer = null;
                        for (int k = 0; k < typeArray.length; k++) {
                            if (k % 2 == 0) {
                                shipContainer = DIV("ship_tree_column");
                                groupRow.content(shipContainer);
                            }
                            shipContainer.content(DIV("ship_tree_entry colour_" + themeName).content(
                                IMG(ResourceLocation.typeIcon(typeArray[k].typeID, context), null, 64)
                                    .className("ship_tree_icon")
                                    .attribute("loading", "lazy"),
                                new PageLink(new TypePage(typeArray[k]))
                            ));
                        }
                        row.content(groupContainer);
                    }
                }
            }
        }
        return tree;
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        Set<Group> shipGroups = context.sde.getCategoryGroups().get(6);
        Objects.requireNonNull(shipGroups);
        Set<Integer> shipTypeIDs = shipGroups
            .stream()
            .filter(group -> group.published)
            .flatMap(group -> context.sde.getGroupTypes().getOrDefault(group.groupID, Set.of()).stream())
            .filter(type -> type.published)
            .map(type -> type.typeID)
            .collect(Collectors.toCollection(HashSet::new));

        shipTypeIDs.remove(670);    // Capsule not included in ship tree

        var content = new TabBox(List.of(
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/amarr.png", context), "Amarr Empire", 64).title("Amarr Empire").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Amarr Empire", "amarr", AMARR_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/caldari.png", context), "Caldari State", 64).title("Caldari State").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Caldari State", "caldari", CALDARI_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/gallente.png", context), "Gallente Federation", 64).title("Gallente Federation").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Gallente Federatopn", "gallente", GALLENTE_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/minmatar.png", context), "Minmatar Republic", 64).title("Minmatar Republic").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Minmatar Republic", "minmatar", MINMATAR_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/ore.png", context), "Outer Ring Excavations", 64).title("Outer Ring Excavations").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Outer Ring Excavations", "ore", ORE_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/guristas.png", context), "Guristas Pirates", 64).title("Guristas Pirates").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Guristas Pirates", "guristas", GURISTAS_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/sansha.png", context), "Sansha's Nation", 64).title("Sansha's Nation").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Sansha's Nation", "sansha", SANSHA_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/bloodraiders.png", context), "Blood Raider Covenant", 64).title("Blood Raider Covenant").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Blood Raider Covenant", "bloodraiders", BLOODRAIDER_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/angel.png", context), "Angel Cartel", 64).title("Angel Cartel").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Angel Cartel", "angelcartel", ANGELCARTEL_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/serpentis.png", context), "Serpentis", 64).title("Serpentis").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Serpentis", "serpentis", SERPENTIS_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/soe.png", context), "Servant Sisters of EVE", 64).title("Sisters of EVE").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Servant Sisters of EVE", "soe", SOE_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/mordus.png", context), "Mordu's Legion", 64).title("Mordu's Legion").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Mordu's Legion", "mordus", MORDUS_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/triglaviancollective.png", context), "Triglavian Collective", 64).title("Triglavian Collective").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Triglavian Collective", "triglavian", TRIGLAVIAN_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/edencom.png", context), "EDENCOM", 64).title("EDENCOM").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "EDENCOM", "edencom", EDENCOM_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/concord.png", context), "CONCORD Assembly", 64).title("CONCORD Assembly").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "CONCORD Assembly", "concord", CONCORD_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/soct.png", context), "Society of Conscious Thought", 64).title("Society of Conscious Thought").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Society of Conscious Thought", "sotc", SOCT_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.fromSharedCache("res:/ui/texture/classes/shiptree/factions/deathless.png", context), "Deathless Circle", 64).title("Deathless Circle").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Deathless Circle", "deathless", DEATHLESS_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.ofIconID(21065, context), "Alliance Tournament Prizes", 64).title("Alliance Tournament").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Alliance Tournament Prizes", "theme", TOURNAMENT_SHIPS)
            ),
            new TabBox.Tab(
                IMG(ResourceLocation.ofIconID(1443, context), "Special Ships", 64).title("Special Ships").className("ship_tree_icon"),
                getTree(context, shipTypeIDs, "Special Ships", "theme", SPECIAL_SHIPS)
            )
        ));

        if (shipTypeIDs.size() > 0) {
            for (Integer shipTypeID : shipTypeIDs) {
                System.out.println("WARNING: Ship not in ship tree: " + context.sde.getTypes().get(shipTypeID).name + "\t(" + shipTypeID + ")");
            }
        }

        return DIV("ship_tree font_header").content(content);
    }

    private static final int[] CALDARI_SHIPS = new int[]{
        582,
        583,
        584,
        601,
        602,
        603,
        605,
        620,
        621,
        623,
        632,
        638,
        640,
        648,
        649,
        672,
        3764,
        4306,
        11176,
        11178,
        11192,
        11194,
        11379,
        11381,
        11957,
        11959,
        11985,
        11993,
        11995,
        12011,
        12032,
        12729,
        12731,
        16227,
        16238,
        17619,
        17634,
        17636,
        19726,
        20185,
        22436,
        22446,
        22464,
        22470,
        23915,
        23917,
        24688,
        24698,
        28710,
        28844,
        29340,
        29984,
        32309,
        32876,
        33153,
        34828,
        37455,
        37458,
        37482,
        37605,
        72812,
        72904,
        73793,
        73795,
        77284
    };

    private static final int[] MINMATAR_SHIPS = new int[]{
        585,
        586,
        587,
        588,
        598,
        599,
        622,
        629,
        630,
        631,
        639,
        644,
        651,
        652,
        653,
        3766,
        4310,
        11132,
        11182,
        11196,
        11198,
        11371,
        11387,
        11400,
        11961,
        11963,
        11978,
        11999,
        12013,
        12015,
        12034,
        12735,
        12747,
        16231,
        16242,
        17713,
        17732,
        17812,
        19722,
        20189,
        22440,
        22444,
        22456,
        22468,
        22852,
        23773,
        24483,
        24694,
        24702,
        28665,
        28846,
        29336,
        29990,
        32311,
        32878,
        33157,
        34562,
        37454,
        37460,
        37480,
        37606,
        72811,
        72903,
        73787,
        73794,
        77288
    };

    private static final int[] AMARR_SHIPS = new int[]{
        589,
        590,
        591,
        596,
        597,
        624,
        625,
        628,
        642,
        643,
        1944,
        2006,
        2161,
        4302,
        11134,
        11184,
        11186,
        11188,
        11190,
        11365,
        11393,
        11567,
        11965,
        11987,
        12003,
        12017,
        12019,
        12038,
        12733,
        12753,
        16233,
        16236,
        17703,
        17709,
        17726,
        19720,
        19744,
        20125,
        20183,
        22428,
        22448,
        22452,
        22474,
        23757,
        23919,
        24692,
        24696,
        28659,
        28850,
        29248,
        29337,
        29986,
        32305,
        32874,
        33155,
        34317,
        37453,
        37457,
        37481,
        37604,
        72872,
        72907,
        73789,
        73790,
        77283
    };

    private static final int[] GALLENTE_SHIPS = new int[]{
        592,
        593,
        594,
        606,
        607,
        608,
        609,
        626,
        627,
        633,
        634,
        641,
        645,
        650,
        654,
        655,
        656,
        657,
        671,
        4308,
        11129,
        11172,
        11174,
        11200,
        11202,
        11377,
        11969,
        11971,
        11989,
        12005,
        12021,
        12023,
        12042,
        12044,
        12743,
        12745,
        16229,
        16240,
        17728,
        17841,
        17843,
        19724,
        20187,
        22430,
        22442,
        22460,
        22466,
        23911,
        23913,
        24690,
        24700,
        28661,
        28848,
        29344,
        29988,
        32307,
        32872,
        33151,
        35683,
        37456,
        37459,
        37483,
        37607,
        72869,
        72913,
        73792,
        73796,
        77281
    };


    private static final int[] SERPENTIS_SHIPS = new int[]{17722, 17740, 17928, 42124, 42125, 42126};

    private static final int[] MORDUS_SHIPS = new int[]{33816, 33818, 33820};

    private static final int[] GURISTAS_SHIPS = new int[]{
        17715,
        17918,
        17930,
        21628,
        45645,
        45647,
        45649,
        78366,
        78367
    };

    private static final int[] ANGELCARTEL_SHIPS = new int[]{
        17720,
        17738,
        17932,
        78333,
        78369,
        78576
    };

    private static final int[] SANSHA_SHIPS = new int[]{17718, 17736, 17924, 3514};

    private static final int[] BLOODRAIDER_SHIPS = new int[]{
        17920,
        17922,
        17926,
        42241,
        42242,
        42243
    };

    private static final int[] SOE_SHIPS = new int[]{33468, 33470, 33472};

    private static final int[] SOCT_SHIPS = new int[]{
        3756,
        29266,
        42685,
        47466,
        77114
    };

    private static final int[] ORE_SHIPS = new int[]{
        2998,
        17476,
        17478,
        17480,
        22544,
        22546,
        22548,
        28352,
        28606,
        32880,
        33697,
        34328,
        37135,
        42244
    };

    private static final int[] TRIGLAVIAN_SHIPS = new int[]{
        47269,
        47270,
        47271,
        49710,
        49711,
        49712,
        49713,
        52250,
        52252,
        52254,
        52907
    };

    private static final int[] EDENCOM_SHIPS = new int[]{
        54731,
        54732,
        54733,
        81008,
        81040,
        81046,
        81047
    };


    private static final int[] CONCORD_SHIPS = new int[]{34496, 44993, 44995, 44996, 45534};

    private static final int[] DEATHLESS_SHIPS = new int[]{85086, 85087};

    private static final int[] TOURNAMENT_SHIPS = new int[]{
        2834,
        2836,
        3516,
        3518,
        26840,
        26842,
        32207,
        32209,
        32788,
        32790,
        33395,
        33397,
        33673,
        33675,
        35779,
        35781,
        42245,
        42246,
        45530,
        45531,
        48635,
        48636,
        60764,
        60765,
        74141,
        74316,
        77726,
        78414,
        85062,
        85229,
        85236
    };

    private static final int[] SPECIAL_SHIPS = new int[]{
        64034,
        33513,
        3532,
        11940,
        11942,
        635,
        2078,
        4363,
        4388,
        11011,
        30842,
        32811,
        33553,
        34590,
        2863,
        11936,
        11938,
        13202,
        33081,
        615,
        33079,
        617,
        33083,
        21097
    };

    private static int SHIP_COUNT = AMARR_SHIPS.length
                                    + CALDARI_SHIPS.length
                                    + GALLENTE_SHIPS.length
                                    + MINMATAR_SHIPS.length
                                    + ORE_SHIPS.length
                                    + GURISTAS_SHIPS.length
                                    + SANSHA_SHIPS.length
                                    + BLOODRAIDER_SHIPS.length
                                    + ANGELCARTEL_SHIPS.length
                                    + SERPENTIS_SHIPS.length
                                    + SOE_SHIPS.length
                                    + TRIGLAVIAN_SHIPS.length
                                    + EDENCOM_SHIPS.length
                                    + CONCORD_SHIPS.length
                                    + SOCT_SHIPS.length
                                    + DEATHLESS_SHIPS.length
                                    + TOURNAMENT_SHIPS.length
                                    + SPECIAL_SHIPS.length;
}
