package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.sentientturtle.nee.util.Position;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/// Work-in-progress data source using the original YAML Static Data Export
// The YAML data is much more annoying to work with and not always conforms to YAML specification.
@SuppressWarnings("Convert2Diamond") // Jackson TypeReference must contain explicit generics to work
public class YAMLDataExportReader implements AutoCloseable {
    private final ZipFile zipFile;
    private final ObjectMapper yamlMapper;

    public YAMLDataExportReader(Path sdePath) throws IOException {
        zipFile = new ZipFile(sdePath.toFile());
        yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

    public record LocalizedString(
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

    public record SdeCategory(
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) Boolean published,
        @Nullable Integer iconID
    ) {}
    public void readCategories(BiConsumer<Integer, SdeCategory> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("categories.yaml");

        yamlMapper.readValue(
            new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
            new TypeReference<LinkedHashMap<Integer, SdeCategory>>() {}
        )
            .forEach(consumer);
    }

    public record SdeGroup(
        @JsonProperty(required = true) int categoryID,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) boolean published,
        @Nullable Integer iconID,
        @JsonProperty(required = true) boolean anchorable,
        @JsonProperty(required = true) boolean anchored,
        @JsonProperty(required = true) boolean fittableNonSingleton,
        @JsonProperty(required = true) boolean useBasePrice
    ) {}
    public void readGroups(BiConsumer<Integer, SdeGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("groups.yaml");

        yamlMapper.readValue(
            new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
            new TypeReference<LinkedHashMap<Integer, SdeGroup>>() {}
        )
            .forEach(consumer);
    }

    public record SdeBonus(
        @Nullable Double bonus,
        @JsonProperty(required = true) LocalizedString bonusText,
        @JsonProperty(required = true) int importance,
        @Nullable Integer unitID,
        @Nullable Boolean isPositive
    ) {}
    public record SdeTypeBonus(
        @Nullable ArrayList<SdeBonus> miscBonuses,
        @Nullable ArrayList<SdeBonus> roleBonuses,
        @Nullable HashMap<Integer, ArrayList<SdeBonus>> types,
        @Nullable Integer iconID
    ) {}
    public void readTypeBonuses(BiConsumer<Integer, SdeTypeBonus> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeBonus.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder typeBuffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !typeBuffer.isEmpty()) {
                    Map.Entry<Integer, SdeTypeBonus> mapEntry = yamlMapper.readValue(
                        typeBuffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeTypeBonus>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    typeBuffer.setLength(0);
                }
            }
            if (!typeBuffer.isEmpty()) typeBuffer.append('\n');
            typeBuffer.append(line);
        }

        if (!typeBuffer.isEmpty()) {
            Map.Entry<Integer, SdeTypeBonus> mapEntry = yamlMapper.readValue(
                typeBuffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeTypeBonus>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdeType(
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
    public void readTypes(BiConsumer<Integer, SdeType> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("types.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder typeBuffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !typeBuffer.isEmpty()) {
                    Map.Entry<Integer, SdeType> mapEntry = yamlMapper.readValue(
                        typeBuffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeType>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    typeBuffer.setLength(0);
                }
            }
            if (!typeBuffer.isEmpty()) typeBuffer.append('\n');
            typeBuffer.append(line);
        }

        if (!typeBuffer.isEmpty()) {
            Map.Entry<Integer, SdeType> mapEntry = yamlMapper.readValue(
                typeBuffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeType>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdeAttribute(
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
    public void readAttributes(BiConsumer<Integer, SdeAttribute> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dogmaAttributes.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeAttribute> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeAttribute>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeAttribute> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeAttribute>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public record SdeEffect(
        @JsonProperty(required = true) int effectID,
        @JsonProperty(required = true) String effectName
    ) {}
    public void readEffects(BiConsumer<Integer, SdeEffect> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dogmaEffects.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeEffect> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeEffect>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeEffect> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeEffect>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    private record SdeTypeAttribute(
        @JsonProperty(required = true) int attributeID,
        @JsonProperty(required = true) double value
    ) {}
    private record SdeTypeEffect(
        @JsonProperty(required = true) int effectID,
        @JsonProperty(required = true) boolean isDefault
    ) {}
    public record SdeTypeDogma(
        int typeID,
        HashMap<Integer, Double> attributes,
        HashMap<Integer, Boolean> effects
    ) {}
    public void readDogma(Consumer<SdeTypeDogma> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeDogma.yaml");

        record DogmaEntry(
            @JsonSetter(nulls = Nulls.AS_EMPTY) ArrayList<SdeTypeAttribute> dogmaAttributes,
            @JsonSetter(nulls = Nulls.AS_EMPTY) ArrayList<SdeTypeEffect> dogmaEffects
        ) {}

        // Split yaml document into individual entries to improve performance
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, DogmaEntry> dogmaEntry = yamlMapper.readValue(buffer.toString(), new TypeReference<Map.Entry<Integer, DogmaEntry>>() {});
                    HashMap<Integer, Double> attributeMap = new HashMap<>();
                    for (SdeTypeAttribute attribute : dogmaEntry.getValue().dogmaAttributes) {
                        attributeMap.put(attribute.attributeID, attribute.value);
                    }
                    HashMap<Integer, Boolean> effectMap = new HashMap<>();
                    for (SdeTypeEffect effect : dogmaEntry.getValue().dogmaEffects) {
                        effectMap.put(effect.effectID, effect.isDefault);
                    }

                    consumer.accept(new SdeTypeDogma(dogmaEntry.getKey(), attributeMap, effectMap));

                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, DogmaEntry> dogmaEntry = yamlMapper.readValue(buffer.toString(), new TypeReference<Map.Entry<Integer, DogmaEntry>>() {});
            HashMap<Integer, Double> attributeMap = new HashMap<>();
            for (SdeTypeAttribute attribute : dogmaEntry.getValue().dogmaAttributes) {
                attributeMap.put(attribute.attributeID, attribute.value);
            }
            HashMap<Integer, Boolean> effectMap = new HashMap<>();
            for (SdeTypeEffect effect : dogmaEntry.getValue().dogmaEffects) {
                effectMap.put(effect.effectID, effect.isDefault);
            }

            consumer.accept(new SdeTypeDogma(dogmaEntry.getKey(), attributeMap, effectMap));
        }
    }

    public record SdeIcon(
        @Nullable String description,
        @Nullable boolean obsolete,
        @JsonProperty(required = true) String iconFile
    ) {}
    public void readIcons(BiConsumer<Integer, SdeIcon> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("icons.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeIcon>>() {}
            )
            .forEach(consumer);
    }

    public record SdeBpItem(
        @JsonProperty(required = true) int quantity,
        @JsonProperty(required = true) int typeID,
        @Nullable Double probability
    ) {}
    public record SdeBpSkill(
        @JsonProperty(required = true) int level,
        @JsonProperty(required = true) int typeID
    ) {}
    public record SdeBpActivity(
        @JsonProperty(required = true) int time,
        @Nullable ArrayList<SdeBpItem> materials,
        @Nullable ArrayList<SdeBpItem> products,
        @Nullable ArrayList<SdeBpSkill> skills
    ) {}
    public record SdeBpActivities(
        @Nullable SdeBpActivity manufacturing,
        @Nullable SdeBpActivity research_time,
        @Nullable SdeBpActivity research_material,
        @Nullable SdeBpActivity copying,
        @Nullable SdeBpActivity invention,
        @Nullable SdeBpActivity reaction
    ) {}
    public record SdeBlueprint(
        @JsonProperty(required = true) SdeBpActivities activities,
        @JsonProperty(required = true) int blueprintTypeID,
        @JsonProperty(required = true) int maxProductionLimit
    ){}
    public void readBlueprints(Consumer<SdeBlueprint> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("blueprints.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    consumer.accept(
                        yamlMapper.readValue(
                            buffer.toString(),
                            new TypeReference<Map.Entry<Integer, SdeBlueprint>>() {}
                        ).getValue()
                    );
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            consumer.accept(
                yamlMapper.readValue(
                    buffer.toString(),
                    new TypeReference<Map.Entry<Integer, SdeBlueprint>>() {}
                ).getValue()
            );
        }
    }

    public record SdeTypeMaterial(
        @JsonProperty(required = true) int materialTypeID,
        @JsonProperty(required = true) int quantity
    ) {}
    public record SdeTypeMaterials(@JsonProperty(required = true) ArrayList<SdeTypeMaterial> materials) {}
    public void readMaterials(BiConsumer<Integer, SdeTypeMaterials> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeMaterials.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeTypeMaterials> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeTypeMaterials>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeTypeMaterials> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeTypeMaterials>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdePlanetSchematicItem(@JsonProperty(required = true) boolean isInput, @JsonProperty(required = true) int quantity){}
    public record SdePlanetSchematic(
        @JsonProperty(required = true) int cycleTime,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) ArrayList<Integer> pins,
        @JsonProperty(required = true) LinkedHashMap<Integer, SdePlanetSchematicItem> types
    ) {}
    public void readSchematics(BiConsumer<Integer, SdePlanetSchematic> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("planetSchematics.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdePlanetSchematic> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdePlanetSchematic>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdePlanetSchematic> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdePlanetSchematic>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record MetaGroupColor(@JsonProperty(required = true) double r, @JsonProperty(required = true) double g, @JsonProperty(required = true) double b) {};
    public record SdeMetaGroup(
        @Nullable MetaGroupColor color,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable Integer iconID,
        @Nullable String iconSuffix,
        @Nullable LocalizedString description
    ) {}
    public void readMetaGroups(BiConsumer<Integer, SdeMetaGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("metaGroups.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeMetaGroup>>() {}
            )
            .forEach(consumer);
    }

    public record SdeFaction(
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
    public void readFactions(BiConsumer<Integer, SdeFaction> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("factions.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeFaction>>() {}
            )
            .forEach(consumer);
    }

    public record SdeMarketGroup(
        @Nullable LocalizedString description,
        @JsonProperty(required = true) LocalizedString name,
        @Nullable Integer iconID,
        @JsonProperty(required = true) boolean hasTypes,
        @Nullable Integer parentGroupID
    ) {}
    public void readMarketGroups(BiConsumer<Integer, SdeMarketGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("marketGroups.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeMarketGroup> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeMarketGroup>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeMarketGroup> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeMarketGroup>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }
    public record SdeStationOperation(
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
    public void readStationOperations(BiConsumer<Integer, SdeStationOperation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("stationOperations.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeStationOperation>>() {}
            )
            .forEach(consumer);
    }

    public record SdeNpcCorporationDivision(int divisionNumber, int leaderID, int size) {}
    public enum SdeNpcCorporationExtent { L, G, R, N, C }
    public enum SdeNpcCorporationSize { T, H, M, L, S }
    public record SdeNpcCorporation(
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
    ){}
    public void readNpcCorporations(BiConsumer<Integer, SdeNpcCorporation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("npcCorporations.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeNpcCorporation> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeNpcCorporation>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeNpcCorporation> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeNpcCorporation>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdeRegion(
        @JsonProperty(required = true) Position position,
        @Nullable LocalizedString description,
        @Nullable Integer factionID,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) int nebulaID,
        @Nullable Integer wormholeClassID,
        @JsonProperty(required = true) int[] constellationIDs
    ) {}
    public void readRegions(BiConsumer<Integer, SdeRegion> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapRegions.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeRegion>>() {}
            )
            .forEach(consumer);
    }

    public record SdeConstellation(
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) LocalizedString name,
        @JsonProperty(required = true) int regionID,
        @JsonProperty(required = true) int[] solarSystemIDs,
        @Nullable Integer factionID,
        @Nullable Integer wormholeClassID
    ) {}
    public void readConstellations(BiConsumer<Integer, SdeConstellation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapConstellations.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeConstellation>>() {}
            )
            .forEach(consumer);
    }

    public record SdeSolarSystem(
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
    public void readSolarSystems(BiConsumer<Integer, SdeSolarSystem> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapSolarSystems.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeSolarSystem> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeSolarSystem>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeSolarSystem> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeSolarSystem>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdeCelestialStatistics(
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

    public record SdeAsteroidBelt(
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
    public void readAsteroidBelts(BiConsumer<Integer, SdeAsteroidBelt> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapAsteroidBelts.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeAsteroidBelt> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeAsteroidBelt>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeAsteroidBelt> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeAsteroidBelt>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdePlanetAttributes(
        @JsonProperty(required = true) int heightMap1,
        @JsonProperty(required = true) int heightMap2,
        @JsonProperty(required = true) boolean population,
        @JsonProperty(required = true) int shaderPreset
    ) {}
    public record SdePlanet(
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
    public void readPlanets(BiConsumer<Integer, SdePlanet> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapPlanets.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdePlanet> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdePlanet>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdePlanet> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdePlanet>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }


    public record SdeMoonAttributes(
        @JsonProperty(required = true) int heightMap1,
        @JsonProperty(required = true) int heightMap2,
        @JsonProperty(required = true) int shaderPreset
    ) {}
    public record SdeMoon(
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
    public void readMoons(BiConsumer<Integer, SdeMoon> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapMoons.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeMoon> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeMoon>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeMoon> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeMoon>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdeStarStatistics(
        @JsonProperty(required = true) double age,
        @JsonProperty(required = true) double life,
        @JsonProperty(required = true) double luminosity,
        @JsonProperty(required = true) String spectralClass,
        @JsonProperty(required = true) double temperature
    ) {}
    public record SdeStar(
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) SdeStarStatistics statistics,
        @JsonProperty(required = true) int typeID
    ) {}
    public void readStars(BiConsumer<Integer, SdeStar> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapStars.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeStar> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeStar>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeStar> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeStar>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public record SdeStargateDestination(int solarSystemID, int stargateID) {}
    public record SdeStargate(
        @JsonProperty(required = true) SdeStargateDestination destination,
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) int typeID
    ) {}
    public void readStargates(BiConsumer<Integer, SdeStargate> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapStargates.yaml");

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                    Map.Entry<Integer, SdeStargate> mapEntry = yamlMapper.readValue(
                        buffer.toString(),
                        new TypeReference<Map.Entry<Integer, SdeStargate>>() {}
                    );
                    consumer.accept(mapEntry.getKey(), mapEntry.getValue());
                    buffer.setLength(0);
                }
            }
            if (!buffer.isEmpty()) buffer.append('\n');
            buffer.append(line);
        }

        if (!buffer.isEmpty()) {
            Map.Entry<Integer, SdeStargate> mapEntry = yamlMapper.readValue(
                buffer.toString(),
                new TypeReference<Map.Entry<Integer, SdeStargate>>() {}
            );
            consumer.accept(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    /* TODO: Replace */
    public record SdeSecondarySun(
        @JsonProperty(required = true) int effectBeaconTypeID,
        @JsonProperty(required = true) int itemID,
        @JsonProperty(required = true) double[] position,
        @JsonProperty(required = true) int typeID
    ) {}

    public record SdeStation(
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
    public void readStations(BiConsumer<Integer, SdeStation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("npcStations.yaml");
        HashMap<Integer, SdeStation> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeStation>>() {});
        stationMap.forEach(consumer);
    }

    public record SdePlainModifier(@JsonProperty(required = true) int dogmaAttributeID) {}
    public record SdeLocationGroupModifier(@JsonProperty(required = true) int dogmaAttributeID, @JsonProperty(required = true) int groupID) {}
    public record SdeLocationSkillModifier(@JsonProperty(required = true) int dogmaAttributeID, @JsonProperty(required = true) int skillID) {}
    public record SdeDbuff(
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
    public void readDbuffs(BiConsumer<Integer, SdeDbuff> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dbuffCollections.yaml");
        HashMap<Integer, SdeDbuff> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeDbuff>>() {});
        stationMap.forEach(consumer);
    }

    public record SdeDynamicAttribute(
        @Nullable Integer highIsGood,
        @JsonProperty(required = true) double max,
        @JsonProperty(required = true) double min
    ) {}
    public record SdeIOMapping(
        @JsonProperty(required = true) int[] applicableTypes,
        @JsonProperty(required = true) int resultingType
    ) {}
    public record SdeDynamicAttributes(
        @JsonProperty(required = true) LinkedHashMap<Integer, SdeDynamicAttribute> attributeIDs,
        @JsonProperty(required = true) SdeIOMapping[] inputOutputMapping
    ) {}
    public void readDynamicAttributes(BiConsumer<Integer, SdeDynamicAttributes> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dynamicItemAttributes.yaml");
        HashMap<Integer, SdeDynamicAttributes> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeDynamicAttributes>>() {});
        stationMap.forEach(consumer);
    }

    public record SdeGraphic(
        @Nullable String graphicFile,
        @Nullable String iconFolder,
        @Nullable String sofFactionName,
        @Nullable String sofHullName,
        @Nullable String[] sofLayout,
        @Nullable Integer sofMaterialSetID,
        @Nullable String sofRaceName
    ) {}
    public void readGraphics(BiConsumer<Integer, SdeGraphic> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("graphics.yaml");
        HashMap<Integer, SdeGraphic> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeGraphic>>() {});
        stationMap.forEach(consumer);
    }
}
