package net.sentientturtle.nee.data;

import net.sentientturtle.nee.data.sharedcache.IconProvider;
import net.sentientturtle.nee.page.PageKind;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

///  3rd party development resources
public class DevResources {
    public record ResourceGroup(
        String name,
        String description,
        List<DevResource> resources
    ) {}

    public record DevResource(
        String name,
        Path path,
        BiConsumer<DataSources, OutputStream> data
    ) {}

    private static final Path OUTPUT_DEV_RES_FOLDER = Path.of(PageKind.DEV_RESOURCE.toString()); // Destination resource folder relative to output

    public static List<ResourceGroup> getResources(DataSources data) {
        return List.of(new ResourceGroup(
            "Item type icons",
            "Drop-in replacement for the official Image-Export-Collection, updated to recent game versions & with correct tech tiers",
            List.of(
                new DevResource("Item type icons (64x64)", OUTPUT_DEV_RES_FOLDER.resolve("type_images - v" + data.gameVersion() + ".zip"), IconProvider::generateTypeIconExport),
                new DevResource("Item type renders (512x512)", OUTPUT_DEV_RES_FOLDER.resolve("type_renders - v" + data.gameVersion() + ".zip"), IconProvider::generateTypeRenderExport)
            )
        ));
    }
}
