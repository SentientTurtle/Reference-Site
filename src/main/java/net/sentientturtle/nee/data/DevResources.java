package net.sentientturtle.nee.data;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

///  3rd party development resources
public class DevResources {
    public record ResourceGroup(
        String name,
        String description,
        List<DevResource> resources
    ) {}

    public record DevResource(
        String name,
        ResourceLocation file,
        @Nullable ResourceLocation README
    ) {}

    private static final Path OUTPUT_DEV_RES_FOLDER = Path.of("dev_resource"); // Destination resource folder relative to output

    public static List<ResourceGroup> getResources(DataSources data) {
        return List.of(new ResourceGroup(
            "Icons",
            "The official Image Service is unhappy, and the Image Export Collection has been deprecated. Below are some of the drop-in replacements for this.",
            List.of(
                new DevResource("Icon generation tool", ResourceLocation.remoteURL("https://github.com/SentientTurtle/EVE-3rd-party-dev-tools"), null),
                new DevResource("De-duplicated icon archive", ResourceLocation.localPath(OUTPUT_DEV_RES_FOLDER.resolve("icons_dedup.zip")), ResourceLocation.file("devrsc/SERVICE_BUNDLE_README.txt")),
                new DevResource("'Image Export Collection' compatible archive", ResourceLocation.localPath(OUTPUT_DEV_RES_FOLDER.resolve("IEC_compat.zip")), ResourceLocation.file("devrsc/IEC_README.txt")),
                new DevResource("Images checksum", ResourceLocation.localPath(OUTPUT_DEV_RES_FOLDER.resolve("icon_checksum.txt")), null)
            )
        ));
    }
}
