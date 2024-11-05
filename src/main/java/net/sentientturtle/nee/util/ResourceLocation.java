package net.sentientturtle.nee.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.html.HTMLUtil;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Mappable;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.pages.PageKind;
import net.sentientturtle.util.ExceptionUtil;
import net.sentientturtle.util.tuple.Tuple2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Object for various resources, which can be linked to or included inline
 */
@SuppressWarnings("WeakerAccess")
public class ResourceLocation {
    // TODO: Move these to Main
    public static final ReferenceFormat REFERENCE_FORMAT = ReferenceFormat.EXTERNAL;
    public static final String OUTPUT_RES_FOLDER = "rsc/";              // Destination resource folder relative to output
    public static final String RES_FOLDER = "rsc/";                     // Input resource folder relative to working dir

    private static final Set<String> typeIcons;
    static {
        try (Stream<Path> files = Files.list(Path.of(RES_FOLDER + "EVE/type_icons/"))) {
            typeIcons = files.map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Object value;
    private final ResourceType resourceType;

    private ResourceLocation(Object value, ResourceType resourceType) {
        this.value = value;
        this.resourceType = resourceType;
        if (!resourceType.valueClass.isInstance(value)) {
            throw new IllegalArgumentException("Invalid value type (" + value + ") must be of type: " + resourceType.valueClass);
        }
    }

    /// Use a file resource, files provided in {@link #RES_FOLDER}, path relative to that folder
    public static ResourceLocation file(String path) {
        return new ResourceLocation(path, ResourceType.FILE);
    }

    public static ResourceLocation typeRender(int typeID) {
        return new ResourceLocation(typeID, ResourceType.TYPE_RENDER_512);
    }

    public static ResourceLocation map(Mappable mappable) {
        return new ResourceLocation(mappable, ResourceType.MAP);
    }

    public static ResourceLocation fromSharedCache(String resource) {
        return new ResourceLocation(resource, ResourceType.SHARED_CACHE);
    }

    public static ResourceLocation iconOfIconID(int iconID) {
        return new ResourceLocation(iconID, ResourceType.ITEM_ICON);
    }

    public static ResourceLocation iconOfTypeID(int typeID) {
        return new ResourceLocation(typeID, ResourceType.TYPE_ICON_64);
    }

    public static ResourceLocation iconOfCorpID(int corporationID) {
        return new ResourceLocation(corporationID, ResourceType.CORP_ICON);
    }

    public static ResourceLocation searchIndex() {
        return new ResourceLocation("searchindex.js", ResourceType.SEARCH_INDEX);
    }

    private Tuple2<Object, OriginType> getOrigin(HtmlContext context) {
        return switch (resourceType) {
            case FILE -> new Tuple2<>(RES_FOLDER + value, OriginType.FILE);
            case TYPE_ICON_64 -> {
                if (typeIcons.contains(value + "_64.png")) {    // If we have type_icons available in the local resource folder, use those as they are more accurate
                    yield new Tuple2<>(RES_FOLDER + "EVE/type_icons/" + value + "_64.png", OriginType.FILE);
                } else {
                    Type invType = context.data.getTypes().get((Integer) value);
                    if (invType.iconID != null && context.sharedCache.containsResource(context.data.getEveIcons().get(invType.iconID))) {
                        yield new Tuple2<>(context.data.getEveIcons().get(invType.iconID), OriginType.SHARED_CACHE);
                    } else {
                        Group group = context.data.getGroups().get(invType.groupID);
                        if (group.categoryID == 9) {
                            yield new Tuple2<>("https://images.evetech.net/types/" + value + "/bp?size=64", OriginType.REMOTE);
                        } else {
                            yield new Tuple2<>("https://images.evetech.net/types/" + value + "/icon?size=64", OriginType.REMOTE);
                        }
                    }
                }
            }
            case TYPE_RENDER_512 -> new Tuple2<>("https://images.evetech.net/types/" + value + "/render?size=512", OriginType.REMOTE);
            case ITEM_ICON -> {
                var icon = context.data.getEveIcons().get((Integer) value);
                if (context.sharedCache.containsResource(icon)) {
                    yield new Tuple2<>(icon, OriginType.SHARED_CACHE);
                } else {
                    throw new IllegalStateException("Missing shared cache entry: " + icon);
                }
            }
            case CORP_ICON -> new Tuple2<>("https://imageserver.eveonline.com/corporations/" + value + "/logo?size=64", OriginType.REMOTE);
            case MAP -> new Tuple2<>(value, OriginType.MAP_RENDER);
            case SEARCH_INDEX -> new Tuple2<>(value, OriginType.SEARCH_INDEX);
            case SHARED_CACHE -> {
                if (context.sharedCache.containsResource((String) value)) {
                    yield new Tuple2<>(value, OriginType.SHARED_CACHE);
                } else {
                    throw new IllegalStateException("Missing shared cache entry: " + value);
                }
            }
        };
    }

    /// Returns a URI to this resource from the specified context
    public String getURI(HtmlContext context) {
        return getURI(context, false);
    }

    /// Returns a URI to this resource, relative to specified context if {@code isAbsolute} is false, absolute from website root otherwise
    public String getURI(HtmlContext context, boolean isAbsolute) {
        Tuple2<Object, OriginType> origin = getOrigin(context);

        switch (REFERENCE_FORMAT) {
            case EXTERNAL:
                if (origin.v2 == OriginType.REMOTE) {
                    return (String) origin.v1;
                } else {
                    // Fallthrough to INTERNAL
                }
            case INTERNAL:
                String dest = switch (resourceType) {
                    case FILE, SEARCH_INDEX -> (String) value;
                    case TYPE_ICON_64 -> "type_icons/" + value + ".png";
                    case TYPE_RENDER_512 -> "type_renders/" + value + ".png";
                    case CORP_ICON -> "corp_icons/" + value + ".png";
                    case ITEM_ICON -> "item_icons/" + value + ".png";
                    case MAP -> "maps/" + ((Mappable) value).getName() + ".png";
                    case SHARED_CACHE -> {
                        String hash = context.sharedCache.resourceHash((String) value);
                        int dotIndex = ((String) value).lastIndexOf('.');
                        if (dotIndex >= 0) {
                            yield "cache/" + hash + ((String) value).substring(dotIndex + 1);
                        } else {
                            yield "cache/" + hash;
                        }
                    }
                };
                context.addFileDependency(OUTPUT_RES_FOLDER + dest, () -> origin.v2.getData(origin.v1, context));
                if (isAbsolute) {
                    return OUTPUT_RES_FOLDER + dest;
                } else {
                    return context.pathTo(OUTPUT_RES_FOLDER + dest);
                }
            case DATA_URI:
                try {
                    StringBuilder builder = new StringBuilder();
                    builder.append("data:")
                        .append(MIME.getType(((String) origin.v1).substring(((String) origin.v1).lastIndexOf('.'))))
                        .append(";base64,");
                    byte[] encode = Base64.getEncoder().encode(origin.v2.getData(origin.v1, context));
                    builder.append(new String(encode));
                    return builder.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
        throw new RuntimeException("UNKNOWN RESOURCE FORMAT: " + REFERENCE_FORMAT);
    }

    /// Resource types & the data type used to specify which resource
    public enum ResourceType {
        // File path, relative to RES_FOLDER
        FILE(String.class),
        // Type id
        TYPE_ICON_64(Integer.class),
        // Type id
        TYPE_RENDER_512(Integer.class),
        // Icon id
        ITEM_ICON(Integer.class),    // Not to be confused with type icons
        // Corporation ID
        CORP_ICON(Integer.class),
        // Mappable object
        MAP(Mappable.class),
        // Search index filename
        SEARCH_INDEX(String.class),
        // Shared cache resource path
        SHARED_CACHE(String.class);

        public final Class<?> valueClass;

        ResourceType(Class<?> valueClass) {
            this.valueClass = valueClass;
        }
    }

    /// Resource URI/reference format
    public enum ReferenceFormat {
        /// Links to external (off-site) resources
        EXTERNAL, // TODO: Remove; privacy regulations
        /// Links to internal (hosted alongside the rest of this website) resources
        INTERNAL,
        /// DATA_URI; Inline in the HTML document
        DATA_URI
    }

    /// Origin for a resource
    public enum OriginType {
        /// File in {@link #RES_FOLDER}
        FILE {
            @Override
            public byte[] getData(Object path, HtmlContext context) throws IOException {
                assert path instanceof String;
                return Files.readAllBytes(new File((String) path).toPath());
            }
        },
        /// Remotely hosted resource
        REMOTE {
            @Override
            public byte[] getData(Object value, HtmlContext context) throws IOException {
                assert value instanceof String;
                return remoteCache.computeIfAbsent((String) value, s -> {
                    try (InputStream stream = new URI(s).toURL().openStream()){
                        return stream.readAllBytes();
                    } catch (IOException e) {
                        return ExceptionUtil.sneakyThrow(e);
                    } catch (URISyntaxException e) {
                        return ExceptionUtil.sneakyThrow(new IOException(e));
                    }
                });
            }
        },
        /// Map render
        MAP_RENDER {
            @Override
            public byte[] getData(Object value, HtmlContext context) {
                assert value instanceof Mappable;
                return MapRenderer.render((Mappable) value, context.data);
            }
        },
        /// Search index
        SEARCH_INDEX {
            private record IndexEntry(String index, String name, String path, String icon) {}

            @Override
            public byte[] getData(Object value, HtmlContext context) {
                assert value.equals("search_index.js");

                ObjectMapper objectMapper = new ObjectMapper();

                List<IndexEntry> indexEntries = PageKind.pageStream(context.data)
                    .map(page -> new IndexEntry(
                        page.name().toLowerCase(),
                        page.name(),
                        HTMLUtil.escapeAttributeValue(page.getPath()),
                        page.getIcon() != null ? HTMLUtil.escapeAttributeValue(page.getIcon().getURI(context)) : null
                    ))
                    .collect(Collectors.toList());

                String json = null;
                try {
                    json = objectMapper.writeValueAsString(indexEntries);
                } catch (JsonProcessingException e) {
                    ExceptionUtil.sneakyThrow(e);
                }

                String js = "const searchindex = " + json + ";\nexport default searchindex;";
                return js.getBytes(StandardCharsets.UTF_8);
            }
        },
        /// Shared cache
        SHARED_CACHE {
            @Override
            public byte[] getData(Object value, HtmlContext context) throws IOException {
                return context.sharedCache.getBytes((String) value);
            }
        };
        private static final HashMap<String, byte[]> remoteCache = new HashMap<>();

        /// Retrieve the file contents of a resource, this is cached for {@link #REMOTE} hosted files, to avoid needless burden on third party servers
        public abstract byte[] getData(Object value, HtmlContext context) throws IOException;
    }
}
