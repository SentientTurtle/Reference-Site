package net.sentientturtle.nee.util;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Mappable;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.util.ExceptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Object for various resources, which can be linked to or included inline
 */
@SuppressWarnings("WeakerAccess")
public class ResourceLocation {
    // TODO: Migrate to path
    private static final String OUTPUT_RES_FOLDER = "./rsc/";              // Destination resource folder relative to output

    private static final Set<String> typeIcons;

    static {
        try (Stream<Path> files = Files.list(Main.RES_FOLDER.resolve("EVE/type_icons/"))) {
            typeIcons = files.map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final DataSource dataSource;
    private final String destinationPath;

    private ResourceLocation(DataSource dataSource, String destinationPath) {
        this.dataSource = dataSource;
        this.destinationPath = destinationPath;
    }

    /// Use a file resource, files provided in {@link Main#RES_FOLDER}, path relative to that folder
    public static ResourceLocation file(String path) {
        return new ResourceLocation(new DataSource.File(Main.RES_FOLDER.resolve(path)), path);
    }

    public static ResourceLocation typeIcon(int typeID, HtmlContext context) {
        if (typeIcons.contains(typeID + "_64.png")) {    // If we have type_icons available in the local resource folder, use those as they are more accurate
            return new ResourceLocation(new DataSource.File(Main.RES_FOLDER.resolve("EVE/type_icons/" + typeID + "_64.png")), "type_icons/" + typeID + ".png");
        } else {
            Type invType = context.data.getTypes().get(typeID);
            if (invType.iconID != null && context.sharedCache.containsResource(context.data.getEveIcons().get(invType.iconID))) {
                return new ResourceLocation(new DataSource.SharedCache(context.data.getEveIcons().get(invType.iconID)), "type_icons/" + typeID + ".png");
            } else {
                Group group = context.data.getGroups().get(invType.groupID);
                try {
                    if (group.categoryID == 9) {
                        return new ResourceLocation(new DataSource.Remote(new URI("https://images.evetech.net/types/" + typeID + "/bp?size=64")), "type_icons/" + typeID + ".png");
                    } else {
                        return new ResourceLocation(new DataSource.Remote(new URI("https://images.evetech.net/types/" + typeID + "/icon?size=64")), "type_icons/" + typeID + ".png");
                    }
                } catch (URISyntaxException e) {
                    return ExceptionUtil.sneakyThrow(e);   // Shouldn't happen as we construct the URL in this function, so we just re-throw
                }
            }
        }
    }

    public static ResourceLocation typeRender(int typeID) {
        try {
            return new ResourceLocation(
                new DataSource.Remote(new URI("https://images.evetech.net/types/" + typeID + "/render?size=512")),
                "type_renders/" + typeID + ".png"
            );
        } catch (URISyntaxException e) {
            return ExceptionUtil.sneakyThrow(e);   // Shouldn't happen as we construct the URL in this function, so we just re-throw
        }
    }

    public static ResourceLocation map(Mappable mappable) {
        return new ResourceLocation(new DataSource.MapRender(mappable), "maps/" + mappable.getName() + ".png");
    }

    private static String sharedCacheDestination(String resource, HtmlContext context) {
        String hash = context.sharedCache.resourceHash(resource);
        // If we have a file extension, append that to hash
        // This is required to make Data URI logic below figure out the mime type of the sharedcache resource
        int dotIndex = (resource).lastIndexOf('.');
        if (dotIndex >= 0) {
            return "cache/" + hash + resource.substring(dotIndex + 1);
        } else {
            return "cache/" + hash;
        }
    }

    public static ResourceLocation fromSharedCache(String resource, HtmlContext context) {
            if (context.sharedCache.containsResource(resource)) {
                return new ResourceLocation(new DataSource.SharedCache(resource), sharedCacheDestination(resource, context));
            } else {
                throw new IllegalStateException("Missing sharedcache entry: " + resource);
            }
    }

    public static ResourceLocation iconOfIconID(int iconID, HtmlContext context) {
        String iconResource = context.data.getEveIcons().get(iconID);
        if (context.sharedCache.containsResource(iconResource)) {
            return new ResourceLocation(new DataSource.SharedCache(iconResource), sharedCacheDestination(iconResource, context));
        } else {
            throw new IllegalStateException("Missing sharedcache entry: " + iconResource);
        }
    }

    public static ResourceLocation iconOfCorpID(int corporationID) {
        try {
            return new ResourceLocation(
                new DataSource.Remote(new URI("https://imageserver.eveonline.com/corporations/" + corporationID + "/logo?size=64")),
                "corp_icons/" + corporationID + ".png"
            );
        } catch (URISyntaxException e) {
            return ExceptionUtil.sneakyThrow(e);   // Shouldn't happen as we construct the URL in this function, so we just re-throw
        }
    }

    public static ResourceLocation searchIndex() {
        return new ResourceLocation(new DataSource.NoData(), "searchindex.js");
    }

    /// Returns a URI to this resource from the specified context
    public String getURI(HtmlContext context) {
        return getURI(context, false);
    }

    /// Returns a URI to this resource, relative to specified context if {@code isAbsolute} is false, absolute from website root otherwise
    public String getURI(HtmlContext context, boolean isAbsolute) {
        switch (Main.REFERENCE_FORMAT) {
            case EXTERNAL:
                if (dataSource instanceof DataSource.Remote remote) {
                    return remote.uri.toASCIIString();
                } else {
                    // Fallthrough to INTERNAL
                }
            case INTERNAL:
                if (!(dataSource instanceof DataSource.NoData)) {
                    context.addFileDependency(OUTPUT_RES_FOLDER + destinationPath, () -> dataSource.getData(context));
                }
                if (isAbsolute) {
                    return OUTPUT_RES_FOLDER + destinationPath;
                } else {
                    return context.pathTo(OUTPUT_RES_FOLDER + destinationPath);
                }
            case DATA_URI:
                try {
                    StringBuilder builder = new StringBuilder();
                    builder.append("data:")
                        .append(MIME.getType((destinationPath).substring(destinationPath.lastIndexOf('.'))))
                        .append(";base64,");
                    byte[] encode = Base64.getEncoder().encode(dataSource.getData(context));    // Fail intentionally on NoData
                    builder.append(new String(encode));
                    return builder.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            default:
                throw new RuntimeException("ResourceLocation#getURI switch must be exhaustive!");
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

    public sealed interface DataSource {
        /// Retrieve the file contents of a resource, this is cached for {@link #REMOTE} hosted files, to avoid needless burden on third party servers
        byte[] getData(HtmlContext context) throws IOException;

        /// File (Usually in {@link Main#RES_FOLDER}, but this is not enforced!)
        record File(Path path) implements DataSource {
            @Override
            public byte[] getData(HtmlContext context) throws IOException {
                return Files.readAllBytes(path);
            }
        }

        /// Remotely hosted resource, usually a URL to some web resource (e.g. EVE Online Image Service images)
        record Remote(URI uri) implements DataSource {
            // Cached to avoid undue burden on external resources (getData() shouldn't be called twice, but some of the services we use are fragile, so just in case)
            // URIs as map key, which is safe
            private static final HashMap<URI, byte[]> remoteCache = new HashMap<>();

            // Must be synchronized if changed to be multithreaded
            @Override
            public byte[] getData(HtmlContext context) throws IOException {
                return remoteCache.computeIfAbsent(uri, _ -> {
                    System.out.println("uri = " + uri);
                    try (InputStream stream = uri.toURL().openStream()) {
                        return stream.readAllBytes();
                    } catch (IOException e) {
                        return ExceptionUtil.sneakyThrow(e);
                    }
                });
            }
        }

        /// Map render (Deprecated and to be replaced with dynamic map)
        record MapRender(Mappable mappable) implements DataSource {
            @Override
            public byte[] getData(HtmlContext context) throws IOException {
                return MapRenderer.render(mappable, context.data);
            }
        }

        record SharedCache(String resource) implements DataSource {
            @Override
            public byte[] getData(HtmlContext context) throws IOException {
                return context.sharedCache.getBytes(resource);
            }
        }

        /// Special case data source that doesn't generate data files
        record NoData() implements DataSource {
            @Override
            public byte[] getData(HtmlContext context) throws IOException {
                throw new UnsupportedOperationException();
            }
        }
    }
}
