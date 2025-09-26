package net.sentientturtle.nee.data.sde;

import net.sentientturtle.nee.Main;
import net.sentientturtle.nee.data.datatypes.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CCPSDEData extends SDEData {
    private final Map<Integer, Category> categories;
    private final Map<Integer, Group> groups;
    private final Map<Integer, Attribute> attributes;
    private final Map<Integer, Effect> effects;
    private final Map<Integer, Type> types;
    private final Map<Integer, Map<Integer, Double>> typeAttributes;
    private final Map<Integer, Set<Integer>> typeEffects;
    private final Map<Integer, TypeTraits> typeTraits;
    private final Map<Integer, String> eveIcons;
    private final Map<Integer, EnumMap<IndustryActivityType, IndustryActivity>> bpActivities;
    private final Map<Integer, Map<Integer, Integer>> reprocessingMaterials;
    private final Map<Integer, PlanetSchematic> planetSchematics;
    private final Map<Integer, MetaGroup> metaGroups;
    private final Map<Integer, Set<Integer>> variants;
    private final Map<Integer, Integer> metaTypes;
    private final Map<Integer, SolarSystem> solarSystems;
    private final Map<Integer, Constellation> constellations;
    private final Map<Integer, Region> regions;
    private final Map<Integer, Set<Integer>> outJumps;
    private final Map<Integer, Set<Integer>> inJumps;
    private final Map<Integer, Set<Celestial>> systemCelestials;
    private final Map<Integer, EnumSet<Station.Service>> operationServices;
    private final Map<Integer, Set<Station>> stations;
    private final Map<Integer, Faction> factions;
    private final Map<Integer, MarketGroup> marketGroups;
    private final Map<Integer, WarfareBuff> warfareBuffs;
    private final Map<Integer, DynamicAttributes> dynamicAttributes;
    private final Map<Integer, String> graphicFolders;

    public static void main(String[] args) throws IOException {
        Main.RES_FOLDER = Path.of("./rsc");

        System.out.println("Loading SDE...");
        long sde_start = System.nanoTime();
//        SDEData sdeData = new CCPSDEData(new YAMLSDEReader(Path.of("./rsc/sde.zip")), false);
        SDEData sdeData = new CCPSDEData(new JSONLSDEReader(Path.of("./rsc/sde_jsonl.zip")), false);
        long sdeDuration = System.nanoTime() - sde_start;
        System.out.println("\tSDE loaded! (" + (TimeUnit.NANOSECONDS.toMillis(sdeDuration) / 1000.0) +")");
    }

    public CCPSDEData(SDEReader reader, boolean patch) throws IOException {
        this.categories = this.produceMap();
        reader.readCategories((categoryID, sdeCategory) -> {
            this.categories.put(
                categoryID,
                new Category(
                    categoryID,
                    Objects.requireNonNull(sdeCategory.name().en()),
                    sdeCategory.iconID(),
                    sdeCategory.published()
                ));
        });

        this.groups = this.produceMap();
        reader.readGroups((groupID, sdeGroup) -> {
            this.groups.put(
                groupID,
                new Group(
                    groupID,
                    sdeGroup.categoryID(),
                    Objects.requireNonNull(sdeGroup.name().en()),
                    sdeGroup.iconID(),
                    sdeGroup.published()
                )
            );
        });

        this.types = this.produceMap();
        this.typeTraits = this.produceMap();
        this.metaTypes = this.produceMap();
        HashMap<Integer, Set<Integer>> typeVariants = new HashMap<>();

        reader.readTypes((typeID, sdeType) -> {
            this.types.put(
                typeID,
                new Type(
                    typeID,
                    sdeType.groupID(),
                    Objects.requireNonNull(sdeType.name().en()),
                    sdeType.description() != null ? sdeType.description().en() : null,
                    sdeType.mass() == null ? 0.0 : sdeType.mass(),
                    sdeType.volume() == null ? 0.0 : sdeType.volume(),
                    sdeType.capacity() == null ? 0.0 : sdeType.capacity(),
                    sdeType.published(),
                    sdeType.iconID(),
                    sdeType.graphicID(),
                    sdeType.marketGroupID()
                )
            );

            if (sdeType.variationParentTypeID() != null) {
                typeVariants.computeIfAbsent(sdeType.variationParentTypeID(), this::produceSet).add(typeID);
            }

            if (sdeType.metaGroupID() != null) {
                metaTypes.put(typeID, sdeType.metaGroupID());
            }
        });

        reader.readTypeBonuses((typeID, typeBonus) -> {
            List<TypeTraits.Bonus> miscBonuses = this.produceList();
            if (typeBonus.miscBonuses() != null) {
                Arrays.stream(typeBonus.miscBonuses())
                    .sorted(Comparator.comparingInt(SDEReader.SdeBonus::importance))
                    .forEach(bonus -> miscBonuses.add(new TypeTraits.Bonus(bonus.bonus(), bonus.bonusText().en(), bonus.unitID())));
            }
            List<TypeTraits.Bonus> roleBonuses = this.produceList();
            if (typeBonus.roleBonuses() != null) {
                Arrays.stream(typeBonus.roleBonuses())
                    .sorted(Comparator.comparingInt(SDEReader.SdeBonus::importance))
                    .forEach(bonus -> roleBonuses.add(new TypeTraits.Bonus(bonus.bonus(), bonus.bonusText().en(), bonus.unitID())));
            }
            Map<Integer, List<TypeTraits.Bonus>> skillBonuses = this.produceMap();
            if (typeBonus.types() != null) {
                for (Map.Entry<Integer, SDEReader.SdeBonus[]> entry : typeBonus.types().entrySet()) {
                    List<TypeTraits.Bonus> bonusList = skillBonuses.computeIfAbsent(entry.getKey(), this::produceList);

                    Arrays.stream(entry.getValue())
                        .sorted(Comparator.comparingInt(SDEReader.SdeBonus::importance))
                        .forEach(bonus -> bonusList.add(new TypeTraits.Bonus(bonus.bonus(), bonus.bonusText().en(), bonus.unitID())));
                }
            }

            this.typeTraits.put(typeID, new TypeTraits(miscBonuses, roleBonuses, skillBonuses));
        });


        this.variants = this.produceMap();
        for (Map.Entry<Integer, Set<Integer>> entry : typeVariants.entrySet()) {
            entry.getValue().add(entry.getKey());
            for (Integer typeID : entry.getValue()) {
                this.variants.put(typeID, entry.getValue());
            }
        }

        this.attributes = this.produceMap();
        reader.readAttributes((attributeID, attribute) -> {
            this.attributes.put(
                attributeID,
                new Attribute(
                    attributeID,
                    attribute.categoryID(),
                    attribute.name(),
                    attribute.displayName() != null ? attribute.displayName().en() : null,
                    attribute.unitID(),
                    attribute.iconID(),
                    attribute.published(),
                    attribute.highIsGood()
                )
            );
        });

        this.effects = this.produceMap();
        reader.readEffects((effectID, effect) -> {
            this.effects.put(
                effectID,
                new Effect(effectID, effect.effectName())
            );
        });

        this.typeAttributes = this.produceMap();
        this.typeEffects = this.produceMap();
        reader.readDogma(dogma -> {
            this.typeAttributes.computeIfAbsent(dogma.typeID(), this::produceMap)
                .putAll(dogma.attributes());
            this.typeEffects.computeIfAbsent(dogma.typeID(), this::produceSet)
                .addAll(dogma.effects().keySet());
        });

        this.eveIcons = this.produceMap();
        reader.readIcons((iconID, sdeIcon) -> this.eveIcons.put(iconID, sdeIcon.iconFile()));

        this.bpActivities = this.produceMap();
        reader.readBlueprints(blueprint -> {
            Map<IndustryActivityType, IndustryActivity> activityMap = this.bpActivities.computeIfAbsent(blueprint.blueprintTypeID(), _ -> new EnumMap<>(IndustryActivityType.class));
            try {
                if (blueprint.activities().manufacturing() != null) {
                    activityMap.put(
                        IndustryActivityType.MANUFACTURING,
                        mapActivity(blueprint.blueprintTypeID(), IndustryActivityType.MANUFACTURING, blueprint.activities().manufacturing())
                    );
                }
                if (blueprint.activities().research_time() != null) {
                    activityMap.put(
                        IndustryActivityType.RESEARCH_TIME,
                        mapActivity(blueprint.blueprintTypeID(), IndustryActivityType.RESEARCH_TIME, blueprint.activities().research_time())
                    );
                }
                if (blueprint.activities().research_material() != null) {
                    activityMap.put(
                        IndustryActivityType.RESEARCH_MATERIAL,
                        mapActivity(blueprint.blueprintTypeID(), IndustryActivityType.RESEARCH_MATERIAL, blueprint.activities().research_material())
                    );
                }
                if (blueprint.activities().copying() != null) {
                    activityMap.put(
                        IndustryActivityType.COPYING,
                        mapActivity(blueprint.blueprintTypeID(), IndustryActivityType.COPYING, blueprint.activities().copying())
                    );
                }
                if (blueprint.activities().invention() != null) {
                    activityMap.put(
                        IndustryActivityType.INVENTION,
                        mapActivity(blueprint.blueprintTypeID(), IndustryActivityType.INVENTION, blueprint.activities().invention())
                    );
                }
                if (blueprint.activities().reaction() != null) {
                    activityMap.put(
                        IndustryActivityType.REACTIONS,
                        mapActivity(blueprint.blueprintTypeID(), IndustryActivityType.REACTIONS, blueprint.activities().reaction())
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("Error in BP: " + blueprint.blueprintTypeID(), e);
            }
        });

        this.reprocessingMaterials = this.produceMap();
        reader.readMaterials((typeID, materials) -> {
            if (materials.materials().length > 0) {
                var prev = this.reprocessingMaterials.put(
                    typeID,
                    Arrays.stream(materials.materials()).collect(Collectors.toMap(SDEReader.SdeTypeMaterial::materialTypeID, SDEReader.SdeTypeMaterial::quantity))
                );
                if (prev != null) throw new IllegalStateException("Duplicate typeMaterials for type " + typeID);
            }
        });

        this.planetSchematics = this.produceMap();
        reader.readSchematics((schematicID, schematic) -> {
            int outputQuantity = -1;
            int outputType = -1;
            LinkedHashMap<Integer, Integer> inputs = new LinkedHashMap<>();
            for (Map.Entry<Integer, SDEReader.SdePlanetSchematicItem> entry : schematic.types().entrySet()) {
                if (entry.getValue().isInput()) {
                    inputs.put(entry.getKey(), entry.getValue().quantity());
                } else {
                    if (outputType != -1) throw new IllegalStateException("Planet schematic with duplicate outputs: " + schematicID);
                    outputType = entry.getKey();
                    outputQuantity = entry.getValue().quantity();
                }
            }
            this.planetSchematics.put(
                schematicID,
                new PlanetSchematic(
                    schematicID,
                    schematic.cycleTime(),
                    outputType,
                    outputQuantity,
                    inputs
                )
            );
        });

        this.metaGroups = this.produceMap();
        reader.readMetaGroups((metaGroupID, metagroup) -> this.metaGroups.put(metaGroupID, new MetaGroup(metaGroupID, metagroup.name().en())));

        this.factions = this.produceMap();
        reader.readFactions((factionID, faction) -> {
            this.factions.put(
                factionID,
                new Faction(
                    factionID,
                    faction.name().en(),
                    faction.iconID()
                )
            );
        });

        this.marketGroups = this.produceMap();
        reader.readMarketGroups((marketGroupID, marketGroup) -> {
            this.marketGroups.put(
                marketGroupID,
                new MarketGroup(
                    marketGroupID,
                    marketGroup.parentGroupID(),
                    marketGroup.name().en(),
                    marketGroup.description() != null ? marketGroup.description().en() : null
                )
            );
        });

        Map<Integer, String> operationNames = this.produceMap();
        this.operationServices = this.produceMap();
        reader.readStationOperations((operationID, operation) -> {
            operationNames.put(operationID, operation.operationName().en());

            EnumSet<Station.Service> services = operationServices.computeIfAbsent(operationID, _ -> EnumSet.noneOf(Station.Service.class));

            for (int serviceID : operation.services()) {
                Station.Service service = Station.Service.fromID(serviceID);
                if (service != null) {
                    services.add(service);
                }
            }
        });

        this.regions = this.produceMap();
        reader.readRegions((regionID, region) -> {
            this.regions.put(regionID, new Region(
                regionID,
                region.name().en(),
                region.position().x(),
                region.position().y(),
                region.position().z(),
                region.factionID(),
                region.wormholeClassID()
            ));
        });

        this.constellations = this.produceMap();
        reader.readConstellations((constellationID, constellation) -> {
            constellations.put(constellationID, new Constellation(
                constellation.regionID(),
                constellationID,
                constellation.name().en(),
                constellation.position().x(),
                constellation.position().y(),
                constellation.position().z(),
                constellation.factionID(),
                constellation.wormholeClassID()
            ));
        });

        this.solarSystems = this.produceMap();
        reader.readSolarSystems((solarSystemID, solarSystem) -> {
            solarSystems.put(solarSystemID, new SolarSystem(
                solarSystem.regionID(),
                solarSystem.constellationID(),
                solarSystemID,
                solarSystem.name().en(),
                solarSystem.position().x(),
                solarSystem.position().y(),
                solarSystem.position().z(),
                solarSystem.securityStatus(),
                solarSystem.factionID(),
                null,   // Filled in when parsing mapStars later.
                solarSystem.wormholeClassID()
            ));
        });

        Map<Integer, Celestial> celestials = this.produceMap();
        this.systemCelestials = this.produceMap();
        reader.readStars((starID, star) -> {
            solarSystems.get(star.solarSystemID()).sunTypeID = star.typeID();
            Celestial starCelestial = new Celestial(
                starID,
                star.typeID(),
                types.get(star.typeID()).groupID,
                Objects.requireNonNull(solarSystems.get(star.solarSystemID()).solarSystemName),
                null,
                null,
                null // Maybe replace with an explicit (0,0,0)
            );
            celestials.put(starID, starCelestial);
            systemCelestials.computeIfAbsent(star.solarSystemID(), this::produceSet).add(starCelestial);
        });

        reader.readPlanets((planetID, planet) -> {
            Celestial planetCelestial = new Celestial(
                planetID,
                planet.typeID(),
                types.get(planet.typeID()).groupID,
                planet.name() != null ? Objects.requireNonNull(planet.name().en()) : celestials.get(planet.orbitID()).itemName + " " + romanNumeral(planet.celestialIndex()),
                planet.celestialIndex(),
                null,
                planet.position()
            );
            celestials.put(planetID, planetCelestial);
            this.systemCelestials.computeIfAbsent(planet.solarSystemID(), this::produceSet).add(planetCelestial);
        });
        reader.readMoons((moonID, moon) -> {
            Celestial moonCelestial = new Celestial(
                moonID,
                moon.typeID(),
                types.get(moon.typeID()).groupID,
                moon.name() != null ? Objects.requireNonNull(moon.name().en()) : celestials.get(moon.orbitID()).itemName + " - Moon " + moon.orbitIndex(),
                moon.celestialIndex(),
                moon.orbitIndex(),
                moon.position()
            );
            celestials.put(moonID, moonCelestial);
            this.systemCelestials.computeIfAbsent(moon.solarSystemID(), this::produceSet).add(moonCelestial);
        });
        reader.readAsteroidBelts((asteroidBeltID, asteroidBelt) -> {
            this.systemCelestials.computeIfAbsent(asteroidBelt.solarSystemID(), this::produceSet)
                .add(new Celestial(
                    asteroidBeltID,
                    asteroidBelt.typeID(),
                    types.get(asteroidBelt.typeID()).groupID,
                    asteroidBelt.name() != null ? Objects.requireNonNull(asteroidBelt.name().en()) : celestials.get(asteroidBelt.orbitID()).itemName + " - Asteroid Belt " + asteroidBelt.orbitIndex(),
                    asteroidBelt.celestialIndex(),
                    asteroidBelt.orbitIndex(),
                    asteroidBelt.position()
                ));
        });

        Map<Integer, String> corporationNames = this.produceMap();
        reader.readNpcCorporations((corporationID, corporation) -> corporationNames.put(corporationID, corporation.name().en()));

        this.stations = this.produceMap();
        reader.readStations((stationID, station) -> {

            String name;
            String orbitName = Objects.requireNonNull(celestials.get(station.orbitID()).itemName);
            String corporationName = Objects.requireNonNull(corporationNames.get(station.ownerID()));
            if (station.useOperationName()) {
                String operationName = Objects.requireNonNull(operationNames.get(station.operationID()));
                name = orbitName + " - " + corporationName + " " + operationName;
            } else {
                name = orbitName + " - " + corporationName;
            }

            this.stations.computeIfAbsent(station.solarSystemID(), this::produceSet)
                .add(new Station(
                    stationID,
                    station.typeID(),
                    name,
                    station.operationID(),
                    this.operationServices.getOrDefault(station.operationID(), EnumSet.noneOf(Station.Service.class))
                ));
        });

//        reader.readUniverseMap(
//                if (system.secondarySun() != null) {
//                    Type type = types.get(system.secondarySun().typeID());
//                    this.celestials.computeIfAbsent(system.solarSystemID(), this::produceSet)
//                        .add(new Celestial(
//                            system.secondarySun().itemID(),
//                            type.typeID,
//                            type.groupID,
//                            type.name,
//                            null,
//                            null,
//                            null
//                        ));
//                }
//
//                for (Map.Entry<Integer, SdeStargate> stargateEntry : system.stargates().entrySet()) {
//                    stargateSystemMap.put(stargateEntry.getKey(), system.solarSystemID());
//                    stargateDestinationMap.put(stargateEntry.getKey(), stargateEntry.getValue().destination());
//                }
//            }
//        );

        this.outJumps = this.produceMap();
        this.inJumps = this.produceMap();
        reader.readStargates((_, stargate) -> {
            // TODO: Celestial object for stargates
            outJumps.computeIfAbsent(stargate.solarSystemID(), this::produceSet)
                .add(stargate.destination().solarSystemID());
            inJumps.computeIfAbsent(stargate.destination().solarSystemID(), this::produceSet)
                .add(stargate.solarSystemID());
        });

        this.warfareBuffs = this.produceMap();
        reader.readDbuffs((buffID, buff) -> {
            this.warfareBuffs.put(buffID, new WarfareBuff(
                buff.displayName() != null ? buff.displayName().en() : null,
                switch (buff.showOutputValueInUI()) {
                    case "ShowNormal" -> WarfareBuff.ShowOutputValue.SHOW_NORMAL;
                    case "ShowInverted" -> WarfareBuff.ShowOutputValue.SHOW_INVERTED;
                    case "Hide" -> WarfareBuff.ShowOutputValue.HIDE;
                    default -> throw new IllegalStateException("Unexpected warfare buff display mode: " + buff.showOutputValueInUI());
                }
            ));
        });

        this.dynamicAttributes = this.produceMap();
        reader.readDynamicAttributes((typeID, dynamicAttributes) -> {
            List<DynamicAttributes.IOMapping> ioMapping = Arrays.stream(dynamicAttributes.inputOutputMapping()).map(io -> new DynamicAttributes.IOMapping(io.resultingType(), io.applicableTypes())).toList();
            LinkedHashMap<Integer, DynamicAttributes.DyAttribute> attributeIDs = new LinkedHashMap<>();
            dynamicAttributes.attributeIDs().forEach((attributeID, dyInfo) -> {
                attributeIDs.put(attributeID, new DynamicAttributes.DyAttribute(dyInfo.min(), dyInfo.max(), ((Integer) 1).equals(dyInfo.highIsGood())));
            });
            this.dynamicAttributes.put(typeID, new DynamicAttributes(ioMapping, attributeIDs));
        });

        this.graphicFolders = this.produceMap();
        reader.readGraphics((graphicID, graphic) -> {
            if (graphic.iconFolder() != null) {
                this.graphicFolders.put(graphicID, graphic.iconFolder().replace('\\', '/'));
            }
        });

        if (patch) this.patch();
        this.loadViews();
    }

    private String romanNumeral(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            case 11 -> "XI";
            case 12 -> "XII";
            case 13 -> "XIII";
            case 14 -> "XIV";
            case 15 -> "XV";
            case 16 -> "XVI";
            case 17 -> "XVII";
            case 18 -> "XVIII";
            case 19 -> "XIX";
            case 20 -> "XX";
            default -> throw new IllegalArgumentException("Roman numeral out of bounds: " + num);
        };
    }

    private IndustryActivity mapActivity(int bpTypeID, IndustryActivityType activityType, SDEReader.SdeBpActivity activity) {
        return new IndustryActivity(
            bpTypeID,
            activityType,
            activity.time(),
            activity.materials() != null
                ? Arrays.stream(activity.materials())
                    .collect(Collectors.toMap(
                        SDEReader.SdeBpItem::typeID,
                        SDEReader.SdeBpItem::quantity,
                        (l, r) -> {
                            if ((int) l == (int) r) {
                                return l;
                            } else {
                                throw new IllegalStateException("Duplicate BP material entry with mixed quantity: " + l + " " + r);
                            }
                        },
                        this::produceMap
                    ))
                : Map.of(),
            activity.products() != null
                ? Arrays.stream(activity.products())
                    .collect(Collectors.toMap(
                        SDEReader.SdeBpItem::typeID,
                        SDEReader.SdeBpItem::quantity,
                        (l, r) -> {
                            if ((int) l == (int) r) {
                                return l;
                            } else {
                                throw new IllegalStateException("Duplicate BP product entry with mixed quantity: " + l + " " + r);
                            }
                        },
                        this::produceMap
                    ))
                : Map.of(),
            activity.products() != null
                ? Arrays.stream(activity.products())
                    .filter(item -> item.probability() != null)
                    .collect(Collectors.toMap(
                        SDEReader.SdeBpItem::typeID,
                        SDEReader.SdeBpItem::probability,
                        (l, r) -> {
                            if ((double) l == (double) r) {
                                return l;
                            } else {
                                throw new IllegalStateException("Duplicate BP product entry with mixed probability: " + l + " " + r);
                            }
                        },
                        this::produceMap
                    ))
                : Map.of(),
            activity.skills() != null
                ? Arrays.stream(activity.skills())
                    .collect(Collectors.toMap(
                        SDEReader.SdeBpSkill::typeID,
                        SDEReader.SdeBpSkill::level,
                        (l, r) -> { // Some (unused/invalid) entries have duplicate records
                            if ((int) l == (int) r) {
                                return l;
                            } else {
                                throw new IllegalStateException("Duplicate BP skill entry with mixed levels: " + l + " " + r);
                            }
                        },
                        this::produceMap
                    ))
                : Map.of()
        );
    }


    @Override
    public Map<Integer, Category> getCategories() {
        return categories;
    }

    @Override
    public Map<Integer, Group> getGroups() {
        return groups;
    }

    @Override
    public Map<Integer, Type> getTypes() {
        return types;
    }

    @Override
    public Map<Integer, TypeTraits> getTypeTraits() {
        return typeTraits;
    }

    @Override
    public Map<Integer, Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getTypeAttributes() {
        return typeAttributes;
    }

    @Override
    public Map<Integer, Effect> getEffects() {
        return effects;
    }

    @Override
    public Map<Integer, Set<Integer>> getTypeEffects() {
        return typeEffects;
    }

    @Override
    public Map<Integer, String> getEveIcons() {
        return eveIcons;
    }

    @Override
    public Map<Integer, EnumMap<IndustryActivityType, IndustryActivity>> getBpActivities() {
        return bpActivities;
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getReprocessingMaterials() {
        return reprocessingMaterials;
    }

    @Override
    public Map<Integer, PlanetSchematic> getPlanetSchematics() {
        return planetSchematics;
    }

    @Override
    public Map<Integer, MetaGroup> getMetaGroups() {
        return metaGroups;
    }

    @Override
    public Map<Integer, Set<Integer>> getVariants() {
        return variants;
    }

    @Override
    public Map<Integer, Integer> getMetaTypes() {
        return metaTypes;
    }

    @Override
    public Map<Integer, SolarSystem> getSolarSystems() {
        return solarSystems;
    }

    @Override
    public Map<Integer, Constellation> getConstellations() {
        return constellations;
    }

    @Override
    public Map<Integer, Region> getRegions() {
        return regions;
    }

    @Override
    public Map<Integer, Set<Integer>> getOutJumps() {
        return outJumps;
    }

    @Override
    public Map<Integer, Set<Integer>> getInJumps() {
        return inJumps;
    }

    @Override
    public Map<Integer, Set<Celestial>> getSystemCelestials() {
        return systemCelestials;
    }

    @Override
    public Map<Integer, EnumSet<Station.Service>> getOperationServices() {
        return operationServices;
    }

    @Override
    public Map<Integer, Set<Station>> getStations() {
        return stations;
    }

    @Override
    public Map<Integer, Faction> getFactions() {
        return factions;
    }

    @Override
    public Map<Integer, MarketGroup> getMarketGroups() {
        return marketGroups;
    }

    @Override
    public Map<Integer, WarfareBuff> getWarfareBuffs() {
        return warfareBuffs;
    }

    @Override
    public Map<Integer, DynamicAttributes> getDynamicAttributes() {
        return dynamicAttributes;
    }

    @Override
    public Map<Integer, String> getGraphicFolders() {
        return graphicFolders;
    }
}
