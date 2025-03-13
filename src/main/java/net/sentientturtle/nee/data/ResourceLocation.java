package net.sentientturtle.nee.data;

import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.data.sharedcache.IconProvider;
import net.sentientturtle.nee.util.MIME;
import net.sentientturtle.nee.util.ExceptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Object for various web file "resources" such as images/scripts/etc, which can be linked to or included inline
 */
@SuppressWarnings("WeakerAccess")
public class ResourceLocation {
    public static final Path OUTPUT_RES_FOLDER = Path.of("rsc");              // Destination resource folder relative to website output folder
    private final ResourceData dataSource;
    private final String destinationPath;

    /// Instances of ResourceLocation are created through the factory methods
    ///
    /// (See: Static methods)
    private ResourceLocation(ResourceData resourceData, String destinationPath) {
        this.dataSource = resourceData;
        this.destinationPath = destinationPath;
    }

    /// Use a file resource, files provided in {@link Main#RES_FOLDER}, path relative to that folder
    public static ResourceLocation file(String path) {
        return new ResourceLocation(new ResourceData.File(Main.RES_FOLDER.resolve(path)), path);
    }

    /// Use a file resource, files provided in {@link Main#RES_FOLDER}, path relative to that folder
    public static ResourceLocation remoteURL(String url) {
        return new ResourceLocation(new ResourceData.Remote(url), null);
    }

    /// Use a file resource, files provided in {@link Main#RES_FOLDER}, path relative to that folder
    public static ResourceLocation localPath(Path path) {
        return new ResourceLocation(new ResourceData.Remote(path.toString()), null);
    }

    /// ResourceLocation for an item-type icon (64x64 PNG file)
    public static ResourceLocation typeIcon(int typeID, HtmlContext context) {
        if (Main.GENERATE_ICONS) {
            Type invType = context.sde.getTypes().get(typeID);
            Group group = context.sde.getGroups().get(invType.groupID);
            int metaGroup = context.sde.getMetaTypes().getOrDefault(typeID, 1);
            boolean isBPC = group.categoryID == 9 && metaGroup != 1 && metaGroup != 54;

            return new ResourceLocation(new ResourceData.IconProvider64(typeID, isBPC, true), "type_icons/" + typeID + ".png");
        } else {
            Type invType = context.sde.getTypes().get(typeID);
            Group group = context.sde.getGroups().get(invType.groupID);
            if (group.categoryID == 9) {
                return new ResourceLocation(new ResourceData.Remote("https://images.evetech.net/types/" + typeID + "/bp?size=64"), null);
            } else {
                return new ResourceLocation(new ResourceData.Remote("https://images.evetech.net/types/" + typeID + "/icon?size=64"), null);
            }
        }
    }

    /// ResourceLocation for an item-type render (512x512 JPG file)
    public static ResourceLocation typeRender(int typeID) {
        if (Main.GENERATE_ICONS) {
            return new ResourceLocation(new ResourceData.IconProvider512(typeID), "type_renders/" + typeID + ".jpg");
        } else {
            return new ResourceLocation(
                new ResourceData.Remote("https://images.evetech.net/types/" + typeID + "/render?size=512"),
                "type_renders/" + typeID + ".jpg"
            );
        }
    }

    private static String sharedCacheFile(String resource, HtmlContext context) {
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

    /// ResourceLocation for a file from the Shared Cache (Unknown/Variable file type, usually PNG images)
    public static ResourceLocation fromSharedCache(String resource, HtmlContext context) {
            if (context.sharedCache.containsResource(resource)) {
                return new ResourceLocation(new ResourceData.SharedCache(resource), sharedCacheFile(resource, context));
            } else {
                throw new IllegalStateException("Missing sharedcache entry: " + resource);
            }
    }

    /// ResourceLocation for an iconID-specified icon (Variable size PNG file)
    public static ResourceLocation ofIconID(int iconID, HtmlContext context) {
        if (iconID == 0) throw new IllegalArgumentException("iconID 0 should be patched out!");
        String iconResource = context.sde.getEveIcons().get(iconID);
        if (context.sharedCache.containsResource(iconResource)) {
            return new ResourceLocation(new ResourceData.SharedCache(iconResource), sharedCacheFile(iconResource, context));
        } else {
            throw new IllegalStateException("Missing sharedcache entry: " + iconResource);
        }
    }

    /// ResourceLocation for an EVE faction's logo icon (128x128 PNG file)
    public static ResourceLocation factionLogo(int factionID) {
        String resource = switch (factionID) {
            case 500001 -> "res:/ui/texture/icons/19_128_1.png";
            case 500002 -> "res:/ui/texture/icons/19_128_2.png";
            case 500003 -> "res:/ui/texture/icons/19_128_4.png";
            case 500004 -> "res:/ui/texture/icons/19_128_3.png";
            case 500005 -> "res:/ui/texture/corps/39_128_3.png";
            case 500006 -> "res:/ui/texture/corps/26_128_3.png";
            case 500007 -> "res:/ui/texture/corps/44_128_4.png";
            case 500008 -> "res:/ui/texture/corps/45_128_3.png";
            case 500009 -> "res:/ui/texture/corps/27_128_2.png";
            case 500010 -> "res:/ui/texture/corps/28_128_3.png";
            case 500011 -> "res:/ui/texture/corps/45_128_2.png";
            case 500012 -> "res:/ui/texture/corps/19_128_3.png";
            case 500013 -> "res:/ui/texture/corps/evermore.png";
            case 500014 -> "res:/ui/texture/corps/27_128_4.png";
            case 500015 -> "res:/ui/texture/corps/44_128_3.png";
            case 500016 -> "res:/ui/texture/corps/14_128_1.png";
            case 500017 -> "res:/ui/texture/corps/36_128_2.png";
            case 500018 -> "res:/ui/texture/corps/34_128_2.png";
            case 500019 -> "res:/ui/texture/corps/44_128_2.png";
            case 500020 -> "res:/ui/texture/corps/45_128_1.png";
            case 500024 -> "res:/ui/texture/corps/48_128_1.png";
            case 500025 -> "res:/ui/texture/corps/roguedronesgeneric.png";
            case 500026 -> "res:/ui/texture/corps/triglaviancollective.png";
            case 500027 -> "res:/ui/texture/corps/edencom.png";
            case 500028 -> "res:/ui/texture/corps/air_laboratories_green.png";
            case 500029 -> "res:/ui/texture/corps/deathlesscircle.png";

            default -> throw new IllegalArgumentException("Unknown faction: " + factionID);
        };
        return new ResourceLocation(
            new ResourceData.SharedCache(resource),
            "faction_logos/" + factionID + ".png"
        );
    }

    /// ResourceLocation for the search index data-script file
    public static ResourceLocation searchIndex() {
        return new ResourceLocation(new ResourceData.NoData(), "searchindex.js");
    }

    /// Returns a URI to this resource from the specified context
    public String getURI(HtmlContext context) {
        return getURI(context, false);
    }
    /// Returns a URI to this resource, relative to specified context if {@code isAbsolute} is false, absolute from website root otherwise
    public String getURI(HtmlContext context, boolean isAbsolute) {
        return getURI(context, isAbsolute, null);
    }

    /// Returns a URI to this resource, relative to specified context if {@code isAbsolute} is false, absolute from website root otherwise
    public String getURI(HtmlContext context, boolean isAbsolute, String absolutePrefix) {
        if (dataSource instanceof ResourceData.Remote(String url)) {
            return url;
        } else {
            if (!(dataSource instanceof ResourceData.NoData)) {
                context.addFileDependency(OUTPUT_RES_FOLDER.resolve(destinationPath), dataSource);
            }
            if (absolutePrefix != null) {
                return absolutePrefix + OUTPUT_RES_FOLDER.resolve(destinationPath).toString().replace("\\", "/");
            } else {
                if (isAbsolute) {
                    return OUTPUT_RES_FOLDER.resolve(destinationPath).toString().replace("\\", "/");
                } else {
                    return context.pathTo(OUTPUT_RES_FOLDER.resolve(destinationPath).toString().replace("\\", "/"));
                }
            }
        }
    }

    public interface ResourceData {
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
        record Remote(String url) implements ResourceData {
            // No data for remote resources
            @Override
            public synchronized byte[] getData(DataSources sources) throws IOException {
                throw new UnsupportedOperationException();
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
