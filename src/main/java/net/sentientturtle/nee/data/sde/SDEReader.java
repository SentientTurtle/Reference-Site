package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import net.sentientturtle.nee.util.Position;
import net.sentientturtle.nee.util.Position2D;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

// There used to be two Reader implementations (YAML & JSONL), but for performance & code robustness reasons the yaml version has been removed
public interface SDEReader extends AutoCloseable {
    enum SdeNpcCorporationExtent {L, G, R, N, C}

    enum SdeNpcCorporationSize {T, H, M, L, S}

    record LocalizedString(
        @Nullable String en,
        @Nullable String de,
        @Nullable String es,
        @Nullable String fr,
        @Nullable String ja,
        @Nullable String ko,
        @Nullable String ru,
        @Nullable String zh,
        @Nullable String it
    ) {}

    record SdeCategory(
        @JsonProperty(value = "_key", required = true) int categoryID,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) Boolean published,
        @Nullable Integer iconID
    ) {}

    record SdeGroup(
        @JsonProperty(value = "_key", required = true) int groupID,
        @JsonProperty(required = true) int categoryID,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) boolean published,
        @Nullable Integer iconID,
        @JsonProperty(required = true) boolean anchorable,
        @JsonProperty(required = true) boolean anchored,
        @JsonProperty(required = true) boolean fittableNonSingleton,
        @JsonProperty(required = true) boolean useBasePrice
    ) {}

    record SdeBonus(
        @Nullable Double bonus,
        @JsonProperty(required = true) LocalizedString bonusText,
        @JsonProperty(required = true) int importance,
        @Nullable Integer unitID,
        @Nullable Boolean isPositive
    ) {}

    record SdeTypeBonus(
        @JsonProperty(value = "_key", required = true) int typeID,
        SdeBonus @Nullable [] miscBonuses,
        SdeBonus @Nullable [] roleBonuses,
        @Nullable LinkedHashMap<Integer, SdeBonus[]> types,
        @Nullable Integer iconID
    ) {}

    record SdeType(
        @JsonProperty(value = "_key", required = true) int typeID,
        @JsonProperty(required = true) int groupID,
        @JsonProperty(required = true) boolean published,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable LocalizedString description,
        @Nullable Double mass,
        @Nullable Double volume,
        @Nullable Double radius,
        @Nullable Double capacity,
        @JsonProperty(required = true) int portionSize,
        @Nullable Integer graphicID,
        @Nullable Integer soundID,
        @Nullable Integer iconID,
        @Nullable Integer raceID,
        @Nullable Integer metaGroupID,
        @Nullable Integer metaLevel,
        @Nullable Double basePrice,
        @Nullable Integer marketGroupID,
        @Nullable Integer variationParentTypeID,
        @Nullable Integer factionID
    ) {}

    record SdeAttribute(
        @JsonProperty(value = "_key", required = true) int attributeID,
        @Nullable Integer attributeCategoryID,
        @JsonProperty(required = true) int dataType,
        @JsonProperty(required = true) double defaultValue,
        @Nullable String description,
        @JsonProperty(required = true) boolean highIsGood,
        @JsonProperty(required = true) String name,
        @JsonProperty(required = true) boolean published,
        @JsonProperty(required = true) boolean stackable,
        @Nullable LocalizedString displayName,
        @Nullable LocalizedString tooltipDescription,
        @Nullable LocalizedString tooltipTitle,
        @Nullable Integer iconID,
        @Nullable Integer unitID,
        @Nullable Integer chargeRechargeTimeID,
        @Nullable Integer maxAttributeID,
        @Nullable Integer minAttributeID,
        @Nullable Boolean displayWhenZero
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SdeEffect(
        @JsonProperty(value = "_key", required = true) int effectID,
        @JsonProperty(required = true) int effectCategoryID,
        @JsonProperty(required = true) String name
    ) {}

    record SdeTypeAttribute(
        @JsonProperty(required = true) int attributeID,
        @JsonProperty(required = true) double value
    ) {}

    record SdeTypeEffect(
        @JsonProperty(required = true) int effectID,
        @JsonProperty(required = true) boolean isDefault
    ) {}

    record SdeTypeDogma(
        @JsonProperty(value = "_key", required = true) int typeID,
        @JsonProperty(required = true) LinkedHashMap<Integer, Double> attributes,
        LinkedHashMap<Integer, Boolean> effects
    ) {}

    record SdeIcon(
        @JsonProperty(value = "_key", required = true) int iconID,
        @Nullable String description,
        @Nullable boolean obsolete,
        @JsonProperty(required = true) String iconFile
    ) {}

    record SdeBpItem(
        @JsonProperty(required = true) int quantity,
        @JsonProperty(required = true) int typeID,
        @Nullable Double probability
    ) {}

    record SdeBpSkill(
        @JsonProperty(required = true) int level,
        @JsonProperty(required = true) int typeID
    ) {}

    record SdeBpActivity(
        @JsonProperty(required = true) int time,
        SdeBpItem @Nullable [] materials,
        SdeBpItem @Nullable [] products,
        SdeBpSkill @Nullable [] skills
    ) {}

    record SdeBpActivities(
        @Nullable SdeBpActivity manufacturing,
        @Nullable SdeBpActivity research_time,
        @Nullable SdeBpActivity research_material,
        @Nullable SdeBpActivity copying,
        @Nullable SdeBpActivity invention,
        @Nullable SdeBpActivity reaction
    ) {}

    record SdeBlueprint(
        @JsonProperty(required = true) SdeBpActivities activities,
        @JsonProperty(required = true) int blueprintTypeID,
        @JsonProperty(required = true) int maxProductionLimit
    ) {}

    record SdeTypeMaterial(
        @JsonProperty(required = true) int materialTypeID,
        @JsonProperty(required = true) int quantity
    ) {}

    record SdeTypeRandomizedMaterial(
        @JsonProperty(required = true) int materialTypeID,
        @JsonProperty(required = true) int quantityMin,
        @JsonProperty(required = true) int quantityMax
    ) {}

    record SdeTypeMaterials(
        @JsonProperty(value = "_key", required = true) int inputTypeID,
        SdeTypeMaterial @Nullable [] materials,
        SdeTypeRandomizedMaterial @Nullable [] randomizedMaterials
    ) {}

    record SdeCompressibleType(
        @JsonProperty(value = "_key", required = true) int inputTypeID,
        @JsonProperty(required = true) int compressedTypeID
    ) {}

    record SdePlanetSchematicItem(@JsonProperty(required = true) boolean isInput, @JsonProperty(required = true) int quantity) {}

    record SdePlanetSchematic(
        @JsonProperty(value = "_key", required = true) int schematicID,
        @JsonProperty(required = true) int cycleTime,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) Integer[] pins,
        @JsonProperty(required = true) LinkedHashMap<Integer, SdePlanetSchematicItem> types
    ) {}

    record MetaGroupColor(@JsonProperty(required = true) double r, @JsonProperty(required = true) double g, @JsonProperty(required = true) double b) {}

    record SdeMetaGroup(
        @JsonProperty(value = "_key", required = true) int metaGroupID,
        @Nullable MetaGroupColor color,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable Integer iconID,
        @Nullable String iconSuffix,
        @Nullable LocalizedString description
    ) {}

    record SdeFaction(
        @JsonProperty(value = "_key", required = true) int factionID,
        @Nullable Integer corporationID,
        @JsonProperty(required = true) LocalizedString description,
        @Nullable String flatLogo,
        @Nullable String flatLogoWithName,
        @JsonProperty(required = true) int iconID,
        @JsonProperty(required = true) int[] memberRaces,
        @Nullable Integer militiaCorporationID,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable LocalizedString shortDescription,
        @JsonProperty(required = true) double sizeFactor,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) boolean uniqueName
    ) {}

    record SdeMarketGroup(
        @JsonProperty(value = "_key", required = true) int marketGroupID,
        @Nullable LocalizedString description,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable Integer iconID,
        @JsonProperty(required = true) boolean hasTypes,
        @Nullable Integer parentGroupID
    ) {}

    record SdeStationOperation(
        @JsonProperty(value = "_key", required = true) int operationID,
        @JsonProperty(required = true) int activityID,
        @JsonProperty(required = true) double border,
        @JsonProperty(required = true) double corridor,
        @JsonProperty(required = true) double fringe,
        @JsonProperty(required = true) double hub,
        @Nullable LocalizedString description,
        @JsonProperty(required = true) double manufacturingFactor,
        @JsonProperty(required = true) LocalizedString operationName,
        @JsonProperty(required = true) double ratio,
        @JsonProperty(required = true) double researchFactor,
        @JsonProperty(required = true) int[] services,
        @Nullable LinkedHashMap<Integer, Integer> stationTypes
    ) {}

    record SdeNpcCorporationDivision(int divisionNumber, int leaderID, int size) {}

    record SdeNpcCorporation(
        @JsonProperty(value = "_key", required = true) int corporationID,
        int @Nullable [] allowedMemberRaces,
        @Nullable Integer ceoID,
        @Nullable LinkedHashMap<Integer, Double> corporationTrades,
        @JsonProperty(required = true) boolean deleted,
        @Nullable LocalizedString description,
        @Nullable LinkedHashMap<Integer, SdeNpcCorporationDivision> divisions,
        @Nullable Integer enemyID,
        @Nullable LinkedHashMap<Integer, Double> exchangeRates,
        @JsonProperty(required = true) SdeNpcCorporationExtent extent,
        @Nullable Integer factionID,
        @Nullable Integer friendID,
        @JsonProperty(required = true) boolean hasPlayerPersonnelManager,
        @Nullable Integer iconID,
        @JsonProperty(required = true) int initialPrice,
        @Nullable LinkedHashMap<Integer, Integer> investors,
        int @Nullable [] lpOfferTables,
        @Nullable Integer mainActivityID,
        @JsonProperty(required = true) int memberLimit,
        @JsonProperty(required = true) double minSecurity,
        @JsonProperty(required = true) double minimumJoinStanding,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable Integer raceID,
        @Nullable Integer secondaryActivityID,
        @JsonProperty(required = true) boolean sendCharTerminationMessage,
        @JsonProperty(required = true) long shares,
        @JsonProperty(required = true) SdeNpcCorporationSize size,
        @Nullable Double sizeFactor,
        @Nullable Integer solarSystemID,
        @Nullable Integer stationID,
        @JsonProperty(required = true) double taxRate,
        @JsonProperty(required = true) String tickerName,
        @JsonProperty(required = true) String uniqueName
    ) {}

    record SdeRegion(
        @JsonProperty(value = "_key", required = true) int regionID,
        @JsonProperty(required = true) Position position,
        @Nullable LocalizedString description,
        @Nullable Integer factionID,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) int nebulaID,
        @Nullable Integer wormholeClassID,
        @JsonProperty(required = true) int[] constellationIDs
    ) {}

    record SdeConstellation(
        @JsonProperty(value = "_key", required = true) int constellationID,
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) int regionID,
        @JsonProperty(required = true) int[] solarSystemIDs,
        @Nullable Integer factionID,
        @Nullable Integer wormholeClassID
    ) {}

    record SdeSolarSystem(
        @JsonProperty(value = "_key", required = true) int solarSystemID,
        @JsonProperty(required = false) boolean border,
        @JsonProperty(required = true) int constellationID,
        @JsonProperty(required = false) boolean corridor,
        int @Nullable [] disallowedAnchorCategories,
        int @Nullable [] disallowedAnchorGroups,
        @Nullable Integer factionID,
        @JsonProperty(required = false) boolean fringe,
        @JsonProperty(required = false) boolean hub,
        @JsonProperty(required = false) boolean international,
        @Nullable Double luminosity,
        @JsonProperty(required = true) LocalizedString name,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] planetIDs,
        @JsonProperty(required = true) Position position,
        @Nullable Position2D position2D,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int regionID,
        @JsonProperty(required = false) boolean regional,
        @Nullable String securityClass,
        @JsonProperty(required = true) double securityStatus,
        @Nullable Integer starID,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] stargateIDs,
        @Nullable String visualEffect,
        @Nullable Integer wormholeClassID
    ) {}

    record SdeCelestialStatistics(
        @JsonProperty(required = true) double density,
        @JsonProperty(required = true) double eccentricity,
        @JsonProperty(required = true) double escapeVelocity,
        @JsonProperty(required = false) boolean fragmented,
        @Nullable Double life,
        @JsonProperty(required = true) boolean locked,
        @JsonProperty(required = true) double massDust,
        @Nullable Double massGas,
        @Nullable Double orbitPeriod,
        @Nullable Double orbitRadius,
        @Nullable Double pressure,
        @Nullable Double radius,
        @JsonProperty(required = true) double rotationRate,
        @JsonProperty(required = true) String spectralClass,
        @Nullable Double surfaceGravity,
        @JsonProperty(required = true) double temperature
    ) {}

    record SdeAsteroidBelt(
        @JsonProperty(value = "_key", required = true) int asteroidBeltID,
        @JsonProperty(required = true) int celestialIndex,
        @Nullable LocalizedString uniqueName,
        @JsonProperty(required = true) int orbitID,
        @JsonProperty(required = true) int orbitIndex,
        @JsonProperty(required = true) Position position,
        @Nullable Double radius,
        @JsonProperty(required = true) int solarSystemID,
        @Nullable SdeCelestialStatistics statistics,
        @JsonProperty(required = true) int typeID
    ) {}

    record SdePlanetAttributes(
        @JsonProperty(required = true) int heightMap1,
        @JsonProperty(required = true) int heightMap2,
        @JsonProperty(required = true) boolean population,
        @JsonProperty(required = true) int shaderPreset
    ) {}

    record SdePlanet(
        @JsonProperty(value = "_key", required = true) int planetID,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] asteroidBeltIDs,
        @JsonProperty(required = true) SdePlanetAttributes attributes,
        @JsonProperty(required = true) int celestialIndex,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] moonIDs,
        @Nullable LocalizedString uniqueName,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] npcStationIDs,
        @JsonProperty(required = true) int orbitID,
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) SdeCelestialStatistics statistics,
        @JsonProperty(required = true) int typeID
    ) {}

    record SdeMoonAttributes(
        @JsonProperty(required = true) int heightMap1,
        @JsonProperty(required = true) int heightMap2,
        @JsonProperty(required = true) int shaderPreset
    ) {}

    record SdeMoon(
        @JsonProperty(value = "_key", required = true) int moonID,
        @Nullable Integer moonNameID,
        @JsonProperty(required = true) SdeMoonAttributes attributes,
        @JsonProperty(required = true) int celestialIndex,
        @Nullable LocalizedString uniqueName,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] npcStationIDs,
        @JsonProperty(required = true) int orbitID,
        @JsonProperty(required = true) int orbitIndex,
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int solarSystemID,
        @Nullable SdeCelestialStatistics statistics,
        @JsonProperty(required = true) int typeID
    ) {}

    record SdeStarStatistics(
        @JsonProperty(required = true) double age,
        @JsonProperty(required = true) double life,
        @JsonProperty(required = true) double luminosity,
        @JsonProperty(required = true) String spectralClass,
        @JsonProperty(required = true) double temperature
    ) {}

    record SdeStar(
        @JsonProperty(value = "_key", required = true) int starID,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) SdeStarStatistics statistics,
        @JsonProperty(required = true) int typeID
    ) {}

    record SdeStargateDestination(int solarSystemID, int stargateID) {}

    record SdeStargate(
        @JsonProperty(value = "_key", required = true) int stargateID,
        @JsonProperty(required = true) SdeStargateDestination destination,
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) int typeID
    ) {}

    /* TODO: Replace */
    record SdeSecondarySun(
        @JsonProperty(required = true) int effectBeaconTypeID,
        @JsonProperty(required = true) int itemID,
        @JsonProperty(required = true) double[] position,
        @JsonProperty(required = true) int typeID
    ) {}

    record SdeStation(
        @JsonProperty(value = "_key", required = true) int stationID,
        @Nullable Integer celestialIndex,
        @JsonProperty(required = true) int operationID,
        @JsonProperty(required = true) int orbitID,
        @Nullable Integer orbitIndex,
        @JsonProperty(required = true) int ownerID,
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) double reprocessingEfficiency,
        @JsonProperty(required = true) int reprocessingHangarFlag,
        @JsonProperty(required = true) double reprocessingStationsTake,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) int typeID,
        @JsonProperty(required = false) boolean useOperationName
    ) {}

    record SdePlainModifier(@JsonProperty(required = true) int dogmaAttributeID) {}

    record SdeLocationGroupModifier(@JsonProperty(required = true) int dogmaAttributeID, @JsonProperty(required = true) int groupID) {}

    record SdeLocationSkillModifier(@JsonProperty(required = true) int dogmaAttributeID, @JsonProperty(required = true) int skillID) {}

    record SdeDbuff(
        @JsonProperty(value = "_key", required = true) int buffID,
        @JsonProperty(required = true) String aggregateMode,
        @JsonProperty(required = true) String developerDescription,
        @Nullable LocalizedString displayName,
        SdePlainModifier @Nullable [] itemModifiers,
        SdeLocationGroupModifier @Nullable [] locationGroupModifiers,
        SdePlainModifier @Nullable [] locationModifiers,
        SdeLocationSkillModifier @Nullable [] locationRequiredSkillModifiers,
        @JsonProperty(required = true) String operationName,
        @JsonProperty(required = true) String showOutputValueInUI
    ) {}

    record SdeDynamicAttribute(
        @Nullable Boolean highIsGood,
        @JsonProperty(required = true) double max,
        @JsonProperty(required = true) double min
    ) {}

    record SdeIOMapping(
        @JsonProperty(required = true) int[] applicableTypes,
        @JsonProperty(required = true) int resultingType
    ) {}

    record SdeDynamicAttributes(
        @JsonProperty(value = "_key", required = true) int typeID,
        @JsonProperty(required = true) LinkedHashMap<Integer, SdeDynamicAttribute> attributeIDs,
        @JsonProperty(required = true) SdeIOMapping[] inputOutputMapping
    ) {}

    record SdeGraphic(
        @JsonProperty(value = "_key", required = true) int graphicID,
        @Nullable String graphicFile,
        @Nullable String iconFolder,
        @Nullable String sofFactionName,
        @Nullable String sofHullName,
        String @Nullable [] sofLayout,
        @Nullable Integer sofMaterialSetID,
        @Nullable String sofRaceName
    ) {}

    void readCategories(Consumer<SdeCategory> consumer) throws IOException;
    void readGroups(Consumer<SdeGroup> consumer) throws IOException;
    void readTypeBonuses(Consumer<SdeTypeBonus> consumer) throws IOException;
    void readTypes(Consumer<SdeType> consumer) throws IOException;
    void readAttributes(Consumer<SdeAttribute> consumer) throws IOException;
    void readEffects(Consumer<SdeEffect> consumer) throws IOException;
    void readDogma(Consumer<SdeTypeDogma> consumer) throws IOException;
    void readIcons(Consumer<SdeIcon> consumer) throws IOException;
    void readBlueprints(Consumer<SdeBlueprint> consumer) throws IOException;
    void readMaterials(Consumer<SdeTypeMaterials> consumer) throws IOException;
    void readCompressibleTypes(Consumer<SdeCompressibleType> consumer) throws IOException;
    void readSchematics(Consumer<SdePlanetSchematic> consumer) throws IOException;
    void readMetaGroups(Consumer<SdeMetaGroup> consumer) throws IOException;
    void readFactions(Consumer<SdeFaction> consumer) throws IOException;
    void readMarketGroups(Consumer<SdeMarketGroup> consumer) throws IOException;
    void readStationOperations(Consumer<SdeStationOperation> consumer) throws IOException;
    void readNpcCorporations(Consumer<SdeNpcCorporation> consumer) throws IOException;
    void readRegions(Consumer<SdeRegion> consumer) throws IOException;
    void readConstellations(Consumer<SdeConstellation> consumer) throws IOException;
    void readSolarSystems(Consumer<SdeSolarSystem> consumer) throws IOException;
    void readAsteroidBelts(Consumer<SdeAsteroidBelt> consumer) throws IOException;
    void readPlanets(Consumer<SdePlanet> consumer) throws IOException;
    void readMoons(Consumer<SdeMoon> consumer) throws IOException;
    void readStars(Consumer<SdeStar> consumer) throws IOException;
    void readStargates(Consumer<SdeStargate> consumer) throws IOException;
    void readStations(Consumer<SdeStation> consumer) throws IOException;
    void readDbuffs(Consumer<SdeDbuff> consumer) throws IOException;
    void readDynamicAttributes(Consumer<SdeDynamicAttributes> consumer) throws IOException;
    void readGraphics(Consumer<SdeGraphic> consumer) throws IOException;
}
