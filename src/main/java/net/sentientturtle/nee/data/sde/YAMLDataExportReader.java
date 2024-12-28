package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.sentientturtle.nee.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/// Work-in-progress data source using the original YAML Static Data Export
// The YAML data is much more annoying to work with and not always conforms to YAML specification.
@SuppressWarnings("Convert2Diamond") // Jackson TypeReference must contain explicit generics to work
public class YAMLDataExportReader {
    private final ThreadLocal<ZipFile> tlZipFile;
    private final ObjectMapper yamlMapper;

    @SuppressWarnings("Convert2Diamond")
    public YAMLDataExportReader(Path sdePath) throws IOException {
        File sdePathFile = sdePath.toFile();

        tlZipFile = ThreadLocal.withInitial(() -> {
            try {
                return new ZipFile(sdePathFile);
            } catch (IOException e) {
                return ExceptionUtil.sneakyThrow(e);
            }
        });
        yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
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
    public void readCategories(BiConsumer<Integer, SdeCategory> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/categories.yaml");

        try {
            yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeCategory>>() {}
            )
                .forEach(consumer);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
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
    public void readGroups(BiConsumer<Integer, SdeGroup> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/groups.yaml");

        try {
            yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeGroup>>() {}
            )
                .forEach(consumer);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeTrait (
        @Nullable Double bonus,
        @JsonProperty(required = true) LocalizedString bonusText,
        @JsonProperty(required = true) int importance,
        @Nullable Integer unitID,
        @Nullable Boolean isPositive
    ) {}

    public record SdeTypeTraits(
        @Nullable ArrayList<SdeTrait> miscBonuses,
        @Nullable ArrayList<SdeTrait> roleBonuses,
        @Nullable HashMap<Integer, ArrayList<SdeTrait>> types,
        @Nullable Integer iconID
    ) {}
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
        @Nullable String sofFactionName,
        @Nullable Double basePrice,
        @Nullable Integer marketGroupID,
        @Nullable Integer variationParentTypeID,
        @Nullable Integer factionID,
        @Nullable LinkedHashMap<Integer, ArrayList<Integer>> masteries,
        @Nullable SdeTypeTraits traits,
        @Nullable Integer sofMaterialSetID
    ) {}
    public void readTypes(BiConsumer<Integer, SdeType> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/types.yaml");

        try {
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
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
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
        @Nullable LocalizedString displayNameID,
        @Nullable LocalizedString tooltipDescriptionID,
        @Nullable LocalizedString tooltipTitleID,
        @Nullable Integer iconID,
        @Nullable Integer unitID,
        @Nullable Integer chargeRechargeTimeID,
        @Nullable Integer maxAttributeID,
        @Nullable Integer minAttributeID,
        @Nullable Boolean displayWhenZero
    ) {}
    public void readAttributes(BiConsumer<Integer, SdeAttribute> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/dogmaAttributes.yaml");

        try {
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
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public record SdeEffect(
        @JsonProperty(required = true) int effectID,
        @JsonProperty(required = true) String effectName
    ) {}
    public void readEffects(BiConsumer<Integer, SdeEffect> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/dogmaEffects.yaml");

        try {
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
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
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
    public void readDogma(Consumer<SdeTypeDogma> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/typeDogma.yaml");

        record DogmaEntry(
            @JsonProperty(required = true) ArrayList<SdeTypeAttribute> dogmaAttributes,
            @JsonProperty(required = true) ArrayList<SdeTypeEffect> dogmaEffects
        ) {}

        try {
            // Split yaml document into individual entries to improve performance
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    char first = line.charAt(0);
                    if (first >= '0' && first <= '9' && !buffer.isEmpty()) {
                        Map.Entry<Integer, DogmaEntry> dogmaEntry = yamlMapper.readValue(buffer.toString(), new TypeReference<Map.Entry<Integer, DogmaEntry>>() {
                        });
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
                Map.Entry<Integer, DogmaEntry> dogmaEntry = yamlMapper.readValue(buffer.toString(), new TypeReference<Map.Entry<Integer, DogmaEntry>>() {
                });
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
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeIcon(
        @Nullable String description,
        @Nullable boolean obsolete,
        @JsonProperty(required = true) String iconFile
    ) {}
    public void readIcons(BiConsumer<Integer, SdeIcon> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/iconIDs.yaml");

        try {
            yamlMapper.readValue(
                    new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                    new TypeReference<LinkedHashMap<Integer, SdeIcon>>() {}
                )
                .forEach(consumer);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
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
    public void readBlueprints(Consumer<SdeBlueprint> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/blueprints.yaml");

        try {
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
                                new TypeReference<Map.Entry<Integer, SdeBlueprint>>() {
                                }
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
                        new TypeReference<Map.Entry<Integer, SdeBlueprint>>() {
                        }
                    ).getValue()
                );
            }
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeTypeMaterial(
        @JsonProperty(required = true) int materialTypeID,
        @JsonProperty(required = true) int quantity
    ) {}
    public record SdeTypeMaterials(@JsonProperty(required = true) ArrayList<SdeTypeMaterial> materials) {}
    public void readMaterials(BiConsumer<Integer, SdeTypeMaterials> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/typeMaterials.yaml");

        try {
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
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdePlanetSchematicItem(@JsonProperty(required = true) boolean isInput, @JsonProperty(required = true) int quantity){}
    public record SdePlanetSchematic(
        @JsonProperty(required = true) int cycleTime,
        @JsonProperty(required = true) LocalizedString nameID,
        @JsonProperty(required = true) ArrayList<Integer> pins,
        @JsonProperty(required = true) LinkedHashMap<Integer, SdePlanetSchematicItem> types
    ) {}
    public void readSchematics(BiConsumer<Integer, SdePlanetSchematic> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/planetSchematics.yaml");

        try {
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
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeMetaGroup(
        @Nullable double[] color,
        @JsonProperty(required = true) LocalizedString nameID,
        @Nullable Integer iconID,
        @Nullable String iconSuffix,
        @Nullable LocalizedString descriptionID
    ) {}
    public void readMetaGroups(BiConsumer<Integer, SdeMetaGroup> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/metaGroups.yaml");

        try {
            yamlMapper.readValue(
                    new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                    new TypeReference<LinkedHashMap<Integer, SdeMetaGroup>>() {}
                )
                .forEach(consumer);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeFaction(
        @Nullable Integer corporationID,
        @JsonProperty(required = true) LocalizedString descriptionID,
        @JsonProperty(required = true) int iconID,
        @JsonProperty(required = true) int[] memberRaces,
        @Nullable Integer militiaCorporationID,
        @JsonProperty(required = true) LocalizedString nameID,
        @Nullable LocalizedString shortDescriptionID,
        @JsonProperty(required = true) double sizeFactor,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) boolean uniqueName
    ) {}
    public void readFactions(BiConsumer<Integer, SdeFaction> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/factions.yaml");

        try {
            yamlMapper.readValue(
                    new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                    new TypeReference<LinkedHashMap<Integer, SdeFaction>>() {}
                )
                .forEach(consumer);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeMarketGroup(
        @Nullable LocalizedString descriptionID,
        @JsonProperty(required = true) LocalizedString nameID,
        @Nullable Integer iconID,
        @JsonProperty(required = true) boolean hasTypes,
        @Nullable Integer parentGroupID
    ) {}
    public void readMarketGroups(BiConsumer<Integer, SdeMarketGroup> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/marketGroups.yaml");

        try {
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
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeRegion(
        @JsonProperty(required = true) double[] center,
        @Nullable Integer descriptionID,
        @Nullable Integer factionID,
        @JsonProperty(required = true) double[] max,
        @JsonProperty(required = true) double[] min,
        @JsonProperty(required = true) int nameID,
        @JsonProperty(required = true) int nebula,
        @JsonProperty(required = true) int regionID,
        @Nullable Integer wormholeClassID
    ) {}
    public record SdeConstellation(
        @JsonProperty(required = true) double[] center,
        @JsonProperty(required = true) double[] max,
        @JsonProperty(required = true) double[] min,
        @JsonProperty(required = true) int nameID,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int constellationID,
        @Nullable Integer factionID,
        @Nullable Integer wormholeClassID
    ) {}
    public record SdeCelestialStatistics(
        @JsonProperty(required = true) double density,
        @JsonProperty(required = true) double eccentricity,
        @JsonProperty(required = true) double escapeVelocity,
        @JsonProperty(required = true) boolean fragmented,
        @JsonProperty(required = true) double life,
        @JsonProperty(required = true) boolean locked,
        @JsonProperty(required = true) double massDust,
        @JsonProperty(required = true) double massGas,
        @JsonProperty(required = true) double orbitPeriod,
        @JsonProperty(required = true) double orbitRadius,
        @JsonProperty(required = true) double pressure,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) double rotationRate,
        @JsonProperty(required = true) String spectralClass,
        @JsonProperty(required = true) double surfaceGravity,
        @JsonProperty(required = true) double temperature
    ) {}
    public record SdeAsteroidBelt(
        @Nullable Integer asteroidBeltNameID,
        @JsonProperty(required = true) double[] position,
        @Nullable SdeCelestialStatistics statistics,
        @JsonProperty(required = true) int typeID
    ) {}
    public record SdePlanetAttributes(
        @JsonProperty(required = true) int heightMap1,
        @JsonProperty(required = true) int heightMap2,
        @JsonProperty(required = true) boolean population,
        @JsonProperty(required = true) int shaderPreset
    ) {}
    public record SdeMapStation(
        @JsonProperty(required = true) int graphicID,
        @JsonProperty(required = true) boolean isConquerable,
        @JsonProperty(required = true) int operationID,
        @JsonProperty(required = true) int ownerID,
        @JsonProperty(required = true) double[] position,
        @JsonProperty(required = true) double reprocessingEfficiency,
        @JsonProperty(required = true) int reprocessingHangarFlag,
        @JsonProperty(required = true) double reprocessingStationsTake,
        @JsonProperty(required = true) int typeID,
        @JsonProperty(required = true) boolean useOperationName
    ) {}
    public record SdeMoon(
        @Nullable Integer moonNameID,
        @JsonProperty(required = true) SdePlanetAttributes planetAttributes,
        @JsonProperty(required = true) double[] position,
        @JsonProperty(required = true) double radius,
        @Nullable SdeCelestialStatistics statistics,
        @JsonProperty(required = true) int typeID,
        @Nullable LinkedHashMap<Integer, SdeMapStation> npcStations
    ) {}
    public record SdePlanet(
        @Nullable Integer planetNameID,
        @Nullable LinkedHashMap<Integer, SdeAsteroidBelt> asteroidBelts,
        @JsonProperty(required = true) int celestialIndex,
        @Nullable LinkedHashMap<Integer, SdeMoon> moons,
        @JsonProperty(required = true) SdePlanetAttributes planetAttributes,
        @JsonProperty(required = true) double[] position,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) SdeCelestialStatistics statistics,
        @JsonProperty(required = true) int typeID,
        @Nullable LinkedHashMap<Integer, SdeMapStation> npcStations
    ) {}
    public record SdeStarStatistics(
        @JsonProperty(required = true) double age,
        @JsonProperty(required = true) double life,
        @JsonProperty(required = true) boolean locked,
        @JsonProperty(required = true) double luminosity,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) String spectralClass,
        @JsonProperty(required = true) double temperature
    ) {}
    public record SdeStar(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) int typeID,
        @JsonProperty(required = true) SdeStarStatistics statistics
    ) {}
    public record SdeStargate(
        @JsonProperty(required = true) int destination,
        @JsonProperty(required = true) double[] position,
        @JsonProperty(required = true) int typeID
    ) {}
    public record SdeSecondarySun(
        @JsonProperty(required = true) int effectBeaconTypeID,
        @JsonProperty(required = true) int itemID,
        @JsonProperty(required = true) double[] position,
        @JsonProperty(required = true) int typeID
    ) {}
    public record SdeSolarSystem(
        @JsonProperty(required = true) boolean border,
        @JsonProperty(required = true) boolean corridor,
        @JsonProperty(required = true) boolean fringe,
        @JsonProperty(required = true) boolean hub,
        @JsonProperty(required = true) boolean international,
        @JsonProperty(required = true) boolean regional,
        @JsonProperty(required = true) double[] center,
        @JsonProperty(required = true) double[] max,
        @JsonProperty(required = true) double[] min,
        @JsonProperty(required = true) double luminosity,
        @JsonProperty(required = true) double radius,
        @JsonProperty(required = true) double security,
        @Nullable String securityClass,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) int solarSystemNameID,
        @Nullable Integer sunTypeID,
        @Nullable Integer wormholeClassID,
        @Nullable int[] disallowedAnchorCategories,
        @Nullable int[] disallowedAnchorGroups,
        @Nullable Integer factionID,
        @JsonProperty(required = true) Map<Integer, SdePlanet> planets,
        @Nullable SdeStar star,
        @JsonProperty(required = true) Map<Integer, SdeStargate> stargates,
        @Nullable String visualEffect,
        @Nullable int descriptionID,
        @Nullable SdeSecondarySun secondarySun
    ) {}
    public record SystemParents(int regionID, int constellationID) {}
    public void readUniverseMap(
        Consumer<SdeRegion> regionConsumer,
        BiConsumer<Integer, SdeConstellation> constellationConsumer,
        BiConsumer<SystemParents, SdeSolarSystem> systemConsumer,
        Consumer<Runnable> addTask
    ) {
        ZipFile zipFile = tlZipFile.get();
        Iterable<? extends ZipEntry> iterable = () -> (Iterator<ZipEntry>) zipFile.entries().asIterator();

        List<String> regions = new ArrayList<>();
        List<String> constellations = new ArrayList<>();
        List<String> systems = new ArrayList<>();
        for (ZipEntry entry : iterable) {
            if (entry.getName().startsWith("universe")) {
                String[] split = entry.getName().split("/");
                String filename = split[split.length - 1];
                switch (filename) {
                    case "landmarks.yaml" -> { /* ignore */ }
                    case "region.yaml" -> regions.add(entry.getName());
                    case "constellation.yaml" -> constellations.add(entry.getName());
                    case "solarsystem.yaml" -> systems.add(entry.getName());
                    default -> throw new IllegalStateException("Unknown yaml universe file: " + entry.getName());
                }
            }
        }


        // Big indentation mess, but strict ordering between these groups is required
        ConcurrentHashMap<String, Integer> idMap = new ConcurrentHashMap<>();

        AtomicInteger regionCount = new AtomicInteger();
        for (String regionEntry : regions) {
            addTask.accept(() -> {
                try {
                    ZipFile zf = tlZipFile.get();
                    String[] split = regionEntry.split("/");
                    String regionName = split[split.length - 2];
                    SdeRegion sdeRegion = yamlMapper.readValue(
                        new String(zf.getInputStream(zf.getEntry(regionEntry)).readAllBytes(), StandardCharsets.UTF_8),
                        new TypeReference<SdeRegion>() {}
                    );
                    idMap.put(regionName, sdeRegion.regionID);
                    regionConsumer.accept(sdeRegion);

                    if (regionCount.incrementAndGet() == regions.size()) {
                        AtomicInteger constellationCount = new AtomicInteger();
                        for (String constellationEntry : constellations) {
                            addTask.accept(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ZipFile zf = tlZipFile.get();
                                        String[] split = constellationEntry.split("/");
                                        String regionName = split[split.length - 3];
                                        String constellationName = split[split.length - 2];
                                        SdeConstellation sdeConstellation = yamlMapper.readValue(
                                            new String(zf.getInputStream(zf.getEntry(constellationEntry)).readAllBytes(), StandardCharsets.UTF_8),
                                            new TypeReference<SdeConstellation>() {}
                                        );
                                        idMap.put(constellationName, sdeConstellation.constellationID);
                                        constellationConsumer.accept(Objects.requireNonNull(idMap.get(regionName)), sdeConstellation);

                                        if (constellationCount.incrementAndGet() == constellations.size()) {


                                            for (String systemEntry : systems) {
                                                addTask.accept(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            ZipFile zf = tlZipFile.get();
                                                            String[] split = systemEntry.split("/");
                                                            String regionName = split[split.length - 4];
                                                            String constellationName = split[split.length - 3];
                                                            SdeSolarSystem sdeSolarSystem = yamlMapper.readValue(
                                                                new String(zf.getInputStream(zf.getEntry(systemEntry)).readAllBytes(), StandardCharsets.UTF_8),
                                                                new TypeReference<SdeSolarSystem>() {}
                                                            );
                                                            systemConsumer.accept(
                                                                new SystemParents(
                                                                    Objects.requireNonNull(idMap.get(regionName)),
                                                                    Objects.requireNonNull(idMap.get(constellationName))
                                                                ),
                                                                sdeSolarSystem
                                                            );
                                                        } catch (IOException e) {
                                                            ExceptionUtil.sneakyThrow(e);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    } catch (IOException e) {
                                        ExceptionUtil.sneakyThrow(e);
                                    }
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    ExceptionUtil.sneakyThrow(e);
                }
            });
        }
    }


    public record SdeStationOperation(
        @JsonProperty(required = true) int activityID,
        @JsonProperty(required = true) double border,
        @JsonProperty(required = true) double corridor,
        @Nullable LocalizedString descriptionID,
        @JsonProperty(required = true) double fringe,
        @JsonProperty(required = true) double hub,
        @JsonProperty(required = true) double manufacturingFactor,
        @JsonProperty(required = true) LocalizedString operationNameID,
        @JsonProperty(required = true) double ratio,
        @JsonProperty(required = true) double researchFactor,
        @JsonProperty(required = true) Set<Integer> services,
        @Nullable Map<Integer, Integer> stationTypes
    ) {}
    public void readStationOperations(BiConsumer<Integer, SdeStationOperation> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("fsd/stationOperations.yaml");

        try {
            yamlMapper.readValue(
                zipFile.getInputStream(entry),
                new TypeReference<LinkedHashMap<Integer, SdeStationOperation>>() {}
            ).forEach(consumer);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeStation(
        @JsonProperty(required = true) int constellationID,
        @JsonProperty(required = true) int corporationID,
        @JsonProperty(required = true) double dockingCostPerVolume,
        @JsonProperty(required = true) double maxShipVolumeDockable,
        @JsonProperty(required = true) double officeRentalCost,
        @JsonProperty(required = true) int operationID,
        @JsonProperty(required = true) int regionID,
        @JsonProperty(required = true) double reprocessingEfficiency,
        @JsonProperty(required = true) int reprocessingHangarFlag,
        @JsonProperty(required = true) double reprocessingStationsTake,
        @JsonProperty(required = true) double security,
        @JsonProperty(required = true) int solarSystemID,
        @JsonProperty(required = true) int stationID,
        @JsonProperty(required = true) String stationName,
        @JsonProperty(required = true) int stationTypeID,
        @JsonProperty(required = true) double x,
        @JsonProperty(required = true) double y,
        @JsonProperty(required = true) double z
    ) {}
    public void readStations(Consumer<SdeStation> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("bsd/staStations.yaml");
        try {
            yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<ArrayList<SdeStation>>() {}).forEach(consumer);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public record SdeItemName(
        @JsonProperty(required = true) int itemID,
        @JsonProperty(required = true) String itemName
    ) {}
    public void readItemNames(Consumer<SdeItemName> consumer) {
        ZipFile zipFile = tlZipFile.get();
        ZipEntry entry = zipFile.getEntry("bsd/invNames.yaml");

        try {
            // Split yaml document into individual entries to improve performance & handle format errors more cleanly
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    char first = line.charAt(0);
                    if (first == '-' && !buffer.isEmpty()) {
                        consumer.accept(yamlMapper.readValue(buffer.toString(), new TypeReference<SdeItemName[]>() {})[0]);
                        buffer.setLength(0);
                    }
                }
                if (!buffer.isEmpty()) buffer.append('\n');
                buffer.append(line);
            }

            if (!buffer.isEmpty()) {
                consumer.accept(yamlMapper.readValue(buffer.toString(), new TypeReference<SdeItemName[]>() {})[0]);
            }
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }
}
