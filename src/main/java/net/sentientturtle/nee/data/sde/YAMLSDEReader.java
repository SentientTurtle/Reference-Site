package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
public class YAMLSDEReader implements SDEReader {
    private final ZipFile zipFile;
    private final ObjectMapper yamlMapper;

    public YAMLSDEReader(Path sdePath) throws IOException {
        zipFile = new ZipFile(sdePath.toFile());
        yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

    @FunctionalInterface
    private interface Parser<T> {
        void accept(T t) throws IOException;
    }

    private void splitYaml(String entryName, Parser<String> parseSegment) throws IOException {
        ZipEntry entry = zipFile.getEntry(entryName);

        // Split yaml document into individual entries to improve performance & handle format errors more cleanly
        StringBuilder typeBuffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                char first = line.charAt(0);
                if (first >= '0' && first <= '9' && !typeBuffer.isEmpty()) {
                    parseSegment.accept(typeBuffer.toString());
                    typeBuffer.setLength(0);
                }
            }
            if (!typeBuffer.isEmpty()) typeBuffer.append('\n');
            typeBuffer.append(line);
        }

        if (!typeBuffer.isEmpty()) {
            parseSegment.accept(typeBuffer.toString());
        }
    }

    @Override
    public void readCategories(BiConsumer<Integer, SdeCategory> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("categories.yaml");

        yamlMapper.readValue(
            new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
            new TypeReference<LinkedHashMap<Integer, SdeCategory>>() {}
        )
            .forEach(consumer);
    }

    @Override
    public void readGroups(BiConsumer<Integer, SdeGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("groups.yaml");

        yamlMapper.readValue(
            new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
            new TypeReference<LinkedHashMap<Integer, SdeGroup>>() {}
        )
            .forEach(consumer);
    }

    @Override
    public void readTypeBonuses(BiConsumer<Integer, SdeTypeBonus> consumer) throws IOException {
        splitYaml(
            "typeBonus.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeTypeBonus>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readTypes(BiConsumer<Integer, SdeType> consumer) throws IOException {
        splitYaml(
            "types.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeType>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readAttributes(BiConsumer<Integer, SdeAttribute> consumer) throws IOException {
        splitYaml(
            "dogmaAttributes.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeAttribute>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readEffects(BiConsumer<Integer, SdeEffect> consumer) throws IOException {
        splitYaml(
            "dogmaEffects.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeEffect>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readDogma(Consumer<SdeTypeDogma> consumer) throws IOException {
        record DogmaEntry(
            @JsonSetter(nulls = Nulls.AS_EMPTY) ArrayList<SdeTypeAttribute> dogmaAttributes,
            @JsonSetter(nulls = Nulls.AS_EMPTY) ArrayList<SdeTypeEffect> dogmaEffects
        ) {}

        splitYaml(
            "typeDogma.yaml",
            entry -> {
                var dogmaEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, DogmaEntry>>() {});
                LinkedHashMap<Integer, Double> attributeMap = new LinkedHashMap<>();
                for (SdeTypeAttribute attribute : dogmaEntry.getValue().dogmaAttributes) {
                    attributeMap.put(attribute.attributeID(), attribute.value());
                }
                LinkedHashMap<Integer, Boolean> effectMap = new LinkedHashMap<>();
                for (SdeTypeEffect effect : dogmaEntry.getValue().dogmaEffects) {
                    effectMap.put(effect.effectID(), effect.isDefault());
                }

                consumer.accept(new SdeTypeDogma(dogmaEntry.getKey(), attributeMap, effectMap));
            }
        );
    }

    @Override
    public void readIcons(BiConsumer<Integer, SdeIcon> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("icons.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeIcon>>() {}
            )
            .forEach(consumer);
    }

    @Override
    public void readBlueprints(Consumer<SdeBlueprint> consumer) throws IOException {
        splitYaml(
            "blueprints.yaml",
            entry -> {
                consumer.accept(
                    yamlMapper.readValue(
                        entry,
                        new TypeReference<Map.Entry<Integer, SdeBlueprint>>() {}
                    ).getValue()
                );
            }
        );
    }

    @Override
    public void readMaterials(BiConsumer<Integer, SdeTypeMaterials> consumer) throws IOException {
        splitYaml(
            "typeMaterials.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeTypeMaterials>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readSchematics(BiConsumer<Integer, SdePlanetSchematic> consumer) throws IOException {
        splitYaml(
            "planetSchematics.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdePlanetSchematic>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readMetaGroups(BiConsumer<Integer, SdeMetaGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("metaGroups.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeMetaGroup>>() {}
            )
            .forEach(consumer);
    }

    @Override
    public void readFactions(BiConsumer<Integer, SdeFaction> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("factions.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeFaction>>() {}
            )
            .forEach(consumer);
    }

    @Override
    public void readMarketGroups(BiConsumer<Integer, SdeMarketGroup> consumer) throws IOException {
        splitYaml(
            "marketGroups.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeMarketGroup>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readStationOperations(BiConsumer<Integer, SdeStationOperation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("stationOperations.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeStationOperation>>() {}
            )
            .forEach(consumer);
    }

    @Override
    public void readNpcCorporations(BiConsumer<Integer, SdeNpcCorporation> consumer) throws IOException {
        splitYaml(
            "npcCorporations.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeNpcCorporation>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readRegions(BiConsumer<Integer, SdeRegion> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapRegions.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeRegion>>() {}
            )
            .forEach(consumer);
    }

    @Override
    public void readConstellations(BiConsumer<Integer, SdeConstellation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapConstellations.yaml");

        yamlMapper.readValue(
                new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<Integer, SdeConstellation>>() {}
            )
            .forEach(consumer);
    }

    @Override
    public void readSolarSystems(BiConsumer<Integer, SdeSolarSystem> consumer) throws IOException {
        splitYaml(
            "mapSolarSystems.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeSolarSystem>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readAsteroidBelts(BiConsumer<Integer, SdeAsteroidBelt> consumer) throws IOException {
        splitYaml(
            "mapAsteroidBelts.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeAsteroidBelt>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readPlanets(BiConsumer<Integer, SdePlanet> consumer) throws IOException {
        splitYaml(
            "mapPlanets.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdePlanet>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }


    @Override
    public void readMoons(BiConsumer<Integer, SdeMoon> consumer) throws IOException {
        splitYaml(
            "mapMoons.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeMoon>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readStars(BiConsumer<Integer, SdeStar> consumer) throws IOException {
        splitYaml(
            "mapStars.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeStar>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readStargates(BiConsumer<Integer, SdeStargate> consumer) throws IOException {
        splitYaml(
            "mapStargates.yaml",
            entry -> {
                var mapEntry = yamlMapper.readValue(entry, new TypeReference<Map.Entry<Integer, SdeStargate>>() {});
                consumer.accept(mapEntry.getKey(), mapEntry.getValue());
            }
        );
    }

    @Override
    public void readStations(BiConsumer<Integer, SdeStation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("npcStations.yaml");
        HashMap<Integer, SdeStation> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeStation>>() {});
        stationMap.forEach(consumer);
    }

    @Override
    public void readDbuffs(BiConsumer<Integer, SdeDbuff> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dbuffCollections.yaml");
        HashMap<Integer, SdeDbuff> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeDbuff>>() {});
        stationMap.forEach(consumer);
    }

    @Override
    public void readDynamicAttributes(BiConsumer<Integer, SdeDynamicAttributes> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dynamicItemAttributes.yaml");
        HashMap<Integer, SdeDynamicAttributes> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeDynamicAttributes>>() {});
        stationMap.forEach(consumer);
    }

    @Override
    public void readGraphics(BiConsumer<Integer, SdeGraphic> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("graphics.yaml");
        HashMap<Integer, SdeGraphic> stationMap = yamlMapper.readValue(zipFile.getInputStream(entry), new TypeReference<HashMap<Integer, SdeGraphic>>() {});
        stationMap.forEach(consumer);
    }
}