package net.sentientturtle.nee.data.sde;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JSONLSDEReader implements SDEReader {
    private final ZipFile zipFile;
    private final ObjectMapper jsonMapper;

    public JSONLSDEReader(Path sdePath) throws IOException {
        zipFile = new ZipFile(sdePath.toFile());
        jsonMapper = new ObjectMapper()
            .registerModule(new SimpleModule("JSONL map override") {
                {this.addDeserializer(LinkedHashMap.class, new JsonlMapDeserializer());}
            })
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    // _key map entry with inline value
    private record InlineEntry<K, V>(
        K _key,
        @JsonUnwrapped V value
    ) {}

    // _key map entry with explicit _value
    private record WrappedEntry<K, V>(
        K _key,
        V _value
    ) {}

    private static class JsonlMapDeserializer extends JsonDeserializer<LinkedHashMap<?, ?>> implements ContextualDeserializer {
        private DeserializationKind kind = DeserializationKind.NORMAL;
        private JavaType jsonType;

        private enum DeserializationKind {
            NORMAL, WRAPPED, INLINE
        }

        @Override
        public LinkedHashMap<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return switch (this.kind) {
                case NORMAL -> ctxt.readValue(p, jsonType);
                case WRAPPED -> {
                    LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
                    WrappedEntry<?, ?>[] keyedArray = ctxt.readValue(p, jsonType);
                    for (WrappedEntry<?, ?> keyed : keyedArray) {
                        map.put(keyed._key, keyed._value);
                    }
                    yield map;
                }
                case INLINE -> {
                    LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
                    InlineEntry<?, ?>[] inlineEntryArray = ctxt.readValue(p, jsonType);
                    for (InlineEntry<?, ?> inlineEntry : inlineEntryArray) {
                        map.put(inlineEntry._key, inlineEntry.value);
                    }
                    yield map;
                }
            };
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            JsonlMapDeserializer deserializer = new JsonlMapDeserializer();

            JavaType mapType = property.getType();
            TypeFactory typeFactory = ctxt.getTypeFactory();

            JavaType keyType = mapType.containedType(0);
            if (keyType.isTypeOrSubTypeOf(String.class)) {
                // String-keyed maps are encoded as normal json objects whose keys are the map's key, and whose values are the map's values
                deserializer.jsonType = mapType;
                deserializer.kind = DeserializationKind.NORMAL;
            } else {
                // For non-string keys, the map is an array of entries, each having the key set in the `_key` field
                JavaType valueType = mapType.containedType(1);
                if (valueType.isArrayType() || valueType.isTypeOrSubTypeOf(String.class) || valueType.isTypeOrSubTypeOf(Number.class) || valueType.isTypeOrSubTypeOf(Boolean.class)) {
                    // non-object value types are wrapped in a _value field
                    JavaType arrayMap = typeFactory.constructParametricType(WrappedEntry.class, keyType, valueType);
                    deserializer.jsonType = typeFactory.constructArrayType(arrayMap);
                    deserializer.kind = DeserializationKind.WRAPPED;
                } else {
                    // json-object values have their fields inlined in the map entry
                    JavaType keyedType = typeFactory.constructParametricType(InlineEntry.class, keyType, valueType);
                    deserializer.jsonType = typeFactory.constructArrayType(keyedType);
                    deserializer.kind = DeserializationKind.INLINE;
                }
            }
            return deserializer;
        }
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

    @Override
    public void readCategories(Consumer<SdeCategory> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("categories.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeCategory.class));
        }
    }

    @Override
    public void readGroups(Consumer<SdeGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("groups.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeGroup.class));
        }
    }

    @Override
    public void readTypeBonuses(Consumer<SdeTypeBonus> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeBonus.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeTypeBonus.class));
        }
    }

    @Override
    public void readTypeLists(Consumer<SdeTypeList> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeLists.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeTypeList.class));
        }
    }

    @Override
    public void readTypes(Consumer<SdeType> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("types.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeType.class));
        }
    }

    @Override
    public void readAttributes(Consumer<SdeAttribute> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dogmaAttributes.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeAttribute.class));
        }
    }

    @Override
    public void readEffects(Consumer<SdeEffect> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dogmaEffects.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeEffect.class));
        }
    }

    @Override
    public void readDogma(Consumer<SdeTypeDogma> consumer) throws IOException {
        record DogmaEntry(
            @JsonSetter(nulls = Nulls.AS_EMPTY) ArrayList<SdeTypeAttribute> dogmaAttributes,
            @JsonSetter(nulls = Nulls.AS_EMPTY) ArrayList<SdeTypeEffect> dogmaEffects
        ) {}

        ZipEntry entry = zipFile.getEntry("typeDogma.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, DogmaEntry>>() {});
            LinkedHashMap<Integer, Double> attributeMap = new LinkedHashMap<>();
            for (SdeTypeAttribute attribute : keyed.value.dogmaAttributes) {
                attributeMap.put(attribute.attributeID(), attribute.value());
            }
            LinkedHashMap<Integer, Boolean> effectMap = new LinkedHashMap<>();
            for (SdeTypeEffect effect : keyed.value.dogmaEffects) {
                effectMap.put(effect.effectID(), effect.isDefault());
            }

            consumer.accept(new SdeTypeDogma(keyed._key, attributeMap, effectMap));
        }
    }

    @Override
    public void readIcons(Consumer<SdeIcon> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("icons.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeIcon.class));
        }
    }

    @Override
    public void readBlueprints(Consumer<SdeBlueprint> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("blueprints.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeBlueprint>>() {}).value);
        }
    }

    @Override
    public void readMaterials(Consumer<SdeTypeMaterials> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeMaterials.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeTypeMaterials.class));
        }
    }

    @Override
    public void readCompressibleTypes(Consumer<SdeCompressibleType> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("compressibleTypes.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeCompressibleType.class));
        }
    }

    @Override
    public void readSchematics(Consumer<SdePlanetSchematic> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("planetSchematics.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdePlanetSchematic.class));
        }
    }

    @Override
    public void readMetaGroups(Consumer<SdeMetaGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("metaGroups.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeMetaGroup.class));
        }
    }

    @Override
    public void readFactions(Consumer<SdeFaction> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("factions.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeFaction.class));
        }
    }

    @Override
    public void readMarketGroups(Consumer<SdeMarketGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("marketGroups.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeMarketGroup.class));
        }
    }

    @Override
    public void readStationOperations(Consumer<SdeStationOperation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("stationOperations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeStationOperation.class));
        }
    }

    @Override
    public void readNpcCorporations(Consumer<SdeNpcCorporation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("npcCorporations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeNpcCorporation.class));
        }
    }

    @Override
    public void readRegions(Consumer<SdeRegion> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapRegions.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeRegion.class));
        }
    }

    @Override
    public void readConstellations(Consumer<SdeConstellation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapConstellations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeConstellation.class));
        }
    }

    @Override
    public void readSolarSystems(Consumer<SdeSolarSystem> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapSolarSystems.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeSolarSystem.class));
        }
    }

    @Override
    public void readAsteroidBelts(Consumer<SdeAsteroidBelt> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapAsteroidBelts.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeAsteroidBelt.class));
        }
    }

    @Override
    public void readPlanets(Consumer<SdePlanet> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapPlanets.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdePlanet.class));
        }
    }


    @Override
    public void readMoons(Consumer<SdeMoon> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapMoons.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeMoon.class));
        }
    }

    @Override
    public void readStars(Consumer<SdeStar> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapStars.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeStar.class));
        }
    }

    @Override
    public void readStargates(Consumer<SdeStargate> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapStargates.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeStargate.class));
        }
    }

    @Override
    public void readStations(Consumer<SdeStation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("npcStations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeStation.class));
        }
    }

    @Override
    public void readDbuffs(Consumer<SdeDbuff> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dbuffCollections.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeDbuff.class));
        }
    }

    @Override
    public void readDynamicAttributes(Consumer<SdeDynamicAttributes> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dynamicItemAttributes.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeDynamicAttributes.class));
        }
    }

    @Override
    public void readGraphics(Consumer<SdeGraphic> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("graphics.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeGraphic.class));
        }
    }

    @Override
    public void readCloneGrades(Consumer<SdeCloneGrade> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("cloneGrades.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            consumer.accept(jsonMapper.readValue(line, SdeCloneGrade.class));
        }
    }
}