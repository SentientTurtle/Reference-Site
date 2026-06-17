package net.sentientturtle.nee.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sentientturtle.html.HTML;
import net.sentientturtle.html.HeadEntries;
import net.sentientturtle.html.IndexSetting;
import net.sentientturtle.html.RenderingException;
import net.sentientturtle.html.context.HtmlContext;
import net.sentientturtle.nee.data.Resource;
import net.sentientturtle.nee.data.datatypes.*;
import net.sentientturtle.nee.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static net.sentientturtle.html.HTML.*;

/// Page for the dynamic Map
public class DynamicMapPage extends Page {
    @Override
    public String name() {
        return "Map";
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public String filename() {
        return "map";
    }

    @Override
    public PageKind getPageKind() {
        return PageKind.STATIC;
    }

    @Override
    public IndexSetting getIndexSetting() {
        return IndexSetting.INDEX;
    }

    @Override
    public @Nullable Resource getIcon(HtmlContext context) {
        return Resource.ofIconID(2355, context);
    }

    @Override
    protected HeadEntries headEntries(HtmlContext context) {
        // Add a file-dependency on three.core
        Resource.file("three/three.core.min.js").getURI(context);
        return super.headEntries(context).append(
            HTML.META().attribute("name", "description").attribute("content", "A map of the EVE Online game world"),
            SCRIPT_IMPORTMAP(new ImportMap(Map.of(
                "three", "./" + Resource.file("three/three.module.min.js").getURI(context),
                "CSS2D", "./" + Resource.file("three/CSS2DRenderer.js").getURI(context),
                "Orbit", "./" + Resource.file("three/OrbitControls.js").getURI(context),
                "Stats", "./" + Resource.file("three/stats.module.js").getURI(context)
            ))),
            SCRIPT_EXTERNAL(Resource.file("mapscript.js"))
        );
    }

    @Override
    protected HTML getContent(HtmlContext context) {
        // Map canvas is contained in an 'absolute' element to stop the canvas increasing the size of the containing elements
        // Canvas dynamically follows the size of 'map_spacer', but this is done from JS after a reflow, requiring the reflow to happen first.
        // Noscript: Display only the iframe
        return RAW("""
            <noscript><style>#map_spacer { display: none !important; } .map_controls { display: none !important; } #map_frame { width: 100%; height: 100rem; }</style></noscript>
            <div id="map_spacer" class="colour_theme_minor"><div id="map_container"></div></div>
            <div class="map_controls">
                <div class="map_controls_header eve_clip_top colour_theme_border_bg"><div class="eve_clip_top colour_theme_bg">Controls</div></div>
                <div class="map_controls_entry">
                    <label for="map_select">Map:</label>
                    <select name="mapselect" id="map_select" disabled>
                      <option value="nec">New Eden Cluster</option>
                      <option value="pochven">Pochven</option>
                      <option value="anoikis">Anoikis</option>
                    </select>
                </div>
                <div class="map_controls_entry">
                    <input type="checkbox" name="showjumps" id="map_show_jumps" checked disabled>
                    <label for="map_show_jumps">Show connections</label>
                </div>
                <fieldset class="map_colours">
                    <legend>Colours</legend>
                    <div class="map_controls_entry">
                        <input type="radio" name="map_colour" value="security" id="map_colour_sec" checked><label for="map_colour_sec">Security</label>
                    </div>
                    <div class="map_controls_entry">
                        <input type="radio" name="map_colour" value="jumps" id="map_colour_jumps" disabled><label for="map_colour_jumps">Jumps</label>
                    </div>
                    <div class="map_controls_entry">
                        <input type="radio" name="map_colour" value="ship_kills" id="map_colour_ship_kills" disabled><label for="map_colour_ship_kills">Ship Kills</label>
                    </div>
                    <div class="map_controls_entry">
                        <input type="radio" name="map_colour" value="npc_kills" id="map_colour_npc_kills" disabled><label for="map_colour_npc_kills">NPC Kills</label>
                    </div>
                </fieldset>
                <button id="map_reset_camera" disabled>Reset Camera</button>
            </div>
            <iframe id="map_frame" src="../map/-1.html"></iframe>
            """);
    }

    @Override
    protected @Nullable String getCSS(HtmlContext context) {
        return """
            #map_spacer {
                flex-grow: 1;
                height: calc(100vh - 5rem);
                position: relative;
            }
        
            @media (max-width: 70rem) {
                #map_spacer {
                    height: calc(80vh - 5rem);
                    width: 100%;
                }
            
                .map_controls {
                    width: 100%;
                }
            
                #map_frame {
                    width: 100%;
                    height: 40rem !important;
                }
            }
            
            #map_container {
                 height: 100%;
                 width: 100%;
                 position: absolute;
            }
            
            #map_frame {
                height: 100%;
                min-width: min(20rem, 100%);
                border: none;
            }
            
            .map_controls {
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }
            
            .map_colours {
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }
            
            .map_controls_header {
                padding: 1px;
            }
            
            .map_controls_header > div {
                padding-block: 0.25rem;
                padding-inline: 0.5rem;
                text-align: center;
            }
            
            .map_controls_entry {
                display: flex;
                align-items: center;
            }
            
            .map_label_outer {
                display: flex;
                border-radius: 2rem;
                border: 1px solid #BBBBBB;
                translate: -50% -1rem;
                background-color: #000000AA;
                color: #BBBBBB;
            }
            
            .map_label_name {
                padding: 0.5rem;
                border-top-left-radius: 2rem;
                border-bottom-left-radius: 2rem;
            }
            
            .map_label_security {
                padding: 0.5rem;
                border-top-right-radius: 2rem;
                border-bottom-right-radius: 2rem;
                border-left: 1px solid #BBBBBB;
            }
            """;
    }

    private static final double cameraFoV = Math.toRadians(35);
    @Override
    public void renderTo(HtmlContext context) throws RenderingException {
        super.renderTo(context);

        HashMap<Integer, Set<Integer>> dedupJumps = new HashMap<>();
        context.sde.getOutJumps().forEach((from, toSet) -> {
            for (Integer to : toSet) {
                if (!dedupJumps.containsKey(to)) {
                    dedupJumps.computeIfAbsent(from, _ -> new HashSet<>()).add(to);
                }
            }
        });
        record RSystem(String solarSystemName, double x, double y, double z, double security, @Nullable String whclass) {}

        HashMap<String, RSystem> NEC = new HashMap<>();
        ArrayList<int[]> NEC_jumps = new ArrayList<>();
        HashMap<String, RSystem> pochven = new HashMap<>();
        ArrayList<int[]> pochvenJumps = new ArrayList<>();

        HashMap<String, RSystem> anoikis = new HashMap<>();

        for (SolarSystem s : context.sde.getSolarSystems().values()) {
            if ((s.regionID >= 12000001 && s.regionID <= 12000005) || (s.regionID >= 14000001 && s.regionID <= 14000005)) continue;

            // We only create RSystem instances after checking regionIDs to avoid an NPE; Ensure the abyssal filament systems without suns are excluded
            if (Cluster.KSPACE_REGIONS.contains(s.regionID)) {
                NEC.put(String.valueOf(s.solarSystemID), new RSystem(s.solarSystemName, s.x, s.y, s.z, s.security, getWhClassName(s.wormholeClassID)));
                dedupJumps.getOrDefault(s.solarSystemID, Set.of())
                    .stream()
                    .map(to -> new int[] { s.solarSystemID, to})
                    .forEach(NEC_jumps::add);
            } else if (Cluster.WSPACE_REGIONS.contains(s.regionID)) {
                anoikis.put(
                    String.valueOf(s.solarSystemID),
                    new RSystem(
                        s.solarSystemName,
                        s.x - 7.70416391716947e+18,
                        s.y, // y intentionally not offset
                        s.z - (-9.51905586204134e+18),
                        s.security,
                        getWhClassName(s.wormholeClassID)
                    ));
            } else if (s.regionID == 10000070) {
                pochven.put(String.valueOf(s.solarSystemID), new RSystem(s.solarSystemName, s.x, s.y, s.z, s.security, getWhClassName(s.wormholeClassID)));
                dedupJumps.getOrDefault(s.solarSystemID, Set.of())
                    .stream()
                    .map(to -> new int[] { s.solarSystemID, to})
                    .forEach(pochvenJumps::add);
            }
        }

        record Selectable(@Nullable String name, double x, double y, double z, double distance, @Nullable Double security, @Nullable String whclass, @Nullable List<String> systems) {}
        HashMap<Integer, Selectable> selectables = new HashMap<>(context.sde.getRegions().size() + context.sde.getConstellations().size() + 2);

        selectables.put(-1, new Selectable(null, 0, 0, 0, 2.0E18, null, null,null));
        selectables.put(-2, new Selectable( null, 0, 0, 0, 2.0E18, null, null,null));

        for (Region region : context.sde.getRegions().values()) {
            double size = 0.0 /* TODO */;
            double distance = Math.max(1.25 * (size / 2.0) / Math.tan(cameraFoV / 2.0), 5.0E17);
            List<String> systemIDs = new ArrayList<>();
            double security = 0.0;
            for (Constellation constellation : context.sde.getRegionConstellationMap().get(region.regionID)) {
                for (SolarSystem solarSystem : context.sde.getConstellationSolarSystemMap().get(constellation.constellationID)) {
                    systemIDs.add(String.valueOf(solarSystem.getID()));
                    security += solarSystem.security;
                }
            }
            security /= systemIDs.size();

            if (Cluster.WSPACE_REGIONS.contains(region.regionID)) {
                selectables.put(region.regionID, new Selectable(
                    region.regionName,
                    region.x - 7.70416391716947e+18,
                    region.y,
                    region.z - -9.51905586204134e+18,
                    distance,
                    security,
                    getWhClassName(region.wormholeClassID),
                    systemIDs
                ));
            } else {
                selectables.put(
                    region.regionID,
                    new Selectable(
                        region.regionName,
                        region.x, region.y, region.z,
                        distance,
                        security,
                        getWhClassName(region.wormholeClassID),
                        systemIDs
                    )
                );
            }
        }

        for (Constellation constellation : context.sde.getConstellations().values()) {
            double size = 0.0 /* TODO */;
            double distance = Math.max(1.25 * (size / 2.0) / Math.tan(cameraFoV / 2.0), 5.0E17);
            List<String> systemIDs = new ArrayList<>();
            double security = 0.0;

            for (SolarSystem solarSystem : context.sde.getConstellationSolarSystemMap().get(constellation.constellationID)) {
                systemIDs.add(String.valueOf(solarSystem.getID()));
                security += solarSystem.security;
            }
            security /= systemIDs.size();

            if (Cluster.WSPACE_REGIONS.contains(constellation.regionID)) {
                selectables.put(
                    constellation.constellationID,
                    new Selectable(
                        constellation.constellationName,
                        constellation.x - 7.70416391716947e+18,
                        constellation.y,
                        constellation.z - -9.51905586204134e+18,
                        distance,
                        security,
                        getWhClassName(constellation.wormholeClassID),
                        systemIDs
                    )
                );
            } else {
                selectables.put(
                    constellation.constellationID,
                    new Selectable(
                        constellation.constellationName,
                        constellation.x, constellation.y, constellation.z,
                        distance,
                        security,
                        getWhClassName(constellation.wormholeClassID),
                        systemIDs
                    )
                );
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] NEC_bytes = objectMapper.writeValueAsBytes(NEC);
            byte[] NEC_jumps_bytes = objectMapper.writeValueAsBytes(NEC_jumps);
            byte[] anoikisBytes = objectMapper.writeValueAsBytes(anoikis);
            byte[] pochvenBytes = objectMapper.writeValueAsBytes(pochven);
            byte[] pochvenJumpsBytes = objectMapper.writeValueAsBytes(pochvenJumps);
            byte[] selectionBytes = objectMapper.writeValueAsBytes(selectables);

            byte[] systemJumpsBytes;
            try (InputStream inputStream = new URI("https://esi.evetech.net/latest/universe/system_jumps/?datasource=tranquility").toURL().openStream()) {
                systemJumpsBytes = inputStream.readAllBytes();
            }
            byte[] systemKillsBytes;
            try (InputStream inputStream = new URI("https://esi.evetech.net/latest/universe/system_kills/?datasource=tranquility").toURL().openStream()) {
                systemKillsBytes = inputStream.readAllBytes();
            }

            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/NEC.json"), _ -> NEC_bytes);
            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/NEC_jumps.json"), _ -> NEC_jumps_bytes);
            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/anoikis.json"), _ -> anoikisBytes);
            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/pochven.json"), _ -> pochvenBytes);
            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/pochven_jumps.json"), _ -> pochvenJumpsBytes);
            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/selection.json"), _ -> selectionBytes);
            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/system_jumps.json"), _ -> systemJumpsBytes);
            context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/system_kills.json"), _ -> systemKillsBytes);

            String[] bracketNames = {"sun.png", "planet.png", "moon.png", "asteroidbelt.png"};
            for (String bracketName : bracketNames) {
                context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/brackets/" + bracketName), Resource.fromSharedCache("res:/ui/texture/shared/brackets/" + bracketName, context).getData());
            }

            record CelestialJson(double x, double y, double z, String name, String bracket){}
            for (SolarSystem solarSystem : context.sde.getSolarSystems().values()) {
                Set<Celestial> celestials = context.sde.getSystemCelestials().getOrDefault(solarSystem.solarSystemID, Set.of());
                ArrayList<CelestialJson> celestialPositions = new ArrayList<>(celestials.size() + 1);

                if (solarSystem.sunTypeID != null) {
                    celestialPositions.add(new CelestialJson(0.0, 0.0, 0.0, solarSystem.solarSystemName + " - Sun", "sun"));
                }

                for (Celestial celestial : celestials) {
                    if (celestial.position != null) {
                        // TODO: Remove (Integer) cast once primitive patterns leave preview.
                        String bracketName = switch ((Integer) context.sde.getTypes().get(celestial.typeID).groupID) {
                            case 6, 995 -> "sun";
                            case 7 -> "planet";
                            case 8 -> "moon";
                            case 9 -> "asteroidbelt";
                            case Integer group -> throw new IllegalStateException("Unknown celestial group: " + group);
                        };

                        celestialPositions.add(new CelestialJson(celestial.position.x(), celestial.position.y(), celestial.position.z(), celestial.itemName, bracketName));
                    }
                }
                byte[] celestialBytes = objectMapper.writeValueAsBytes(celestialPositions);
                context.addFileDependency(Resource.OUTPUT_RES_FOLDER.resolve("map/celestials/" + solarSystem.solarSystemID + ".json"), _ -> celestialBytes);
            }
        } catch (IOException | URISyntaxException e) {
            ExceptionUtil.sneakyThrow(e);
        }
    }

    private static String getWhClassName(@Nullable Integer wormholeClassID) {
        return switch (wormholeClassID) {
            case 1 -> "C1";
            case 2 -> "C2";
            case 3 -> "C3";
            case 4 -> "C4";
            case 5 -> "C5";
            case 6 -> "C6";
            case 7, 8, 9 -> null;   // HS, LS, NS
            case 10, 11 -> null;    // Jove space
            case 12 -> null;        // Thera
            case 13 -> "C13";
            case 14 -> "'Sentinel'";
            case 15 -> "'Barbican'";
            case 16 -> "'Vidette'";
            case 17 -> "'Conflux'";
            case 18 -> "'Redoubt'";
            case 19, 20, 21, 22, 23 -> null;    // ADR regions
            case 25 -> null;        // Pochven
            case null, default -> null;
        };
    }
}
