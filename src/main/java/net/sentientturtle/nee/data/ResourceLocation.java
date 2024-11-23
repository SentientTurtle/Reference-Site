package net.sentientturtle.nee.data;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Mappable;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sharedcache.IconProvider;
import net.sentientturtle.nee.util.MIME;
import net.sentientturtle.nee.util.MapRenderer;
import net.sentientturtle.util.ExceptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Object for various resources, which can be linked to or included inline
 */
@SuppressWarnings("WeakerAccess")
public class ResourceLocation {
    private static final Path OUTPUT_RES_FOLDER = Path.of("rsc");              // Destination resource folder relative to output

    private final ResourceData dataSource;
    private final String destinationPath;

    private ResourceLocation(ResourceData resourceData, String destinationPath) {
        this.dataSource = resourceData;
        this.destinationPath = destinationPath;
    }

    /// Use a file resource, files provided in {@link Main#RES_FOLDER}, path relative to that folder
    public static ResourceLocation file(String path) {
        return new ResourceLocation(new ResourceData.File(Main.RES_FOLDER.resolve(path)), path);
    }

    public static ResourceLocation typeIcon(int typeID, HtmlContext context) {
        if (Main.GENERATE_ICONS) {
            Type invType = context.sde.getTypes().get(typeID);
            Group group = context.sde.getGroups().get(invType.groupID);
            boolean isBPC = group.categoryID == 9 && context.sde.getMetaTypes().getOrDefault(typeID, 1) != 1;

            return new ResourceLocation(new ResourceData.IconProvider64(typeID, isBPC, true), "type_icons/" + typeID + ".png");
        } else {
            Type invType = context.sde.getTypes().get(typeID);
            Group group = context.sde.getGroups().get(invType.groupID);
            try {
                if (group.categoryID == 9) {
                    return new ResourceLocation(new ResourceData.Remote(new URI("https://images.evetech.net/types/" + typeID + "/bp?size=64")), "type_icons/" + typeID + ".png");
                } else {
                    return new ResourceLocation(new ResourceData.Remote(new URI("https://images.evetech.net/types/" + typeID + "/icon?size=64")), "type_icons/" + typeID + ".png");
                }
            } catch (URISyntaxException e) {
                return ExceptionUtil.sneakyThrow(e);   // Shouldn't happen as we construct the URL in this function, so we just re-throw
            }
        }
    }

    public static ResourceLocation typeRender(int typeID) {
        if (Main.GENERATE_ICONS) {
            return new ResourceLocation(new ResourceData.IconProvider512(typeID), "type_renders/" + typeID + ".jpg");
        } else {
            try {
                return new ResourceLocation(
                    new ResourceData.Remote(new URI("https://images.evetech.net/types/" + typeID + "/render?size=512")),
                    "type_renders/" + typeID + ".jpg"
                );
            } catch (URISyntaxException e) {
                return ExceptionUtil.sneakyThrow(e);   // Shouldn't happen as we construct the URL in this function, so we just re-throw
            }
        }
    }

    public static ResourceLocation map(Mappable mappable) {
        return new ResourceLocation(new ResourceData.MapRender(mappable), "maps/" + mappable.getName() + ".png");
    }

    private static String sharedCacheDestination(String resource, HtmlContext context) {
        String hash = context.sharedCache.getResourceHash(resource);
        // If we have a file extension, append that to hash
        // This is required to make Data URI logic below figure out the mime type of the sharedcache resource
        int dotIndex = (resource).lastIndexOf('.');
        if (dotIndex >= 0) {
            return "cache/" + hash + resource.substring(dotIndex);  // Include the dot of the file extension
        } else {
            return "cache/" + hash;
        }
    }

    public static ResourceLocation fromSharedCache(String resource, HtmlContext context) {
            if (context.sharedCache.containsResource(resource)) {
                return new ResourceLocation(new ResourceData.SharedCache(resource), sharedCacheDestination(resource, context));
            } else {
                throw new IllegalStateException("Missing sharedcache entry: " + resource);
            }
    }

    public static ResourceLocation iconOfIconID(int iconID, HtmlContext context) {
        String iconResource = context.sde.getEveIcons().get(iconID);
        if (context.sharedCache.containsResource(iconResource)) {
            return new ResourceLocation(new ResourceData.SharedCache(iconResource), sharedCacheDestination(iconResource, context));
        } else {
            throw new IllegalStateException("Missing sharedcache entry: " + iconResource);
        }
    }

    public static ResourceLocation iconOfCorpID(int corporationID) {
        try {
            return new ResourceLocation(
                new ResourceData.Remote(new URI("https://imageserver.eveonline.com/corporations/" + corporationID + "/logo?size=64")),
                "corp_icons/" + corporationID + ".png"
            );
        } catch (URISyntaxException e) {
            return ExceptionUtil.sneakyThrow(e);   // Shouldn't happen as we construct the URL in this function, so we just re-throw
        }
    }

    public static ResourceLocation searchIndex() {
        return new ResourceLocation(new ResourceData.NoData(), "searchindex.js");
    }

    /// Returns a URI to this resource from the specified context
    public String getURI(HtmlContext context) {
        return getURI(context, false);
    }

    /// Returns a URI to this resource, relative to specified context if {@code isAbsolute} is false, absolute from website root otherwise
    public String getURI(HtmlContext context, boolean isAbsolute) {
        switch (Main.REFERENCE_FORMAT) {
            case EXTERNAL:
                if (dataSource instanceof ResourceData.Remote(URI uri)) {
                    return uri.toASCIIString();
                } else {
                    // Fallthrough to INTERNAL
                }
            case INTERNAL:
                if (!(dataSource instanceof ResourceData.NoData)) {
                    context.addFileDependency(OUTPUT_RES_FOLDER.resolve(destinationPath).toString().replace("\\", "/"), dataSource);
                }
                if (isAbsolute) {
                    return OUTPUT_RES_FOLDER.resolve(destinationPath).toString().replace("\\", "/");
                } else {
                    return context.pathTo(OUTPUT_RES_FOLDER.resolve(destinationPath).toString().replace("\\", "/"));
                }
            case DATA_URI:
                try {
                    StringBuilder builder = new StringBuilder();
                    builder.append("data:")
                        .append(MIME.getType((destinationPath).substring(destinationPath.lastIndexOf('.'))))     // TODO: Move mime type into datasource?
                        .append(";base64,");
                    byte[] encode = Base64.getEncoder().encode(dataSource.getData(context.dataSources));    // Fail intentionally on NoData
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
        /// Links to external resources
        EXTERNAL,
        /// Copies external resources to resource files for self-hosting
        INTERNAL,
        /// DATA_URI; Inline in the HTML document
        DATA_URI
    }

    public sealed interface ResourceData {
        /// Retrieve the file contents of a resource, this is cached for {@link ResourceData.Remote} hosted files, to avoid needless burden on third party servers
        /// Must be thread-safe!
        byte[] getData(DataSources sources) throws IOException;

        /// File (Usually in {@link Main#RES_FOLDER}, but this is not enforced!)
        record File(Path path) implements ResourceData {
            @Override
            public byte[] getData(DataSources sources) throws IOException {
                return Files.readAllBytes(path);
            }
        }

        /// Remotely hosted resource, usually a URL to some web resource (e.g. EVE Online Image Service images)
        record Remote(URI uri) implements ResourceData {
            // Cached to avoid undue burden on external resources (getData() shouldn't be called twice, but some of the services we use are fragile, so just in case)
            // URIs as map key, which is safe
            private static final HashMap<URI, byte[]> remoteCache = new HashMap<>();

            // Synchronized to avoid excessive load on external resources by locking simultaneous requests to 1
            // If changed to parallelize, remoteCache must be made concurrent
            @Override
            public synchronized byte[] getData(DataSources sources) throws IOException {
                return remoteCache.computeIfAbsent(uri, _ -> {
                    try (InputStream stream = uri.toURL().openStream()) {
                        return stream.readAllBytes();
                    } catch (IOException e) {
                        return ExceptionUtil.sneakyThrow(e);
                    }
                });
            }
        }

        /// Type icon Provider
        record IconProvider64(int typeID, boolean isBPC, boolean useOldOverlay) implements ResourceData {
            @Override
            public byte[] getData(DataSources sources) throws IOException {
                byte[] data = IconProvider.getTypeIcon64(this.typeID, sources, this.isBPC, this.useOldOverlay);
                if (data != null) {
                    return data;
                } else {
                    return sources.sharedCache().getBytes("res:/ui/texture/icons/7_64_15.png");
                }
            }
        }

        /// Type render Provider
        record IconProvider512(int typeID) implements ResourceData {
            @Override
            public byte[] getData(DataSources sources) throws IOException {
                return IconProvider.getTypeRender512(this.typeID, sources, false);
            }
        }

        /// Map render (Deprecated and to be replaced with dynamic map)
        record MapRender(Mappable mappable) implements ResourceData {
            @Override
            public byte[] getData(DataSources sources) throws IOException {
                return MapRenderer.render(mappable, sources.SDEData());
            }
        }

        record SharedCache(String resource) implements ResourceData {
            @Override
            public byte[] getData(DataSources sources) throws IOException {
                return sources.sharedCache().getBytes(resource);
            }
        }

        /// Special case data source that doesn't generate data files
        record NoData() implements ResourceData {
            @Override
            public byte[] getData(DataSources sources) throws IOException {
                throw new UnsupportedOperationException();
            }
        }
    }
}
