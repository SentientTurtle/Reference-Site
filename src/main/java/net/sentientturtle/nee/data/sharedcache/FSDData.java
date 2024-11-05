package net.sentientturtle.nee.data.sharedcache;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.components.ItemStats;
import net.sentientturtle.nee.components.ModuleFitting;
import net.sentientturtle.util.ExceptionUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/// Additional FSD Data not available through SDE
public class FSDData {
    public record TypeList(
        String name,
        String description,
        List<Integer> includedTypeIDs,
        List<Integer> includedGroupIDs,
        List<Integer> includedCategoryIDs,
        List<Integer> excludedTypeIDs,
        List<Integer> excludedGroupIDs,
        List<Integer> excludedCategoryIDs
    ) {}
    public final LinkedHashMap<Integer, TypeList> typeLists;

    public record IOMapping(int resultingType, List<Integer> applicableTypes) {}
    public record DyAttribute(double min, double max, Boolean highIsGood) {}
    public record DynamicAttributes(List<IOMapping> inputOutputMapping, LinkedHashMap<Integer, DyAttribute> attributeIDs) {}
    public final LinkedHashMap<Integer, DynamicAttributes> dynamicAttributes;

    public record WarfareBuff(int displayNameID, String developerDescription, List<BuffModifier> itemModifiers, String showOutputValueInUI) {}
    public record BuffModifier(int dogmaAttributeID) {}
    public final LinkedHashMap<Integer, WarfareBuff> warfareBuffs;

    public final HashMap<Integer, String> localizationStrings;

    private static final Map<String, String> fsdHashes = Map.of(
        "res:/staticdata/typelist.fsdbinary", "75954aab7cea9c139c773f179ce4bf1e",
        "res:/staticdata/dynamicitemattributes.fsdbinary", "559f8311c8d54d5b0203e3ecf2dd332e",
        "res:/localizationfsd/localization_fsd_en-us.pickle", "ffd96792ffd92f7c6a66d58d2b20c944"
    );

    @SuppressWarnings("Convert2Diamond")
    public FSDData(SharedCacheReader sharedCache) {
        for (Map.Entry<String, String> entry : fsdHashes.entrySet()) {
            if (!entry.getValue().equals(sharedCache.resourceHash(entry.getKey()))) {
                throw new IllegalStateException("FSD data out of date: " + entry.getKey());
            }
        }

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            typeLists = objectMapper.readValue(
                FSDData.class.getResourceAsStream("/fsdBinary/typelist.json"),
                new TypeReference<LinkedHashMap<Integer, TypeList>>() {}
            );
            dynamicAttributes = objectMapper.readValue(
                FSDData.class.getResourceAsStream("/fsdBinary/dynamicitemattributes.json"),
                new TypeReference<LinkedHashMap<Integer, DynamicAttributes>>() {}
            );

            localizationStrings = objectMapper.readValue(
                FSDData.class.getResourceAsStream("/fsdBinary/localizationstrings.json"),
                new TypeReference<HashMap<Integer, String>>() {}
            );

            warfareBuffs = new LinkedHashMap<>();
            File tempFile = File.createTempFile("dbuffcollections", ".sqlite", Main.TEMP_DIR);
            Files.write(tempFile.toPath(), sharedCache.getBytes("res:/staticdata/dbuffcollections.static"));
            SQLiteConnection sqLiteConnection = new SQLiteConnection(tempFile);
            sqLiteConnection.open();
            SQLiteStatement st = sqLiteConnection.prepare("SELECT key, value FROM cache");
            while (st.step()) {
                assert !st.columnNull(0) && !st.columnNull(1);
                int buffID = st.columnInt(0);
                String buffJSON = st.columnString(1);
                WarfareBuff buff = objectMapper.readValue(buffJSON, WarfareBuff.class);
                warfareBuffs.put(buffID, buff);
            }
            st.dispose();
            sqLiteConnection.dispose();
            tempFile.deleteOnExit();

        } catch (IOException | SQLiteException e) {
            // Throw a fake RuntimeException as JavaC can't see that sneakyThrow will always throw
            throw ExceptionUtil.<RuntimeException, RuntimeException>sneakyThrow(e);
        }
    }
}
