package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import net.sentientturtle.nee.util.Position;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SDEReader extends AutoCloseable {
    void readCategories(BiConsumer<Integer, SdeCategory> consumer) throws IOException;
    void readGroups(BiConsumer<Integer, SdeGroup> consumer) throws IOException;
    void readTypeBonuses(BiConsumer<Integer, SdeTypeBonus> consumer) throws IOException;
    void readTypes(BiConsumer<Integer, SdeType> consumer) throws IOException;
    void readAttributes(BiConsumer<Integer, SdeAttribute> consumer) throws IOException;
    void readEffects(BiConsumer<Integer, SdeEffect> consumer) throws IOException;
    void readDogma(Consumer<SdeTypeDogma> consumer) throws IOException;
    void readIcons(BiConsumer<Integer, SdeIcon> consumer) throws IOException;
    void readBlueprints(Consumer<SdeBlueprint> consumer) throws IOException;
    void readMaterials(BiConsumer<Integer, SdeTypeMaterials> consumer) throws IOException;
    void readSchematics(BiConsumer<Integer, SdePlanetSchematic> consumer) throws IOException;
    void readMetaGroups(BiConsumer<Integer, SdeMetaGroup> consumer) throws IOException;
    void readFactions(BiConsumer<Integer, SdeFaction> consumer) throws IOException;
    void readMarketGroups(BiConsumer<Integer, SdeMarketGroup> consumer) throws IOException;
    void readStationOperations(BiConsumer<Integer, SdeStationOperation> consumer) throws IOException;
    void readNpcCorporations(BiConsumer<Integer, SdeNpcCorporation> consumer) throws IOException;
    void readRegions(BiConsumer<Integer, SdeRegion> consumer) throws IOException;
    void readConstellations(BiConsumer<Integer, SdeConstellation> consumer) throws IOException;
    void readSolarSystems(BiConsumer<Integer, SdeSolarSystem> consumer) throws IOException;
    void readAsteroidBelts(BiConsumer<Integer, SdeAsteroidBelt> consumer) throws IOException;
    void readPlanets(BiConsumer<Integer, SdePlanet> consumer) throws IOException;
    void readMoons(BiConsumer<Integer, SdeMoon> consumer) throws IOException;
    void readStars(BiConsumer<Integer, SdeStar> consumer) throws IOException;
    void readStargates(BiConsumer<Integer, SdeStargate> consumer) throws IOException;
    void readStations(BiConsumer<Integer, SdeStation> consumer) throws IOException;
    void readDbuffs(BiConsumer<Integer, SdeDbuff> consumer) throws IOException;
    void readDynamicAttributes(BiConsumer<Integer, SdeDynamicAttributes> consumer) throws IOException;
    void readGraphics(BiConsumer<Integer, SdeGraphic> consumer) throws IOException;

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
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) Boolean published,
        @Nullable Integer iconID
    ) {}

    record SdeGroup(
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
        @Nullable SdeBonus[] miscBonuses,
        @Nullable SdeBonus[] roleBonuses,
        @Nullable LinkedHashMap<Integer, SdeBonus[]> types,
        @Nullable Integer iconID
    ) {}

    record SdeType(
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
        @Nullable Double basePrice,
        @Nullable Integer marketGroupID,
        @Nullable Integer variationParentTypeID,
        @Nullable Integer factionID
    ) {}

    record SdeAttribute(
        @JsonProperty(required = true) int attributeID,
        @Nullable Integer categoryID,
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
        @JsonProperty(required = true) int effectID,
        @JsonProperty(required = true) String effectName
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
        int typeID,
        LinkedHashMap<Integer, Double> attributes,
        LinkedHashMap<Integer, Boolean> effects
    ) {}

    record SdeIcon(
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
        @Nullable SdeBpItem[] materials,
        @Nullable SdeBpItem[] products,
        @Nullable SdeBpSkill[] skills
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

    record SdeTypeMaterials(@JsonProperty(required = true) SdeTypeMaterial[] materials) {}

    record SdePlanetSchematicItem(@JsonProperty(required = true) boolean isInput, @JsonProperty(required = true) int quantity) {}

    record SdePlanetSchematic(
        @JsonProperty(required = true) int cycleTime,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) Integer[] pins,
        @JsonProperty(required = true) LinkedHashMap<Integer, SdePlanetSchematicItem> types
    ) {}

    record MetaGroupColor(@JsonProperty(required = true) double r, @JsonProperty(required = true) double g, @JsonProperty(required = true) double b) {}

    record SdeMetaGroup(
        @Nullable MetaGroupColor color,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable Integer iconID,
        @Nullable String iconSuffix,
        @Nullable LocalizedString description
    ) {}

    record SdeFaction(
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
        @Nullable LocalizedString description,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable Integer iconID,
        @JsonProperty(required = true) boolean hasTypes,
        @Nullable Integer parentGroupID
    ) {}

    record SdeStationOperation(
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
        @Nullable int[] allowedMemberRaces,
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
        @Nullable int[] lpOfferTables,
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
        @JsonProperty(required = true) Position position,
        @Nullable LocalizedString description,
        @Nullable Integer factionID,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) int nebulaID,
        @Nullable Integer wormholeClassID,
        @JsonProperty(required = true) int[] constellationIDs
    ) {}

    record SdeConstellation(
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) int regionID,
        @JsonProperty(required = true) int[] solarSystemIDs,
        @Nullable Integer factionID,
        @Nullable Integer wormholeClassID
    ) {}

    record SdeSolarSystem(
        @JsonProperty(required = false) boolean border,
        @JsonProperty(required = true) int constellationID,
        @JsonProperty(required = false) boolean corridor,
        @Nullable int[] disallowedAnchorCategories,
        @Nullable int[] disallowedAnchorGroups,
        @Nullable Integer factionID,
        @JsonProperty(required = false) boolean fringe,
        @JsonProperty(required = false) boolean hub,
        @JsonProperty(required = false) boolean international,
        @Nullable Double luminosity,
        @JsonProperty(required = true) LocalizedString name,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] planetIDs,
        @JsonProperty(required = true) Position position,
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
        @JsonProperty(required = true) int celestialIndex,
        @Nullable LocalizedString name,
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
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] asteroidBeltIDs,
        @JsonProperty(required = true) SdePlanetAttributes attributes,
        @JsonProperty(required = true) int celestialIndex,
        @JsonSetter(nulls = Nulls.AS_EMPTY) int[] moonIDs,
        @Nullable LocalizedString name,
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
        @Nullable Integer moonNameID,
        @JsonProperty(required = true) SdeMoonAttributes attributes,
        @JsonProperty(required = true) int celestialIndex,
        @Nullable LocalizedString name,
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
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) SdeStarStatistics statistics,
        @JsonProperty(required = true) int typeID
    ) {}

    record SdeStargateDestination(int solarSystemID, int stargateID) {}

    record SdeStargate(
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
        @JsonProperty(required = true) String aggregateMode,
        @JsonProperty(required = true) String developerDescription,
        @Nullable LocalizedString displayName,
        @Nullable SdePlainModifier[] itemModifiers,
        @Nullable SdeLocationGroupModifier[] locationGroupModifiers,
        @Nullable SdePlainModifier[] locationModifiers,
        @Nullable SdeLocationSkillModifier[] locationRequiredSkillModifiers,
        @JsonProperty(required = true) String operationName,
        @JsonProperty(required = true) String showOutputValueInUI
    ) {}

    record SdeDynamicAttribute(
        @Nullable Integer highIsGood,
        @JsonProperty(required = true) double max,
        @JsonProperty(required = true) double min
    ) {}

    record SdeIOMapping(
        @JsonProperty(required = true) int[] applicableTypes,
        @JsonProperty(required = true) int resultingType
    ) {}

    record SdeDynamicAttributes(
        @JsonProperty(required = true) LinkedHashMap<Integer, SdeDynamicAttribute> attributeIDs,
        @JsonProperty(required = true) SdeIOMapping[] inputOutputMapping
    ) {}

    record SdeGraphic(
        @Nullable String graphicFile,
        @Nullable String iconFolder,
        @Nullable String sofFactionName,
        @Nullable String sofHullName,
        @Nullable String[] sofLayout,
        @Nullable Integer sofMaterialSetID,
        @Nullable String sofRaceName
    ) {}
}
