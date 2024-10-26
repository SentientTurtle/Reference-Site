package net.sentientturtle.nee.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/// Work-in-progress data source using the original YAML Static Data Export
// The YAML data is much more annoying to work with and not always conforms to YAML specification.
public class YAMLDataExportReader {
    public static void main(String[] args) throws IOException {
        new YAMLDataExportReader(Path.of("./rsc/EVE"));
    }

    private final HashMap<Integer, SdeCategory> categories;
    private final HashMap<Integer, SdeGroup> groups;
    private final HashMap<Integer, SdeType> types;
    private final HashMap<Integer, HashMap<Integer, Double>> typeAttributes;
    private final HashMap<Integer, HashMap<Integer, Boolean>> typeEffects;


    @SuppressWarnings("Convert2Diamond")    // TypeReference must contain explicit generics to work
    public YAMLDataExportReader(Path dataFolder) throws IOException {
        Path sdePath = dataFolder.resolve("sde.zip");
        if (!Files.exists(sdePath)) throw new IllegalArgumentException("Data folder does not contain `sde.zip`");

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        try (ZipFile zipFile = new ZipFile(sdePath.toFile())) {
            {
                ZipEntry entry = zipFile.getEntry("fsd/categories.yaml");

                categories = yamlMapper.readValue(
                        new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                        new TypeReference<HashMap<Integer, SdeCategory>>() {}
                );

                for (Map.Entry<Integer, SdeCategory> mapEntry : categories.entrySet()) {
                    mapEntry.getValue().id = mapEntry.getKey();
                }
            }
            {
                ZipEntry entry = zipFile.getEntry("fsd/groups.yaml");

                groups = yamlMapper.readValue(
                        new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8),
                        new TypeReference<HashMap<Integer, SdeGroup>>() {}
                );

                for (Map.Entry<Integer, SdeGroup> mapEntry : groups.entrySet()) {
                    mapEntry.getValue().id = mapEntry.getKey();
                }
            }
            {
                ZipEntry entry = zipFile.getEntry("fsd/types.yaml");

                types = new HashMap<>();

                // Split yaml document into individual entries to improve performance
                StringBuilder typeBuffer = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        char first = line.charAt(0);
                        if (first >= '0' && first <= '9' && !typeBuffer.isEmpty()) {
                            String typeYaml = typeBuffer.toString();
                            //noinspection Convert2Diamond
                            Map.Entry<Integer, SdeType> mapEntry = yamlMapper.readValue(typeYaml, new TypeReference<Map.Entry<Integer, SdeType>>() {});
                            mapEntry.getValue().typeID = mapEntry.getKey();
                            types.put(mapEntry.getKey(), mapEntry.getValue());
                            typeBuffer.setLength(0);
                        }
                    }
                    if (!typeBuffer.isEmpty()) typeBuffer.append('\n');
                    typeBuffer.append(line);
                }

                if (!typeBuffer.isEmpty()) {
                    String typeYaml = typeBuffer.toString();
                    //noinspection Convert2Diamond
                    Map.Entry<Integer, SdeType> mapEntry = yamlMapper.readValue(typeYaml, new TypeReference<Map.Entry<Integer, SdeType>>() {});
                    mapEntry.getValue().typeID = mapEntry.getKey();
                    types.put(mapEntry.getKey(), mapEntry.getValue());
                }
            }
            {
                ZipEntry entry = zipFile.getEntry("fsd/typeDogma.yaml");

                typeAttributes = new HashMap<>();
                typeEffects = new HashMap<>();

                record DogmaEntry(ArrayList<SdeTypeAttribute> dogmaAttributes, ArrayList<SdeTypeEffect> dogmaEffects) {}

                // Split yaml document into individual entries to improve performance
                StringBuilder typeBuffer = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        char first = line.charAt(0);
                        if (first >= '0' && first <= '9' && !typeBuffer.isEmpty()) {
                            String dogmaYaml = typeBuffer.toString();
                            //noinspection Convert2Diamond
                            Map.Entry<Integer, DogmaEntry> dogmaEntry = yamlMapper.readValue(dogmaYaml, new TypeReference<Map.Entry<Integer, DogmaEntry>>() {});
                            HashMap<Integer, Double> attributeMap = typeAttributes.computeIfAbsent(dogmaEntry.getKey(), _ -> new HashMap<>());
                            HashMap<Integer, Boolean> effectMap = typeEffects.computeIfAbsent(dogmaEntry.getKey(), _ -> new HashMap<>());

                            for (SdeTypeAttribute attribute : dogmaEntry.getValue().dogmaAttributes) {
                                attributeMap.put(attribute.attributeID, attribute.value);
                            }
                            for (SdeTypeEffect effect : dogmaEntry.getValue().dogmaEffects) {
                                effectMap.put(effect.effectID, effect.isDefault);
                            }

                            typeBuffer.setLength(0);
                        }
                    }
                    if (!typeBuffer.isEmpty()) typeBuffer.append('\n');
                    typeBuffer.append(line);
                }

                if (!typeBuffer.isEmpty()) {
                    String dogmaYaml = typeBuffer.toString();
                    //noinspection Convert2Diamond
                    Map.Entry<Integer, DogmaEntry> dogmaEntry = yamlMapper.readValue(dogmaYaml, new TypeReference<Map.Entry<Integer, DogmaEntry>>() {});
                    HashMap<Integer, Double> attributeMap = typeAttributes.computeIfAbsent(dogmaEntry.getKey(), _ -> new HashMap<>());
                    HashMap<Integer, Boolean> effectMap = typeEffects.computeIfAbsent(dogmaEntry.getKey(), _ -> new HashMap<>());

                    for (SdeTypeAttribute attribute : dogmaEntry.getValue().dogmaAttributes) {
                        attributeMap.put(attribute.attributeID, attribute.value);
                    }
                    for (SdeTypeEffect effect : dogmaEntry.getValue().dogmaEffects) {
                        effectMap.put(effect.effectID, effect.isDefault);
                    }
                }
            }
        }
    }

    public static class SdeCategory {
        public int id;
        public HashMap<String, String> name;
        public Boolean published;
        public Integer iconID;
    }

    public static class SdeGroup {
        public int id;
        public Integer categoryID;
        public HashMap<String, String> name;
        public Boolean published;
        public Integer iconID;
        public Boolean anchorable;
        public Boolean anchored;
        public Boolean fittableNonSingleton;
        public Boolean useBasePrice;
    }

    public record SdeTrait (
        Double bonus,
        HashMap<String, String> bonusText,
        Integer importance,
        Integer unitID,
        Boolean isPositive
    ) {}

    public record SdeTypeTraits(
            ArrayList<SdeTrait> miscBonuses,
            ArrayList<SdeTrait> roleBonuses,
            HashMap<Integer, ArrayList<SdeTrait>> types,
            Integer iconID
    ) {}

    private static class SdeType {
        public int typeID;
        public int groupID;
        public boolean published;
        public HashMap<String, String> name;
        public HashMap<String, String> description;
        public Double mass;
        public Double volume;
        public Double radius;
        public Double capacity;

        public int portionSize;
        public Integer graphicID;
        public Integer soundID;
        public Integer iconID;
        public Integer raceID;
        public Integer metaGroupID;
        public String sofFactionName;
        public Double basePrice;
        public Integer marketGroupID;
        public Integer variationParentTypeID;
        public Integer factionID;
        public HashMap<Integer, ArrayList<Integer>> masteries;
        public SdeTypeTraits traits;
        public Integer sofMaterialSetID;
    }

    private record SdeTypeAttribute(int attributeID, double value) {}
    private record SdeTypeEffect(int effectID, boolean isDefault) {}
}
