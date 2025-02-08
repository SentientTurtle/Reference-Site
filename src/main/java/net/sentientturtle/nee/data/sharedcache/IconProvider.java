package net.sentientturtle.nee.data.sharedcache;

import com.almworks.sqlite4java.SQLiteException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.DataSources;
import net.sentientturtle.nee.data.datatypes.Group;
import net.sentientturtle.nee.data.datatypes.Type;
import net.sentientturtle.nee.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/// Icon generation utility class
public class IconProvider {
    /// Entrypoint for generating Icon Export
    public static void main(String[] args) throws IOException, SQLiteException {
        DataSources sources = Main.initialize(false);

        FileOutputStream types = new FileOutputStream(Main.OUTPUT_DIR.resolve("type_images.zip").toFile());
        generateTypeIconExport(sources, types);
        types.close();

        FileOutputStream renders = new FileOutputStream(Main.OUTPUT_DIR.resolve("render_images.zip").toFile());
        generateTypeRenderExport(sources, renders);
        renders.close();
    }

    public static void generateTypeRenderExport(DataSources data, OutputStream outputStream) {
        ZipOutputStream rendersOut = new ZipOutputStream(outputStream);
        rendersOut.setLevel(Deflater.NO_COMPRESSION);

        Set<Integer> index = Collections.synchronizedSet(new HashSet<>());

        data.sdeData().getTypes()
            .values()
            .parallelStream()
            .filter(type -> IconProvider.hasRender(type, data))
            .forEach(type -> {
                try {
                    byte[] typeRender512 = getTypeRender512(type.typeID, data, false);
                    if (typeRender512 != null) {
                        synchronized (rendersOut) {
                            rendersOut.putNextEntry(new ZipEntry(type.typeID + "_512.jpg"));
                            rendersOut.write(typeRender512);
                            rendersOut.closeEntry();
                        }
                        index.add(type.typeID);
                    }
                } catch (Exception e) {
                    ExceptionUtil.sneakyThrow(e);
                }
            });
        try {
            rendersOut.putNextEntry(new ZipEntry("index.json"));
            new ObjectMapper()
                .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
                .writeValue(rendersOut, index);
            rendersOut.closeEntry();

            rendersOut.finish();
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public static void generateTypeIconExport(DataSources data, OutputStream outputStream) {
        ZipOutputStream iconsOut = new ZipOutputStream(outputStream);
        iconsOut.setLevel(Deflater.NO_COMPRESSION);

        ConcurrentHashMap<Integer, Boolean> index = new ConcurrentHashMap<>();

        data.sdeData().getTypes()
            .values()
            .parallelStream()
            .forEach(type -> {
                try {
                    byte[] typeIcon64 = getTypeIcon64(
                        type.typeID,
                        data,
                        false,
                        false
                    );
                    if (typeIcon64 != null) {
                        synchronized (iconsOut) {
                            iconsOut.putNextEntry(new ZipEntry(type.typeID + "_64.png"));
                            iconsOut.write(typeIcon64);
                            iconsOut.closeEntry();
                        }
                    }

                    if (
                        data.sdeData().getGroups().get(type.groupID).categoryID == 9
                        && !(type.groupID == 1888 || type.groupID == 1889 || type.groupID == 1890 || type.groupID == 4097)
                    ) {
                        typeIcon64 = getTypeIcon64(
                            type.typeID,
                            data,
                            true,
                            false
                        );
                        if (typeIcon64 != null) {
                            synchronized (iconsOut) {
                                iconsOut.putNextEntry(new ZipEntry(type.typeID + "_64_bpc.png"));
                                iconsOut.write(typeIcon64);
                                iconsOut.closeEntry();
                            }
                        }
                        index.put(type.typeID, true);
                    } else {
                        index.put(type.typeID, false);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error in " + type, e);
                }
            });

        try {
            iconsOut.putNextEntry(new ZipEntry("index.json"));
            new ObjectMapper()
                .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
                .writeValue(iconsOut, index);
            iconsOut.closeEntry();

            iconsOut.finish();
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    public static void generateBulkIconExport(DataSources data, OutputStream outputStream) {
        ZipOutputStream iconsOut = new ZipOutputStream(outputStream);
        iconsOut.setLevel(Deflater.NO_COMPRESSION);

        Set<Integer> index = Collections.synchronizedSet(new HashSet<>());

        for (Map.Entry<Integer, String> entry : data.sdeData().getEveIcons().entrySet()) {
            Integer iconID = entry.getKey();
            String iconResource = entry.getValue();
            try {
                if (!data.sharedCache().containsResource(iconResource)) continue;

                byte[] iconPNG;
                if (iconResource.endsWith(".png")) {
                    iconPNG = data.sharedCache().getBytes(iconResource);
                } else {
                    Process process = new ProcessBuilder(
                        "magick",
                        data.sharedCache().getPath(iconResource).toString(),
                        "png:-"
                    ).start();
                    iconPNG = process.getInputStream().readAllBytes();

                    // This shouldn't block as we've already read all bytes
                    if (process.waitFor() != 0) {
                        System.err.println(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
                        throw new IllegalStateException("An imagemagick error occurred!");
                    }
                }

                iconsOut.putNextEntry(new ZipEntry(iconID + ".png"));
                iconsOut.write(iconPNG);
                iconsOut.closeEntry();

                index.add(iconID);
            } catch (Exception e) {
                throw new RuntimeException("Error in " + iconID, e);
            }
        }
        try {
            iconsOut.putNextEntry(new ZipEntry("index.json"));
            new ObjectMapper()
                .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
                .writeValue(iconsOut, index);
            iconsOut.closeEntry();

            iconsOut.finish();
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    // ConcurrentMap's support for parallel writes is required for icon generation parallelism to work!
    private static final AtomicBoolean CACHE_INVALID = new AtomicBoolean(false);
    private static final ConcurrentHashMap<String, byte[]> CACHED_ICONS = new ConcurrentHashMap<>();

    public static void readIconCache() throws IOException {
        if (Files.exists(Main.ICON_CACHE_FILE)) {
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(Main.ICON_CACHE_FILE.toFile()))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    CACHED_ICONS.put(entry.getName(), zipInputStream.readAllBytes());
                }
            } catch (FileNotFoundException e) { // shouldn't happen, rethrow as runtime exception
                ExceptionUtil.sneakyThrow(e);
            } catch (IOException e) { // Rethrow to signal something unexpected happened
                ExceptionUtil.sneakyThrow(e);
            }
        }
    }

    public static void writeIconCache() {
        if (!CACHE_INVALID.get()) return;

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(Main.ICON_CACHE_FILE.toFile()))) {
            zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
            CACHED_ICONS.forEach((name, bytes) -> {
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(name));
                    zipOutputStream.write(bytes);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    ExceptionUtil.sneakyThrow(e);
                }
            });
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    private static Path techOverlayPath(int metaGroup, DataSources dataSources, boolean useOld) {
        if (metaGroup == 1) {
            return null;
        } else if (!useOld) {
            return dataSources.sharedCache().getPath(
                switch (metaGroup) {
                    case 2 -> "res:/ui/texture/icons/73_16_242.png";
                    case 3 -> "res:/ui/texture/icons/73_16_245.png";
                    case 4 -> "res:/ui/texture/icons/73_16_246.png";
                    case 5 -> "res:/ui/texture/icons/73_16_248.png";
                    case 6 -> "res:/ui/texture/icons/73_16_247.png";
                    case 14 -> "res:/ui/texture/icons/73_16_243.png";
                    case 15 -> "res:/ui/texture/icons/itemoverlay/abyssal.png";
                    case 17 -> "res:/ui/texture/icons/itemoverlay/nes.png";
                    case 19 -> "res:/ui/texture/icons/itemoverlay/timelimited.png";
                    case 52 -> "res:/ui/texture/shared/structureoverlayfaction.png";
                    case 53 -> "res:/ui/texture/shared/structureoverlayt2.png";
                    case 54 -> "res:/ui/texture/shared/structureoverlay.png";
                    default -> throw new IllegalStateException("Unknown metaGroup " + metaGroup);
                }
            );
        } else {
            return switch (metaGroup) {
                case 2 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Tech 2.png");
                case 3 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Storyline.png");
                case 4 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Faction.png");
                case 5 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Officer.png");
                case 6 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Deadspace.png");
                case 14 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Tech 3.png");
                case 15 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Abyssal.png");
                case 17 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/NES.png");
                case 19 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Time Limited.png");
                case 52 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Structure Faction.png");
                case 53 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Structure Tech 2.png");
                case 54 -> Main.RES_FOLDER.resolve("EVE/type_overlays_old/Structure Tech 1.png");
                default -> throw new IllegalStateException("Unknown metaGroup " + metaGroup);
            };
        }
    }

    private static boolean useIconInsteadOfGraphic(int groupID) {
        return groupID == 4168 || groupID == 711
               || groupID == 12 || groupID == 340 || groupID == 448 || groupID == 649;
    }

    private static final Semaphore imageServiceSemaphore = new Semaphore(1);

    public static @Nullable byte[] getTypeIcon64(int typeID, DataSources dataSources, boolean isBPC, boolean useOldOverlay) throws IOException {
        Type type = dataSources.sdeData().getTypes().get(typeID);
        Group group = dataSources.sdeData().getGroups().get(type.groupID);
        int metaGroup = dataSources.sdeData().getMetaTypes().getOrDefault(type.typeID, 1);

        String cacheKey = null;
        ProcessBuilder imageMagickCall = null;
        if (group.categoryID == 9 || group.categoryID == 34) {    // Blueprint
            String backgroundResource;
            String overlayResource;

            if (group.categoryID == 34) {   // Relics
                backgroundResource = "res:/ui/texture/icons/relic.png";
                overlayResource = "res:/ui/texture/icons/relic_overlay.png";

            } else if (group.groupID == 1888 || group.groupID == 1889 || group.groupID == 1890 || group.groupID == 4097) { // Reactions
                backgroundResource = "res:/ui/texture/icons/reaction.png";
                overlayResource = "res:/ui/texture/icons/bpo_overlay.png";
            } else { // Blueprints
                if (isBPC) {
                    backgroundResource = "res:/ui/texture/icons/bpc.png";
                    overlayResource = "res:/ui/texture/icons/bpc_overlay.png";
                } else {
                    backgroundResource = "res:/ui/texture/icons/bpo.png";
                    overlayResource = "res:/ui/texture/icons/bpo_overlay.png";
                }
            }

            FSDData.Graphic graphic = dataSources.fsdData().graphics.get(type.graphicID != null ? type.graphicID : 0);
            if (imageMagickCall == null && graphic != null && graphic.iconInfo() != null && !useIconInsteadOfGraphic(type.groupID)) {
                String graphicResource;
                if (graphic.iconInfo().folder().endsWith("/")) {
                    graphicResource = graphic.iconInfo().folder() + type.graphicID + (isBPC ? "_64_bpc.png" : "_64_bp.png");
                } else {
                    graphicResource = graphic.iconInfo().folder() + "/" + type.graphicID + (isBPC ? "_64_bpc.png" : "_64_bp.png");
                }

                if (dataSources.sharedCache().containsResource(graphicResource)) {
                    Path techOverlay = techOverlayPath(metaGroup, dataSources, useOldOverlay);
                    if (techOverlay == null) {
                        if (graphicResource.endsWith(".png")) {
                            // No need to cache
                            return dataSources.sharedCache().getBytes(graphicResource);
                        } else {
                            // Some resources are not PNGs
                            cacheKey = dataSources.sharedCache().getResourceHash(graphicResource);
                            imageMagickCall = new ProcessBuilder("magick", dataSources.sharedCache().getPath(graphicResource).toString(), "png:-");
                        }
                    } else {
                        cacheKey = metaGroup + ";" + useOldOverlay + ";" + dataSources.sharedCache().getResourceHash(graphicResource);
                        imageMagickCall = new ProcessBuilder(
                            "magick",
                            dataSources.sharedCache().getPath(graphicResource).toString(),
                            "-resize", "64x64",
                            "(", techOverlay.toString(), "-resize", "16x16!", ")",
                            "-composite",
                            "png:-"
                        );
                    }
                } else if (type.iconID == null) {
                    System.out.println("\tMissing BP graphic, falling back to image server!: " + type);
                    String bpType = isBPC ? "bpc" : "bp";
                    // use image-service specific caching so these will be invalid should the missing graphics return
                    cacheKey = "IMAGESERV;" + bpType + ";" + type.typeID;
                        return CACHED_ICONS.computeIfAbsent(cacheKey, _ -> {
                            System.out.println("\t\tconnecting to Image Service...");
                            try {
                                imageServiceSemaphore.acquire();
                            } catch (InterruptedException e) {
                                ExceptionUtil.sneakyThrow(new IOException("Interrupted while waiting on image service semaphore!", e));
                            }
                            try (InputStream inputStream = new URI("https://images.evetech.net/types/" + type.typeID + "/" + bpType).toURL().openStream()) {
                                return inputStream.readAllBytes();
                            } catch (IOException | URISyntaxException e) {
                                return ExceptionUtil.sneakyThrow(e);
                            } finally {
                                imageServiceSemaphore.release();
                            }
                        });
                }
            }

            if (imageMagickCall == null && type.iconID != null) {
                String iconResource = dataSources.sdeData().getEveIcons().get(type.iconID);

                Path techOverlay = techOverlayPath(metaGroup, dataSources, useOldOverlay);
                if (techOverlay != null) {
                    cacheKey = metaGroup
                               + ";" + useOldOverlay
                               + ";" + dataSources.sharedCache().getResourceHash(backgroundResource)
                               + ";" + dataSources.sharedCache().getResourceHash(iconResource)
                               + ";" + dataSources.sharedCache().getResourceHash(overlayResource);
                    imageMagickCall = new ProcessBuilder(
                        "magick",
                        dataSources.sharedCache().getPath(backgroundResource).toString(),
                        dataSources.sharedCache().getPath(iconResource).toString(),
                        "-resize", "64x64",
                        "-composite",
                        "-compose", "plus",
                        dataSources.sharedCache().getPath(overlayResource).toString(),
                        "-composite",
                        "-compose", "over",
                        "(", techOverlay.toString(), "-resize", "16x16!", ")",
                        "-composite",
                        "png:-"
                    );
                } else {
                    cacheKey = metaGroup
                               + ";" + useOldOverlay
                               + ";" + dataSources.sharedCache().getResourceHash(backgroundResource)
                               + ";" + dataSources.sharedCache().getResourceHash(iconResource)
                               + ";" + dataSources.sharedCache().getResourceHash(overlayResource);
                    imageMagickCall = new ProcessBuilder(
                        "magick",
                        dataSources.sharedCache().getPath(backgroundResource).toString(),
                        dataSources.sharedCache().getPath(iconResource).toString(),
                        "-resize", "64x64",
                        "-composite",
                        "-compose", "plus",
                        dataSources.sharedCache().getPath(overlayResource).toString(),
                        "-composite",
                        "png:-"
                    );
                }
            }

            if (imageMagickCall == null) {
                return null;
            }
        } else {    // Regular item
            String iconResource = null;
            FSDData.Graphic graphic = dataSources.fsdData().graphics.get(type.graphicID != null ? type.graphicID : 0);
            if (graphic != null && graphic.iconInfo() != null && !useIconInsteadOfGraphic(type.groupID)) {
                if (graphic.iconInfo().folder().endsWith("/")) {
                    iconResource = graphic.iconInfo().folder() + type.graphicID + "_64.png";
                } else {
                    iconResource = graphic.iconInfo().folder() + "/" + type.graphicID + "_64.png";
                }
                if (!dataSources.sharedCache().containsResource(iconResource) && type.iconID != null) {
                    iconResource = dataSources.sdeData().getEveIcons().get(type.iconID);
                }
            } else if (type.iconID != null) {
                iconResource = dataSources.sdeData().getEveIcons().get(type.iconID);
            }

            if (iconResource == null || !dataSources.sharedCache().containsResource(iconResource)) {
                return null;
            }
            Path techOverlay = techOverlayPath(metaGroup, dataSources, useOldOverlay);
            if (techOverlay != null) {
                cacheKey = metaGroup + ";" + useOldOverlay + ";" + dataSources.sharedCache().getResourceHash(iconResource);
                imageMagickCall = new ProcessBuilder(
                    "magick",
                    dataSources.sharedCache().getPath(iconResource).toString(),
                    "-resize", "64x64",
                    "(", techOverlay.toString(), "-resize", "16x16!", ")",
                    "-composite",
                    "png:-"
                );
            } else {
                if (iconResource.endsWith(".png")) {
                    // No need to cache
                    return dataSources.sharedCache().getBytes(iconResource);
                } else {
                    cacheKey = dataSources.sharedCache().getResourceHash(iconResource);
                    imageMagickCall = new ProcessBuilder("magick", dataSources.sharedCache().getPath(iconResource).toString(), "png:-");
                }
            }
        }

        assert cacheKey != null;
        ProcessBuilder finalImageMagickCall = imageMagickCall;
        return CACHED_ICONS.computeIfAbsent(
            cacheKey,
            _ -> {
                CACHE_INVALID.set(true);
                try {
                    Process process = finalImageMagickCall.start();
                    byte[] bytes = process.getInputStream().readAllBytes();

                    // This shouldn't block as we've already read all bytes
                    int statusCode = process.waitFor();
                    if (statusCode != 0) {
                        System.err.println(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
                        throw new IllegalStateException("An imagemagick error occurred!");
                    }

                    return bytes;
                } catch (InterruptedException | IOException e) {
                    return ExceptionUtil.sneakyThrow(e);
                }
            }
        );
    }

    public static boolean hasRender(Type type, DataSources dataSources) {
        int categoryID = dataSources.sdeData().getGroups().get(type.groupID).categoryID;
        if (categoryID == 6         // Ship
            || categoryID == 18     // Drone
            || categoryID == 22     // Deployable
            || categoryID == 24     // Starbase
            || categoryID == 65     // Structure
            || categoryID == 87     // Fighter
        ) {
            String renderResource = null;
            FSDData.Graphic graphic = dataSources.fsdData().graphics.get(type.graphicID != null ? type.graphicID : 0);
            if (graphic != null && graphic.iconInfo() != null) {
                if (graphic.iconInfo().folder().endsWith("/")) {
                    renderResource = graphic.iconInfo().folder() + type.graphicID + "_512.jpg";
                } else {
                    renderResource = graphic.iconInfo().folder() + "/" + type.graphicID + "_512.jpg";
                }
            }

            return (renderResource != null && dataSources.sharedCache().containsResource(renderResource));
        } else {
            return false;
        }
    }


    public static @Nullable byte[] getTypeRender512(int typeID, DataSources dataSources, boolean convertToPNG) throws IOException {
        Type type = dataSources.sdeData().getTypes().get(typeID);

        String renderResource;
        FSDData.Graphic graphic = dataSources.fsdData().graphics.get(type.graphicID != null ? type.graphicID : 0);
        if (graphic != null && graphic.iconInfo() != null) {
            if (graphic.iconInfo().folder().endsWith("/")) {
                renderResource = graphic.iconInfo().folder() + type.graphicID + "_512.jpg";
            } else {
                renderResource = graphic.iconInfo().folder() + "/" + type.graphicID + "_512.jpg";
            }
        } else {
            renderResource = null;
        }

        if (renderResource == null || !dataSources.sharedCache().containsResource(renderResource)) {
            return null;
        } else if (!convertToPNG) {
            return dataSources.sharedCache().getBytes(renderResource);
        } else {
            String cacheKey = dataSources.sharedCache().getResourceHash(renderResource);
            return CACHED_ICONS.computeIfAbsent(
                cacheKey,
                _ -> {
                    try {
                        Process process = new ProcessBuilder(
                            "magick",
                            dataSources.sharedCache().getPath(renderResource).toString(),
                            "png:-"
                        ).start();
                        byte[] bytes = process.getInputStream().readAllBytes();

                        // This shouldn't block as we've already read all bytes
                        int statusCode = process.waitFor();

                        if (statusCode != 0) {
                            System.err.println(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
                            throw new IllegalStateException("An imagemagick error occurred!");
                        }

                        return bytes;
                    } catch (InterruptedException | IOException e) {
                        return ExceptionUtil.sneakyThrow(e);
                    }
                }
            );
        }
    }
}
