package net.sentientturtle.nee.data.sde;

import net.sentientturtle.html.HTML;
import net.sentientturtle.html.PageLink;
import net.sentientturtle.nee.components.TypeSkills;
import net.sentientturtle.nee.data.datatypes.*;
import net.sentientturtle.nee.page.GroupPage;
import net.sentientturtle.nee.page.TypePage;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Datasource for EVE Online Static-Data-Export Data.
 * <br>
 * Implementors must call {@link SDEData#patch()} and then {@link SDEData#loadViews()} after initializing the abstract collections in this type
 *
 * @see SQLiteSDEData
 */
public abstract class SDEData {
    private static final DecimalFormat decimalFormat;
    private static final SimpleDateFormat dateFormat;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');   // English style number formatting to match that of the game
        symbols.setGroupingSeparator(',');
        symbols.setNaN("???");              // NaN treated as "Unknown"
        decimalFormat = new DecimalFormat("###,##0.##", symbols);

        // This should be replaced with more robust date formatting
        dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    // Data

    /// Map<CategoryID, Category>
    public abstract Map<Integer, Category> getCategories();

    /// Map<GroupID, Group>
    public abstract Map<Integer, Group> getGroups();

    /// Map<TypeID, Type>
    public abstract Map<Integer, Type> getTypes();

    /// Map<TypeID, TypeTrait>, with special values for SkillID {-1, -2}
    public abstract Map<Integer, TypeTraits> getTypeTraits();

    /// Map<AttributeID, Attribute>
    public abstract Map<Integer, Attribute> getAttributes();

    /// Map<TypeID, Map<AttributeID, AttributeValue>>
    public abstract Map<Integer, Map<Integer, Double>> getTypeAttributes();

    /// Map<EffectID, Effect>
    public abstract Map<Integer, Effect> getEffects();

    /// Map<TypeID, Set<EffectID>>
    public abstract Map<Integer, Set<Integer>> getTypeEffects();

    /// Map<IconID, Icon resource>
    public abstract Map<Integer, String> getEveIcons();

    /// Map<Blueprint TypeID, Map<IndustryActivityType, IndustryActivity using blueprint>>
    public abstract Map<Integer, EnumMap<IndustryActivityType, IndustryActivity>> getBpActivities();

    /// Map<TypeID, Map<Material TypeID, quantity>>
    public abstract Map<Integer, Map<Integer, Integer>> getReprocessingMaterials();

    /// Map<SchematicID, PlanetSchematic>
    public abstract Map<Integer, PlanetSchematic> getPlanetSchematics();

    /// Map<MetaGroupID, MetaGroup>
    public abstract Map<Integer, MetaGroup> getMetaGroups();

    /// Map<TypeID, Set<TypeID>>
    public abstract Map<Integer, Set<Integer>> getVariants();

    /// Map<TypeID, MetaGroupID>
    public abstract Map<Integer, Integer> getMetaTypes();

    /// Map<SolarSystemID, SolarSystem>
    public abstract Map<Integer, SolarSystem> getSolarSystems();

    /// Map<ConstellationID, Constellation>
    public abstract Map<Integer, Constellation> getConstellations();

    /// Map<RegionID, Region>
    public abstract Map<Integer, Region> getRegions();

    /// Map<SolarSystemID, Set<SolarSystemID>>
    public abstract Map<Integer, Set<Integer>> getOutJumps();

    /// Map<SolarSystemID, Set<SolarSystemID>>
    public abstract Map<Integer, Set<Integer>> getInJumps();

    /// Map<SolarSystemID, Set<Celestial in solarsystem>>
    public abstract Map<Integer, Set<Celestial>> getCelestials();

    /// Map<SolarSystemID, Set<Station in solarsystem>>
    public abstract Map<Integer, Set<Station>> getStations();

    /// Map<FactionID, Faction>
    public abstract Map<Integer, Faction> getFactions();

    /// Map<MarketGroupID, Faction>
    public abstract Map<Integer, MarketGroup> getMarketGroups();

    // Views
    // Map<CategoryID, Set<Group>>
    private Map<Integer, Set<Group>> categoryGroups;
    // Map<GroupID, Set<Type>>
    private Map<Integer, Set<Type>> groupTypes;

    // Map<Material TypeID, Set<IndustryActivity using material>>
    private Map<Integer, Set<IndustryActivity>> materialActivityMap;
    // Map<Product TypeID, Set<IndustryActivity producing product>>
    private Map<Integer, Set<IndustryActivity>> productActivityMap;
    // Map<Skill TypeID, Set<IndustryActivity using skill>>
    private Map<Integer, Set<IndustryActivity>> skillActivityMap;
    // Map<Output TypeID, PlanetSchematic producing output>
    private Map<Integer, PlanetSchematic> outputSchematicMap;
    // Map<Input TypeID, Set<PlanetSchematic using input>>
    private Map<Integer, Set<PlanetSchematic>> inputSchematicMap;
    // Map<Material TypeID, Set<TypeID yielding material>>
    private Map<Integer, Set<Integer>> oreReprocessingMap;
    // Map<MarketGroupID, Set<Type>>
    private Map<Integer, Set<Type>> marketGroupTypeMap;
    // Map<MarketGroupID, Set<child MarketGroup>>
    private Map<Integer, Set<MarketGroup>> marketGroupChildMap;
    // Map<ConstellationID, List<SolarSystem>>
    private Map<Integer, List<SolarSystem>> constellationSolarSystems;
    // Map<RegionID, List<SolarSystem>>
    private Map<Integer, List<SolarSystem>> regionSolarSystems;
    // Map<RegionID, List<Constellation>>
    private Map<Integer, List<Constellation>> regionConstellations;
    // Map<skill TypeID, Map<Level, Set<TypeID requiring skill>>>
    private Map<Integer, Map<Integer, Set<Integer>>> requiresSkillMap;

    // View getters

    /// Map<CategoryID, Set<Group>>
    public Map<Integer, Set<Group>> getCategoryGroups() {
        if (categoryGroups == null) throw new IllegalStateException("View collections not initialized!");
        return categoryGroups;
    }

    /// Map<GroupID, Set<Type>>
    public Map<Integer, Set<Type>> getGroupTypes() {
        if (groupTypes == null) throw new IllegalStateException("View collections not initialized!");
        return groupTypes;
    }

    /// Map<Material TypeID, Set<IndustryActivity using material>>
    public Map<Integer, Set<IndustryActivity>> getMaterialActivityMap() {
        if (materialActivityMap == null) throw new IllegalStateException("View collections not initialized!");
        return materialActivityMap;
    }

    /// Map<Product TypeID, Set<IndustryActivity producing product>>
    public Map<Integer, Set<IndustryActivity>> getProductActivityMap() {
        if (productActivityMap == null) throw new IllegalStateException("View collections not initialized!");
        return productActivityMap;
    }

    /// Map<Skill TypeID, Set<IndustryActivity using skill>>
    public Map<Integer, Set<IndustryActivity>> getSkillActivityMap() {
        if (skillActivityMap == null) throw new IllegalStateException("View collections not initialized!");
        return skillActivityMap;
    }

    /// Map<Output TypeID, PlanetSchematic producing output>
    public Map<Integer, PlanetSchematic> getOutputSchematicMap() {
        if (outputSchematicMap == null) throw new IllegalStateException("View collections not initialized!");
        return outputSchematicMap;
    }

    /// Map<Input TypeID, Set<PlanetSchematic using input>>
    public Map<Integer, Set<PlanetSchematic>> getInputSchematicMap() {
        if (inputSchematicMap == null) throw new IllegalStateException("View collections not initialized!");
        return inputSchematicMap;
    }

    /// Map<Material TypeID, Set<TypeID yielding material>>
    public Map<Integer, Set<Integer>> getOreReprocessingMap() {
        if (oreReprocessingMap == null) throw new IllegalStateException("View collections not initialized!");
        return oreReprocessingMap;
    }

    /// Map<MarketGroupID, Set<Type>>
    public Map<Integer, Set<Type>> getMarketGroupTypeMap() {
        if (marketGroupTypeMap == null) throw new IllegalStateException("View collections not initialized!");
        return marketGroupTypeMap;
    }

    /// Map<MarketGroupID, Set<MarketGroup>>
    public Map<Integer, Set<MarketGroup>> getMarketGroupChildMap() {
        if (marketGroupChildMap == null) throw new IllegalStateException("View collections not initialized!");
        return marketGroupChildMap;
    }

    /// Map<RegionID, List<Constellation>>
    public Map<Integer, List<SolarSystem>> getConstellationSolarSystemMap() {
        if (constellationSolarSystems == null) throw new IllegalStateException("View collections not initialized!");
        return constellationSolarSystems;
    }

    /// Map<RegionID, List<SolarSystem>>
    public Map<Integer, List<SolarSystem>> getRegionSolarSystemMap() {
        if (regionSolarSystems == null) throw new IllegalStateException("View collections not initialized!");
        return regionSolarSystems;
    }

    /// Map<RegionID, List<Constellation>>
    public Map<Integer, List<Constellation>> getRegionConstellationMap() {
        if (regionConstellations == null) throw new IllegalStateException("View collections not initialized!");
        return regionConstellations;
    }

    /// Map<skill TypeID, Map<Level, Set<TypeID requiring skill>>>
    public Map<Integer, Map<Integer, Set<Integer>>> getRequiresSkillMap() {
        if (requiresSkillMap == null) throw new IllegalStateException("View collections not initialized!");
        return requiresSkillMap;
    }

    /*
     * Utility methods to allow usage as method reference, and setting the type of collection used in one place
     */
    public <K, V> Map<K, V> produceMap() {
        return new LinkedHashMap<>();
    }

    public <K, V> Map<K, V> produceMap(Object ignored) {  // For use as a method reference
        return produceMap();
    }

    public <E> Set<E> produceSet() {
        return new LinkedHashSet<>();
    }

    public <E> Set<E> produceSet(Object ignored) {
        return produceSet();
    }

    public <E> List<E> produceList() {
        return new ArrayList<>();
    }

    public <E> List<E> produceList(Object ignored) {
        return produceList();
    }

    /// Initialize data views
    protected void loadViews() {
        categoryGroups = produceMap();
        for (Group group : getGroups().values()) {
            categoryGroups.computeIfAbsent(group.categoryID, this::produceSet).add(group);
        }

        groupTypes = produceMap();
        marketGroupTypeMap = produceMap();
        for (Type type : getTypes().values()) {
            groupTypes.computeIfAbsent(type.groupID, this::produceSet).add(type);
            if (type.marketGroupID != null) {
                marketGroupTypeMap.computeIfAbsent(type.marketGroupID, this::produceSet).add(type);
            }
        }

        materialActivityMap = produceMap();
        productActivityMap = produceMap();
        skillActivityMap = produceMap();
        for (Map<IndustryActivityType, IndustryActivity> activityMap : this.getBpActivities().values()) {
            for (IndustryActivity activity : activityMap.values()) {
                for (Integer materialID : activity.materialMap.keySet()) {
                    materialActivityMap.computeIfAbsent(materialID, this::produceSet).add(activity);
                }
                for (Integer productID : activity.productMap.keySet()) {
                    productActivityMap.computeIfAbsent(productID, this::produceSet).add(activity);
                }
                for (Integer skillID : activity.skillMap.keySet()) {
                    skillActivityMap.computeIfAbsent(skillID, this::produceSet).add(activity);
                }
            }
        }

        outputSchematicMap = produceMap();
        inputSchematicMap = produceMap();
        for (PlanetSchematic schematic : this.getPlanetSchematics().values()) {
            outputSchematicMap.put(schematic.outputTypeID, schematic);
            for (Integer input : schematic.inputs.keySet()) {
                inputSchematicMap.computeIfAbsent(input, this::produceSet).add(schematic);
            }
        }

        oreReprocessingMap = produceMap();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : getReprocessingMaterials().entrySet()) {
            int source = entry.getKey();
            if (getGroups().get(getTypes().get(source).groupID).categoryID == 25) {
                for (Integer materialID : entry.getValue().keySet()) {
                    oreReprocessingMap.computeIfAbsent(materialID, this::produceSet).add(source);
                }
            }
        }


        marketGroupChildMap = produceMap();
        for (MarketGroup marketGroup : this.getMarketGroups().values()) {
            if (marketGroup.parentGroupID != null) {
                marketGroupChildMap.computeIfAbsent(marketGroup.parentGroupID, this::produceSet).add(marketGroup);
            }
        }

        constellationSolarSystems = produceMap();
        regionSolarSystems = produceMap();
        for (SolarSystem solarSystem : getSolarSystems().values()) {
            constellationSolarSystems.computeIfAbsent(solarSystem.constellationID, this::produceList).add(solarSystem);
            regionSolarSystems.computeIfAbsent(solarSystem.regionID, this::produceList).add(solarSystem);
        }

        regionConstellations = produceMap();
        for (Constellation constellation : getConstellations().values()) {
            regionConstellations.computeIfAbsent(constellation.regionID, this::produceList).add(constellation);
        }

        requiresSkillMap = produceMap();
        for (Map.Entry<Integer, Map<Integer, Double>> entry : getTypeAttributes().entrySet()) {
            int typeID = entry.getKey();
            Map<Integer, Double> typeAttributes = entry.getValue();
            int[] skillAttributes = TypeSkills.SKILL_ATTRIBUTES;
            for (int i = 0; i < skillAttributes.length; i++) {
                Double skillTypeID = typeAttributes.get(skillAttributes[i]);
                if (skillTypeID != null) {
                    int level = (int) (double) typeAttributes.getOrDefault(TypeSkills.LEVEL_ATTRIBUTES[i], 1.0);
                    requiresSkillMap.computeIfAbsent((int) (double) skillTypeID, this::produceMap).computeIfAbsent(level, this::produceSet).add(typeID);
                }
            }
        }
    }


    private HTML formatDuration(double duration, TimeUnit timeUnit) {
        if (Double.isNaN(duration)) {
            return HTML.TEXT("???");
        } else {
            // convert to seconds, as Double
            double multiplier = ((double) timeUnit.toNanos(1)) / ((double) TimeUnit.SECONDS.toNanos(1));

            long durationLong = Math.round(duration * multiplier);
            if (durationLong < 60) {
                return HTML.TEXT(String.format("%2ds", durationLong));
            } else if (durationLong < 60 * 60) {
                return HTML.TEXT(String.format("%2dm %2ds", durationLong / 60, durationLong % 60));
            } else if (durationLong < 60 * 60 * 24) {
                return HTML.TEXT(String.format("%2dh %2dm %2ds", durationLong / (60 * 60), durationLong % (60 * 60) / 60, durationLong % 60));
            } else if (durationLong < 60.0 * 60.0 * 24.0 * 30.0) {
                return HTML.TEXT(String.format("%2dd", durationLong / (60 * 60 * 24)) + " " + String.format("%2dh %2dm %2ds", durationLong % (60 * 60 * 24) / (60 * 60), durationLong % (60 * 60) / 60, durationLong % 60));
            } else if (durationLong < 60.0 * 60.0 * 24.0 * 365.0) {
                return HTML.TEXT(String.format("%2dm", durationLong / (60 * 60 * 24 * 30)) + " " + String.format("%2dd", durationLong % (60 * 60 * 24 * 30) / (60 * 60 * 24)) + " " + String.format("%2dh %2dm %2ds", durationLong % (60 * 60 * 24) / (60 * 60), durationLong % (60 * 60) / 60, durationLong % 60));
            } else {
                return HTML.TEXT(String.format("%2dy", durationLong / (60 * 60 * 24 * 365)) + " " + String.format("%2dm", durationLong % (60 * 60 * 24 * 365) / (60 * 60 * 24 * 30)) + " " + String.format("%2dd", durationLong % (60 * 60 * 24 * 30) / (60 * 60 * 24)) + " " + String.format("%2dh %2dm %2ds", durationLong % (60 * 60 * 24) / (60 * 60), durationLong % (60 * 60) / 60, durationLong % 60));
            }
        }
    }

    /**
     * Formats a value into a String with a given unit
     *
     * @param value  Value to format
     * @param unitID UnitID of the unit to format the value with
     * @return Value formatted as a String with the specified unit
     */
    public HTML format_with_unit(double value, @Nullable Integer unitID) {
        if (unitID == null) unitID = -1;

        return HTML.SPAN("no_break")
            .content(switch (unitID) {
                case -5:    // Mining yield (m³/s)
                    yield HTML.TEXT(decimalFormat.format(value) + " m³/s");
                case -4:    // Health sustain (HP/s)
                    yield HTML.TEXT(decimalFormat.format(value) + " HP/s");
                case -3:    // Capacitor sustain (GJ/s)
                    yield HTML.TEXT(decimalFormat.format(value) + " GJ/s");
                case -2:    // Datetime (unix)
                    yield HTML.TEXT(dateFormat.format(new Date(((long) value) * 1000)) + " (EVE)");
                case -1:    // No unit
                    yield HTML.TEXT(decimalFormat.format(value));
                case 1:     // Metre
                    if (value > 1000.0) {
                        yield HTML.TEXT(decimalFormat.format(value / 1000.0) + " km");
                    } else {
                        yield HTML.TEXT(decimalFormat.format(value) + " m");
                    }
                case 2:     // Kilogram
                    yield HTML.TEXT(decimalFormat.format(value) + " kg");
                case 101:   // Milliseconds
                    value = value / 1000.0;
                case 3:     // Seconds
                    yield formatDuration(value, TimeUnit.SECONDS);
                case 4:     // Ampere
                    yield HTML.TEXT(decimalFormat.format(value) + " A");
                case 5:     // Kelvin
                    yield HTML.TEXT(decimalFormat.format(value) + " K");
                case 6:     // Mole
                    yield HTML.TEXT(decimalFormat.format(value) + " mol");
                case 7:     // Candela
                    yield HTML.TEXT(decimalFormat.format(value) + " cd");
                case 8:     // Square metre
                    yield HTML.TEXT(decimalFormat.format(value) + " m²");
                case 9:     // Cubic metre
                    yield HTML.TEXT(decimalFormat.format(value) + " m³");
                case 10:    // Metres per second
                    yield HTML.TEXT(decimalFormat.format(value) + " m/s");
                case 11:    // Metres per second squared; Displayed as metres-per-second for :legacy code: reasons
                    yield HTML.TEXT(decimalFormat.format(value) + " m/s");
                case 12:    // Wave number / Reciprocal metre
                    yield HTML.TEXT(decimalFormat.format(value) + " m⁻¹");
                case 13:    // Kilogram per cubic metre
                    yield HTML.TEXT(decimalFormat.format(value) + " kg/m³");
                case 14:    // Cubic metre per kilogram
                    yield HTML.TEXT(decimalFormat.format(value) + " m³/kg");
                case 15:    // Ampere per square meter
                    yield HTML.TEXT(decimalFormat.format(value) + " A/m²");
                case 16:    // Ampere per meter
                    yield HTML.TEXT(decimalFormat.format(value) + " A/m");
                case 17:    // Mole per cubic meter
                    yield HTML.TEXT(decimalFormat.format(value) + " m/m³");
                case 18:    // Candela per square meter
                    yield HTML.TEXT(decimalFormat.format(value) + " cd/m²");
                case 19:    // Mass fraction / Kilogram per Kilogram
                    yield HTML.TEXT(decimalFormat.format(value));
                    // No units 20 to 100
                    // 101 merged with 3 for durations
                case 102:    // Millimetre
                    yield HTML.TEXT(decimalFormat.format(value) + " mm");
                case 103:   // MegaPascal
                    yield HTML.TEXT(decimalFormat.format(value) + " MPa");
                case 104:   // Multiplier
                    yield HTML.TEXT(decimalFormat.format(value) + "×");
                case 105:   // Percentage
                    yield HTML.TEXT(decimalFormat.format(value) + "%");
                case 106:   // Teraflops
                    yield HTML.TEXT(decimalFormat.format(value) + " tf");  // Game convention breaks with formal unit
                case 107:   // Megawatt
                    yield HTML.TEXT(decimalFormat.format(value) + " MW");
                case 108:   // Inverse Absolute Percent
                    yield HTML.TEXT(Math.round((1 - value) * 100) + "%");
                case 109:   // Modifier Percent
                    yield HTML.TEXT((Math.round((value - 1) * 100) > 0 ? "+" + Math.round((value - 1) * 100) : Math.round((value - 1) * 100)) + "%");
                    // No unit 110
                case 111:   // Inversed Modifier Percent
                    yield HTML.TEXT(Math.round((1 - value) * 100) + "%");
                case 112:   // Radians per second
                    yield HTML.TEXT(decimalFormat.format(value) + " rad/s");
                case 113:   // Hitpoints
                    yield HTML.TEXT(decimalFormat.format(value) + " HP");
                case 114:   // Gigajoule
                    yield HTML.TEXT(decimalFormat.format(value) + " GJ");
                case 115:   // "groupID"
                    Map<Integer, Group> groupMap = getGroups();
                    if (groupMap.containsKey((int) value)) {
                        yield new PageLink(new GroupPage(groupMap.get((int) value)));
                    } else {
                        throw new RuntimeException("Reference to unknown group: " + value);
                    }
                case 116:   // "typeID"
                    Map<Integer, Type> typeMap = getTypes();
                    if (typeMap.containsKey((int) value)) {
                        yield new PageLink(new TypePage(typeMap.get((int) value)));
                    } else {
                        throw new RuntimeException("Reference to unknown item: " + value);
                    }
                case 117:   // Size class
                    if (value == 0.0) {
                        yield HTML.TEXT("X-Small");
                    } else if (value == 1.0) {
                        yield HTML.TEXT("Small");
                    } else if (value == 2.0) {
                        yield HTML.TEXT("Medium");
                    } else if (value == 3.0) {
                        yield HTML.TEXT("Large");
                    } else if (value == 4.0) {
                        yield HTML.TEXT("X-Large");
                    } else {
                        throw new RuntimeException("Invalid size class: " + value);
                    }
                case 118:   // Ore units
                    throw new RuntimeException("Unsupported unit: Ore Units");
                case 119:   // "attributeID"
                    throw new RuntimeException("Unsupported unit: attribute ID");   // Exception to find usage
                case 120:   // "attributePoints"
                    yield HTML.TEXT(decimalFormat.format(value) + (value == 1.0 ? " point" : " points"));
                case 121:   // Real percentage "Used for real percentages, i.e. the number 5 is 5%"
                    yield HTML.TEXT(decimalFormat.format(value) + "%");
                case 122:   // Fitting slots
                    yield HTML.TEXT(decimalFormat.format(value) + (value == 1.0 ? " slot" : " slots"));
                case 123:   // Seconds (No time formatting)
                    yield HTML.TEXT(decimalFormat.format(value) + "s");
                case 124:   // Modifier Relative Percent "Used for relative percentages displayed as %"
                    yield HTML.TEXT(decimalFormat.format(value) + "%");
                case 125:   // Newton
                    yield HTML.TEXT(decimalFormat.format(value) + " N");
                case 126:   // Light-year
                    yield HTML.TEXT(decimalFormat.format(value) + " ly");
                case 127:   // Absolute Percent	"0.0 = 0% 1.0 = 100%"
                    yield HTML.TEXT(decimalFormat.format(value * 100) + "%");
                case 128:   // Drone bandwidth
                    yield HTML.TEXT(decimalFormat.format(value) + " Mbit/s");
                case 129:   // Hours
                    yield formatDuration(value, TimeUnit.HOURS);
                // No units 130-132
                case 133:   // "Money"	"ISK"	"ISK"
                    yield HTML.TEXT(decimalFormat.format(value) + " ISK");
                case 134:   // Logistical capacity
                    yield HTML.TEXT(decimalFormat.format(value) + " m³/hr");
                case 135:   // Astronomical Unit
                    yield HTML.TEXT(decimalFormat.format(value) + " m³/hr");
                case 136:   // Slot    "Slot number prefix for various purposes"
                    yield HTML.TEXT("Slot " + decimalFormat.format(value));
                case 137:   // Boolean	"1=True 0=False"
                    if (value == 1.0) {
                        yield HTML.TEXT("True");
                    } else if (value == 0.0) {
                        yield HTML.TEXT("False");
                    } else {
                        throw new RuntimeException("Invalid boolean: " + value);
                    }
                case 138:   // Units	"Units of something, for example fuel"
                    yield HTML.TEXT(decimalFormat.format(value) + (value == 1.0 ? " unit" : " units"));
                case 139:   // Bonus	"Forces a plus sign for positive values"
                    if (value > 0) {
                        yield HTML.TEXT("+" + decimalFormat.format(value));
                    } else {
                        yield HTML.TEXT(decimalFormat.format(value));
                    }
                case 140:   // Level	"Level"	"For anything which is divided by levels"
                    yield HTML.TEXT("Level " + decimalFormat.format(value));
                case 141:   // Hardpoints	"For various counts to do with turret, launcher and rig hardpoints"
                    yield HTML.TEXT(decimalFormat.format(value) + (value == 1.0 ? " hardpoint" : " hardpoints"));
                case 142:   // Sex	"1=Male 2=Unisex 3=Female"
                    if (value == 1.0) {
                        yield HTML.TEXT("Male");
                    } else if (value == 2.0) {
                        yield HTML.TEXT("Unisex");
                    } else if (value == 3.0) {
                        yield HTML.TEXT("Female");
                    } else {
                        throw new RuntimeException("Invalid sex: " + value);
                    }
                case 143:   // Datetime	"Date and time"
                    long unix_timestamp = (long) (value * 86400.0);
                    yield HTML.TEXT(dateFormat.format(new Date(unix_timestamp * 1000)) + " (EVE)");
                case 144:   // Astronomical Unit per second
                    yield HTML.TEXT(decimalFormat.format(value) + " AU/s");
                    // No unit 145-204
                case 205:   // Modifier real percentage
                    if (value > 0) {
                        yield HTML.TEXT("+" + decimalFormat.format(value) + "%");
                    } else {
                        yield HTML.TEXT(decimalFormat.format(value) + "%");
                    }
                default:
                    throw new IllegalArgumentException("Unknown unit: " + unitID);
            });
    }

    protected void patch() {
        // TODO: Finish comments on these IDs
        Set<Integer> publishCategories = Set.of();
        HashSet<Integer> publishCategoriesAndChildren = new HashSet<>();
        Set<Integer> publishGroups = Set.of();
        HashSet<Integer> publishGroupsAndChildren = new HashSet<>(List.of(6, 15, 1324));
        Set<Integer> publishTypes = Set.of(30574, 30575, 30576, 30577, 30669, 30670);

        Set<Integer> unpublishCategories = Set.of();
        HashSet<Integer> unpublishCategoriesAndChildren = new HashSet<>(List.of(11, 54, 91, 2118));
        Set<Integer> unpublishGroups = Set.of();
        HashSet<Integer> unpublishGroupsAndChildren = new HashSet<>(List.of(
            110, 186, 316, 836, 872, 876, 920, 935, 952, 1016, 1020, 1021, 1048, 1273, 1461, 1717, 1975, 1977, 1984, 2004, 2022, 2026, 4161
        ));
        Set<Integer> unpublishTypes = Set.of(
            3404,   // Legacy item, other items in this group are set unpublished
            27038,  // Mission item
            28320,  // Group is unpublished
            34574,  // Legacy entry
            35915,  // Unused FLEX structure module
            47449,  // Mission item, other items in group set unpublished
            // Defunct starbase Blueprint
            2742, 2743, 2744, 2745, 2746, 2747, 2748, 2786, 2790, 2791, 2793, 2795, 2797, 2820, 2821, 28605, 33515, 33584, 2788, 2789, 2800, 33582,
            // Defunct starbase structure
            12239, 14343, 16221, 16869, 17982, 20175, 22634, 24684, 25270, 25271, 25280, 25821, 30655, 30656, 16216, 24567, 28351, 32245, 33477, 33581, 33583,
            27673, 27674, 27897
        );

        for (Category category : getCategories().values()) {
            if (publishCategories.contains(category.categoryID) || publishCategoriesAndChildren.contains(category.categoryID)) {
                category.published = true;
            }
            if (unpublishCategories.contains(category.categoryID) || unpublishCategoriesAndChildren.contains(category.categoryID)) {
                category.published = false;
            }
        }

        for (Group group : getGroups().values()) {
            if (publishGroups.contains(group.groupID) || publishGroupsAndChildren.contains(group.groupID)) {
                group.published = true;
            }
            if (publishCategoriesAndChildren.contains(group.categoryID)) {
                group.published = true;
                publishGroupsAndChildren.add(group.groupID);
            }

            if (unpublishGroups.contains(group.groupID) || unpublishGroupsAndChildren.contains(group.groupID)) {
                group.published = false;
            }
            if (unpublishCategoriesAndChildren.contains(group.categoryID)) {
                group.published = false;
                unpublishGroupsAndChildren.add(group.groupID);
            }
        }

        HashSet<Integer> validGroups = new HashSet<>();
        for (Iterator<Type> iterator = getTypes().values().iterator(); iterator.hasNext();) {
            Type type = iterator.next();
            if (publishTypes.contains(type.typeID) || publishGroupsAndChildren.contains(type.groupID)) {
                type.published = true;
            }
            if (unpublishTypes.contains(type.typeID) || unpublishGroupsAndChildren.contains(type.groupID)) {
                type.published = false;
            }
            if (type.published) {
                validGroups.add(type.groupID);
            } else {
                iterator.remove();
            }
        }

        // Clean up groups/categories with no items in them
        HashSet<Integer> validCategories = new HashSet<>();
        for (Iterator<Group> iterator = getGroups().values().iterator(); iterator.hasNext();) {
            Group group = iterator.next();
            if (validGroups.contains(group.groupID)) {
                group.published = true; // If the group still has published types, set it to published
                validCategories.add(group.categoryID);
            } else {
                iterator.remove();
            }
        }
        getCategories().keySet().retainAll(validCategories);
        for (Category category : getCategories().values()) {
            category.published = true;  // If the category still has published groups, set it to published
        }

        Map<Integer, Type> types = getTypes();

        // Remove unpublished types
        this.getTypeTraits().keySet().removeIf(typeID -> !types.containsKey(typeID));
        for (TypeTraits trait : this.getTypeTraits().values()) {
            trait.skillBonuses().keySet().removeIf(typeID -> !(typeID < 0 || types.containsKey(typeID)));
        }

        this.getTypeAttributes().keySet().removeIf(typeID -> !types.containsKey(typeID));
        this.getBpActivities().keySet().removeIf(typeID -> !types.containsKey(typeID));
        for (Map<IndustryActivityType, IndustryActivity> map : this.getBpActivities().values()) {
            map.values().removeIf(activity -> activity.productMap.keySet().stream().anyMatch(typeID -> !types.containsKey(typeID)));
        }
        getBpActivities().values().removeIf(Map::isEmpty);
        this.getReprocessingMaterials().keySet().removeIf(typeID -> !types.containsKey(typeID));

        Map<Integer, Set<Integer>> typeVariants = getVariants();

        typeVariants.keySet().removeIf(typeID -> !types.containsKey(typeID));
        for (Set<Integer> variants : typeVariants.values()) {
            variants.removeIf(typeID -> !types.containsKey(typeID));
        }

        this.getMetaTypes().keySet().removeIf(typeID -> !types.containsKey(typeID));

        // Create parent marketGroup for all without one
        Map<Integer, MarketGroup> marketGroups = this.getMarketGroups();
        marketGroups.put(-1, new MarketGroup(-1, null, "Items", null));
        for (MarketGroup marketGroup : marketGroups.values()) {
            if (marketGroup.parentGroupID == null && marketGroup.marketGroupID != -1) {
                marketGroup.parentGroupID = -1;
            }
        }
        marketGroups.get(157).parentGroupID = 9;    // Parent Drones market group to Ship Equipment
        marketGroups.get(955).parentGroupID = 9;    // Parent Ship Rigs market group to Ship Equipment
        marketGroups.get(2203).parentGroupID = 2202;    // Parent Structure Rigs market group to Structure Equipment

        Set<Integer> validMarketGroups = types.values()
            .stream()
            .map(type -> type.marketGroupID)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(this::produceSet));

        // New collection as we modify validMarketGroups in this iteration
        for (Integer marketGroupID : new ArrayList<>(validMarketGroups)) {
            Integer parentGroupID = marketGroups.get(marketGroupID).parentGroupID;
            while (parentGroupID != null) {
                validMarketGroups.add(parentGroupID);
                parentGroupID = marketGroups.get(parentGroupID).parentGroupID;
            }
        }

        marketGroups.keySet().retainAll(validMarketGroups);

        Map<Integer, Attribute> attributes = this.getAttributes();

        // Warp speed has two attributes; "Multiplier" with the actual value, and "speed" for display. We patch the latter's name/unit/icon onto the former
        var warp_speed = attributes.get(1281);
        var warp_multiplier = attributes.get(600);
        warp_multiplier.displayName = warp_speed.displayName;
        warp_multiplier.unitID = warp_speed.unitID;
        warp_multiplier.iconID = Objects.requireNonNull(warp_speed.iconID);
        warp_multiplier.published = warp_speed.published;

        // Set attribute categoryID for cargo hold categories
        attributes.get(38).categoryID = 40;
        attributes.get(1233).categoryID = 40;
        attributes.get(1770).categoryID = 40;

        // Fix units
        attributes.get(2104).unitID = -1;
        attributes.get(90).unitID = 114;

        // Patch meta level
        for (Map.Entry<Integer, Integer> entry : getMetaTypes().entrySet()) {
            int typeID = entry.getKey();
            Map<Integer, Double> typeAttributes = getTypeAttributes().get(typeID);
            if (typeAttributes != null) {
                int metaGroup = entry.getValue();
                int minLevel = getMetaGroups().get(metaGroup).getMetaLevel();

                Double metaLevel = typeAttributes.get(633);
                if (metaLevel != null && metaLevel < minLevel) {
                    typeAttributes.put(633, (double) minLevel);
                }
            }
        }

        // Patch missing variants
        Set<Integer> standupECMVariants = typeVariants.get(35940);
        standupECMVariants.add(46577);
        typeVariants.putIfAbsent(46577, standupECMVariants);

        Set<Integer> researchLabVariants = produceSet();
        researchLabVariants.add(35891);
        researchLabVariants.add(45550);
        typeVariants.put(35891, researchLabVariants);
        typeVariants.put(45550, researchLabVariants);

        // Patch map factions
        Map<Integer, Integer> regionFactions = getRegions()
            .values()
            .stream()
            .filter(region -> region.factionID != null)
            .collect(Collectors.toMap(region -> region.regionID, region -> region.factionID));

        Map<Integer, Integer> constellationFactions = new HashMap<>();
        for (Constellation constellation : getConstellations().values()) {
            if (constellation.factionID == null) {
                constellation.factionID = regionFactions.get(constellation.regionID);
            }
            constellationFactions.put(constellation.constellationID, constellation.factionID);
        }

        for (SolarSystem solarSystem : getSolarSystems().values()) {
            if (solarSystem.factionID == null) {
                solarSystem.factionID = constellationFactions.get(solarSystem.constellationID);
            }
        }

        // Patch iconIDs
        for (Category category : getCategories().values()) {
            if (category.iconID != null && category.iconID == 0) category.iconID = null;
        }
        for (Group group : getGroups().values()) {
            if  (group.iconID != null && group.iconID == 0) group.iconID = null;
        }
        for (Attribute attribute : getAttributes().values()) {
            if (attribute.iconID != null && attribute.iconID == 0) attribute.iconID = null;
        }
    }
}
