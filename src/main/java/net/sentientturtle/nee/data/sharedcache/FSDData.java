package net.sentientturtle.nee.data.sharedcache;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.nee.Main;
import net.sentientturtle.util.ExceptionUtil;

import java.io.IOException;
import java.lang.foreign.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
    ) {
    }

    public final LinkedHashMap<Integer, TypeList> typeLists;

    public record IOMapping(int resultingType, List<Integer> applicableTypes) {
    }

    public record DyAttribute(double min, double max, Boolean highIsGood) {
    }

    public record DynamicAttributes(List<IOMapping> inputOutputMapping, LinkedHashMap<Integer, DyAttribute> attributeIDs) {
    }

    public final LinkedHashMap<Integer, DynamicAttributes> dynamicAttributes;

    public record IconInfo(String folder) {
    }

    public record Graphic(int explosionBucketID, IconInfo iconInfo, String sofRaceName, String sofFactionName, String sofHullName) {
    }

    public final LinkedHashMap<Integer, Graphic> graphics;

    public record WarfareBuff(int displayNameID, String developerDescription, List<BuffModifier> itemModifiers, String showOutputValueInUI) {
    }

    public record BuffModifier(int dogmaAttributeID) {
    }

    public final LinkedHashMap<Integer, WarfareBuff> warfareBuffs;

    public final HashMap<Integer, String> localizationStrings;

    private static final String FSD_SCRIPT_TEMPLATE = """
        import sys
        sys.path.append(%s);
        sys.path.append(%s);
        
        import importlib
        import json
        from json import JSONEncoder
        
        def map_to_dict(o, attributes):
            d = dict()
            for attribute in attributes:
                d[attribute] = getattr(o, attribute)
            return d
        
        class FSDEncoder(JSONEncoder):
            def default(self, o):
                if str(type(o)) == "<type 'cfsd.dict'>":
                    pdict = {}
                    for key, value in o.iteritems():
                        pdict[self.encode(key)] = value
                    return pdict
                elif str(type(o)) == "<type 'cfsd.list'>":
                    return [i for i in o]
                else:
                    return map_to_dict(o, [field for field in dir(o) if not field.startswith("__")])
        
        
        loader = importlib.import_module(%s)
        cdict = loader.load(%s)
        json_out = json.dumps(cdict, cls=FSDEncoder, indent=4)
        """;

    private static final String LOCALIZATION_STRING_SCRIPT_TEMPLATE = """
        import sys
        sys.path.append(%s);
        sys.path.append(%s);
        import pickle
        
        stringdict = {}
        localization = pickle.load(open(%s))
        for key, value in localization[1].iteritems():
            stringdict[key] = value[0]
        
        json_out = json.dumps(stringdict, cls=FSDEncoder, indent=4)
        """;

    // This approach is dumb, but avoids the need for a fragile python 2.7 environment & synchronizing with manually-copied resources
    // It is also quite funny to have cursed python environments
    private static boolean hasRanPython = false; // Because of a bug or Python limitation, we can't re-initialize Python, so this method only permits being ran one

    private static List<String> runPython(String pythonLibrary, List<String> scripts) {
        if (hasRanPython) throw new IllegalStateException("Cannot run python twice!");
        hasRanPython = true;

        ArrayList<String> out = new ArrayList<>(scripts.size());
        try (Arena arena = Arena.ofShared()) {
            Linker linker = Linker.nativeLinker();
            SymbolLookup symbolLookup = SymbolLookup.libraryLookup(pythonLibrary, arena);
            symbolLookup.find("Py_NoSiteFlag")
                .orElseThrow()
                .reinterpret(4)
                .set(ValueLayout.JAVA_INT, 0, 1);

            linker.downcallHandle(symbolLookup.find("Py_Initialize").orElseThrow(), FunctionDescriptor.ofVoid()).invoke();

            MemorySegment main = (MemorySegment) linker.downcallHandle(
                symbolLookup.find("PyImport_AddModule").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            ).invoke(arena.allocateFrom("__main__"));


            for (String script : scripts) {
                int ret = (Integer) linker.downcallHandle(
                    symbolLookup.find("PyRun_SimpleString").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                ).invoke(arena.allocateFrom(script));

                if (ret != 0) throw new IllegalStateException("A python error occurred!");

                if (main.address() == 0) throw new NullPointerException("Null pointer returned from python");

                MemorySegment outObject = (MemorySegment) linker.downcallHandle(
                    symbolLookup.find("PyObject_GetAttrString").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
                ).invoke(main, arena.allocateFrom("json_out"));

                if (outObject.address() == 0) throw new NullPointerException("Null pointer returned from python");

                MemorySegment outCString = (MemorySegment) linker.downcallHandle(
                    symbolLookup.find("PyString_AsString").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
                ).invoke(outObject);

                if (outCString.address() == 0) throw new NullPointerException("Null pointer returned from python");
                String outString = outCString.reinterpret(Long.MAX_VALUE).getString(0);
                out.add(outString);
            }

            linker.downcallHandle(symbolLookup.find("Py_Finalize").orElseThrow(), FunctionDescriptor.ofVoid()).invoke();
        } catch (Throwable e) {
            return ExceptionUtil.sneakyThrow(e);
        }
        return out;
    }

    @SuppressWarnings("Convert2Diamond")
    public FSDData(SharedCacheReader sharedCache) {
        String libPath = Main.RES_FOLDER.resolve("pythonlib").toAbsolutePath().toString().replace("\\", "\\\\");
        String binPath = sharedCache.getCacheFolder().resolve("tq/bin64/").toAbsolutePath().toString().replace("\\", "\\\\");
        String typeListPath = sharedCache.getPath("res:/staticdata/typelist.fsdbinary").toAbsolutePath().toString().replace("\\", "\\\\");
        String dynamicAttributesPath = sharedCache.getPath("res:/staticdata/dynamicitemattributes.fsdbinary").toAbsolutePath().toString().replace("\\", "\\\\");
        String graphicsPath = sharedCache.getPath("res:/staticdata/graphicids.fsdbinary").toAbsolutePath().toString().replace("\\", "\\\\");

        String localizationPath = sharedCache.getPath("res:/localizationfsd/localization_fsd_en-us.pickle").toAbsolutePath().toString().replace("\\", "\\\\");

        List<String> json = runPython(
            sharedCache.getCacheFolder().resolve("tq/bin64/python27.dll").toAbsolutePath().toString(),  // TODO: Check if this approach is possible with the linux client, and if so, make OS-agnostic
            List.of(
                String.format(
                    FSD_SCRIPT_TEMPLATE,
                    "\"" + libPath + "\"",
                    "\"" + binPath + "\"",
                    "\"typeListLoader\"",
                    "\"" + typeListPath + "\""
                ),
                String.format(
                    FSD_SCRIPT_TEMPLATE,
                    "\"" + libPath + "\"",
                    "\"" + binPath + "\"",
                    "\"dynamicItemAttributesLoader\"",
                    "\"" + dynamicAttributesPath + "\""
                ),
                String.format(
                    FSD_SCRIPT_TEMPLATE,
                    "\"" + libPath + "\"",
                    "\"" + binPath + "\"",
                    "\"graphicIDsLoader\"",
                    "\"" + graphicsPath + "\""
                ),
                String.format(
                    LOCALIZATION_STRING_SCRIPT_TEMPLATE,
                    "\"" + libPath + "\"",
                    "\"" + binPath + "\"",
                    "\"" + localizationPath + "\""
                )
            ));

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        try {
            Files.writeString(Path.of("./graphicids.json"), json.get(2));

            typeLists = objectMapper.readValue(json.get(0), new TypeReference<LinkedHashMap<Integer, TypeList>>() {});
            dynamicAttributes = objectMapper.readValue(json.get(1), new TypeReference<LinkedHashMap<Integer, DynamicAttributes>>() {});
            graphics = objectMapper.readValue(json.get(2), new TypeReference<LinkedHashMap<Integer, Graphic>>() {});
            localizationStrings = objectMapper.readValue(json.get(3), new TypeReference<HashMap<Integer, String>>() {});
            warfareBuffs = new LinkedHashMap<>();

            SQLiteConnection sqLiteConnection = new SQLiteConnection(sharedCache.getPath("res:/staticdata/dbuffcollections.static").toFile());
            sqLiteConnection.openReadonly();
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
        } catch (IOException | SQLiteException e) {
            // Throw a fake RuntimeException as JavaC can't see that sneakyThrow will always throw
            throw ExceptionUtil.<RuntimeException, RuntimeException>sneakyThrow(e);
        }
    }
}
