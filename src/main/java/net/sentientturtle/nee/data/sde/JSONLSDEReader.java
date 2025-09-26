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
import java.util.function.BiConsumer;
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
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
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
    public void readCategories(BiConsumer<Integer, SdeCategory> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("categories.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeCategory>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readGroups(BiConsumer<Integer, SdeGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("groups.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeGroup>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readTypeBonuses(BiConsumer<Integer, SdeTypeBonus> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeBonus.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeTypeBonus>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readTypes(BiConsumer<Integer, SdeType> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("types.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeType>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readAttributes(BiConsumer<Integer, SdeAttribute> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dogmaAttributes.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeAttribute>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readEffects(BiConsumer<Integer, SdeEffect> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dogmaEffects.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeEffect>>() {});
            consumer.accept(keyed._key, keyed.value);
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
    public void readIcons(BiConsumer<Integer, SdeIcon> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("icons.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeIcon>>() {});
            consumer.accept(keyed._key, keyed.value);
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
    public void readMaterials(BiConsumer<Integer, SdeTypeMaterials> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("typeMaterials.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeTypeMaterials>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readSchematics(BiConsumer<Integer, SdePlanetSchematic> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("planetSchematics.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdePlanetSchematic>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readMetaGroups(BiConsumer<Integer, SdeMetaGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("metaGroups.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeMetaGroup>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readFactions(BiConsumer<Integer, SdeFaction> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("factions.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeFaction>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readMarketGroups(BiConsumer<Integer, SdeMarketGroup> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("marketGroups.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeMarketGroup>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readStationOperations(BiConsumer<Integer, SdeStationOperation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("stationOperations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeStationOperation>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readNpcCorporations(BiConsumer<Integer, SdeNpcCorporation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("npcCorporations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeNpcCorporation>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readRegions(BiConsumer<Integer, SdeRegion> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapRegions.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeRegion>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readConstellations(BiConsumer<Integer, SdeConstellation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapConstellations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeConstellation>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readSolarSystems(BiConsumer<Integer, SdeSolarSystem> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapSolarSystems.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeSolarSystem>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readAsteroidBelts(BiConsumer<Integer, SdeAsteroidBelt> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapAsteroidBelts.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeAsteroidBelt>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readPlanets(BiConsumer<Integer, SdePlanet> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapPlanets.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdePlanet>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }


    @Override
    public void readMoons(BiConsumer<Integer, SdeMoon> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapMoons.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeMoon>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readStars(BiConsumer<Integer, SdeStar> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapStars.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeStar>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readStargates(BiConsumer<Integer, SdeStargate> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("mapStargates.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeStargate>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readStations(BiConsumer<Integer, SdeStation> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("npcStations.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeStation>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readDbuffs(BiConsumer<Integer, SdeDbuff> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dbuffCollections.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeDbuff>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readDynamicAttributes(BiConsumer<Integer, SdeDynamicAttributes> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("dynamicItemAttributes.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeDynamicAttributes>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }

    @Override
    public void readGraphics(BiConsumer<Integer, SdeGraphic> consumer) throws IOException {
        ZipEntry entry = zipFile.getEntry("graphics.jsonl");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        String line;
        while ((line = br.readLine()) != null) {
            var keyed = jsonMapper.readValue(line, new TypeReference<InlineEntry<Integer, SdeGraphic>>() {});
            consumer.accept(keyed._key, keyed.value);
        }
    }
}