package net.sentientturtle.nee.data.sde;

import net.sentientturtle.nee.data.datatypes.*;
import net.sentientturtle.nee.data.sde.YAMLDataExportReader.SdeBpItem;
import net.sentientturtle.nee.data.sde.YAMLDataExportReader.SdeTrait;
import net.sentientturtle.nee.data.sde.YAMLDataExportReader.SdeTypeTraits;
import net.sentientturtle.nee.util.ExceptionUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.sentientturtle.nee.data.sde.YAMLDataExportReader.*;

public class YamlSDEData extends SDEData {
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
    private final Map<Integer, Set<Celestial>> celestials;
    private final Map<Integer, Set<Station>> stations;
    private final Map<Integer, Faction> factions;
    private final Map<Integer, MarketGroup> marketGroups;

    public YamlSDEData(YAMLDataExportReader reader, HashMap<Integer, String> localizationStrings, boolean patch) throws IOException {
        long start = System.nanoTime();
        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {

            this.categories = this.produceMap();
            this.groups = this.produceMap();
            this.types = this.produceMap();
            this.typeTraits = this.produceMap();
            this.metaTypes = this.produceMap();
            this.variants = this.produceMap();
            this.attributes = this.produceMap();
            this.effects = this.produceMap();
            this.typeAttributes = this.produceMap();
            this.typeEffects = this.produceMap();
            this.eveIcons = this.produceMap();
            this.bpActivities = this.produceMap();
            this.reprocessingMaterials = this.produceMap();
            this.planetSchematics = this.produceMap();
            this.metaGroups = this.produceMap();
            this.factions = this.produceMap();
            this.marketGroups = this.produceMap();

            this.stations = Collections.synchronizedMap(this.produceMap());
            this.regions = Collections.synchronizedMap(this.produceMap());
            this.constellations = Collections.synchronizedMap(this.produceMap());
            this.solarSystems = Collections.synchronizedMap(this.produceMap());
            this.celestials = Collections.synchronizedMap(this.produceMap());
            this.outJumps = this.produceMap();
            this.inJumps = this.produceMap();

            ConcurrentHashMap<Integer, Integer> stargateSystemMap = new ConcurrentHashMap<>();
            ConcurrentHashMap<Integer, Integer> stargateDestinationMap = new ConcurrentHashMap<>();

            ConcurrentLinkedDeque<Future<?>> futures = new ConcurrentLinkedDeque<>();
            Collections.addAll(futures,
                executorService.submit(() -> {
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
                }),
                executorService.submit(() -> {
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
                }),
                executorService.submit(() -> {
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

                        if (sdeType.traits() != null) {
                            SdeTypeTraits traits = sdeType.traits();

                            List<TypeTraits.Bonus> miscBonuses = this.produceList();
                            if (traits.miscBonuses() != null) {

                                traits.miscBonuses()
                                    .stream()
                                    .sorted(Comparator.comparingInt(SdeTrait::importance))
                                    .forEach(bonus -> miscBonuses.add(new TypeTraits.Bonus(bonus.bonus(), bonus.bonusText().en(), bonus.unitID())));
                            }
                            List<TypeTraits.Bonus> roleBonuses = this.produceList();
                            if (traits.roleBonuses() != null) {
                                traits.roleBonuses()
                                    .stream()
                                    .sorted(Comparator.comparingInt(SdeTrait::importance))
                                    .forEach(bonus -> roleBonuses.add(new TypeTraits.Bonus(bonus.bonus(), bonus.bonusText().en(), bonus.unitID())));
                            }
                            Map<Integer, List<TypeTraits.Bonus>> skillBonuses = this.produceMap();
                            if (traits.types() != null) {
                                for (Map.Entry<Integer, ArrayList<SdeTrait>> entry : traits.types().entrySet()) {
                                    List<TypeTraits.Bonus> bonusList = skillBonuses.computeIfAbsent(entry.getKey(), this::produceList);

                                    entry.getValue()
                                        .stream()
                                        .sorted(Comparator.comparingInt(SdeTrait::importance))
                                        .forEach(bonus -> bonusList.add(new TypeTraits.Bonus(bonus.bonus(), bonus.bonusText().en(), bonus.unitID())));
                                }
                            }

                            this.typeTraits.put(typeID, new TypeTraits(miscBonuses, roleBonuses, skillBonuses));
                        }
                    });

                    for (Map.Entry<Integer, Set<Integer>> entry : typeVariants.entrySet()) {
                        entry.getValue().add(entry.getKey());
                        for (Integer typeID : entry.getValue()) {
                            this.variants.put(typeID, entry.getValue());
                        }
                    }
                }),
                executorService.submit(() -> {
                    reader.readAttributes((attributeID, attribute) -> {
                        this.attributes.put(
                            attributeID,
                            new Attribute(
                                attributeID,
                                attribute.categoryID(),
                                attribute.name(),
                                attribute.displayNameID() != null ? attribute.displayNameID().en() : null,
                                attribute.unitID(),
                                attribute.iconID(),
                                attribute.published(),
                                attribute.highIsGood()
                            )
                        );
                    });
                }),
                executorService.submit(() -> {
                    reader.readEffects((effectID, effect) -> {
                        this.effects.put(
                            effectID,
                            new Effect(effectID, effect.effectName())
                        );
                    });
                }),
                executorService.submit(() -> {
                    reader.readDogma(dogma -> {
                        this.typeAttributes.computeIfAbsent(dogma.typeID(), this::produceMap)
                            .putAll(dogma.attributes());
                        this.typeEffects.computeIfAbsent(dogma.typeID(), this::produceSet)
                            .addAll(dogma.effects().keySet());
                    });
                }),
                executorService.submit(() -> {
                    reader.readIcons((iconID, sdeIcon) -> this.eveIcons.put(iconID, sdeIcon.iconFile()));
                }),
                executorService.submit(() -> {
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
                }),
                executorService.submit(() -> {
                    reader.readMaterials((typeID, materials) -> {
                        if (materials.materials().size() > 0) {
                            var prev = this.reprocessingMaterials.put(
                                typeID,
                                materials.materials().stream().collect(Collectors.toMap(SdeTypeMaterial::materialTypeID, SdeTypeMaterial::quantity))
                            );
                            if (prev != null) throw new IllegalStateException("Duplicate typeMaterials for type " + typeID);
                        }
                    });
                }),
                executorService.submit(() -> {
                    reader.readSchematics((schematicID, schematic) -> {
                        int outputQuantity = -1;
                        int outputType = -1;
                        LinkedHashMap<Integer, Integer> inputs = new LinkedHashMap<>();
                        for (Map.Entry<Integer, SdePlanetSchematicItem> entry : schematic.types().entrySet()) {
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
                }),
                executorService.submit(() -> {
                    reader.readMetaGroups((metaGroupID, metagroup) -> this.metaGroups.put(metaGroupID, new MetaGroup(metaGroupID, metagroup.nameID().en())));
                }),
                executorService.submit(() -> {
                    reader.readFactions((factionID, faction) -> {
                        this.factions.put(
                            factionID,
                            new Faction(
                                factionID,
                                faction.nameID().en(),
                                faction.iconID()
                            )
                        );
                    });
                }),
                executorService.submit(() -> {
                    reader.readMarketGroups((marketGroupID, marketGroup) -> {
                        this.marketGroups.put(
                            marketGroupID,
                            new MarketGroup(
                                marketGroupID,
                                marketGroup.parentGroupID(),
                                marketGroup.nameID().en(),
                                marketGroup.descriptionID() != null ? marketGroup.descriptionID().en() : null
                            )
                        );
                    });
                }),
                executorService.submit(() -> {
                    HashMap<Integer, EnumSet<Station.Service>> operationServices = new HashMap<>();
                    reader.readStationOperations((operationID, operation) -> {
                        EnumSet<Station.Service> services = operationServices.computeIfAbsent(operationID, _ -> EnumSet.noneOf(Station.Service.class));
                        for (Integer serviceID : operation.services()) {
                            Station.Service service = switch (serviceID) {
                                case 5 -> Station.Service.REPROCESSING;
                                case 7 -> Station.Service.MARKET;
                                case 10 -> Station.Service.CLONEBAY;
                                case 13 -> Station.Service.REPAIRSHOP;
                                case 14 -> Station.Service.INDUSTRY;
                                case 17 -> Station.Service.FITTING;
                                case 21 -> Station.Service.INSURANCE;
                                case 25 -> Station.Service.LPSTORE;
                                case 26 -> Station.Service.MILITIAOFFICE;
                                default -> null;
                            };
                            if (service != null) services.add(service);
                        }
                    });

                    reader.readStations(station -> {
                        this.stations.computeIfAbsent(station.solarSystemID(), this::produceSet)
                            .add(new Station(
                                station.stationID(),
                                station.stationTypeID(),
                                station.stationName(),
                                operationServices.getOrDefault(station.operationID(), EnumSet.noneOf(Station.Service.class))
                            ));
                    });
                }),
                executorService.submit(() -> {
                    HashMap<Integer, String> itemNames = new HashMap<>();
                    reader.readItemNames(itemName -> itemNames.put(itemName.itemID(), itemName.itemName()));

                    reader.readUniverseMap(
                        region -> this.regions.put(
                            region.regionID(),
                            new Region(
                                region.regionID(),
                                Objects.requireNonNull(localizationStrings.get(region.nameID())),
                                region.center()[0],
                                region.center()[1],
                                region.center()[2],
                                region.min()[0],
                                region.min()[1],
                                region.min()[2],
                                region.max()[0],
                                region.max()[1],
                                region.max()[2],
                                region.factionID(),
                                region.wormholeClassID()
                            )
                        ),
                        (regionID, constellation) -> this.constellations.put(
                            constellation.constellationID(),
                            new Constellation(
                                regionID,
                                constellation.constellationID(),
                                Objects.requireNonNull(localizationStrings.get(constellation.nameID())),
                                constellation.center()[0],
                                constellation.center()[1],
                                constellation.center()[2],
                                constellation.min()[0],
                                constellation.min()[1],
                                constellation.min()[2],
                                constellation.max()[0],
                                constellation.max()[1],
                                constellation.max()[2],
                                constellation.factionID(),
                                constellation.wormholeClassID()
                            )
                        ),
                        (parentIDs, system) -> {
                            this.solarSystems.put(
                                system.solarSystemID(),
                                new SolarSystem(
                                    parentIDs.regionID(),
                                    parentIDs.constellationID(),
                                    system.solarSystemID(),
                                    Objects.requireNonNull(localizationStrings.get(system.solarSystemNameID())),
                                    system.center()[0],
                                    system.center()[1],
                                    system.center()[2],
                                    system.security(),
                                    system.factionID(),
                                    system.sunTypeID(),
                                    system.wormholeClassID()
                                )
                            );

                            if (system.secondarySun() != null) {
                                Type type = types.get(system.secondarySun().typeID());
                                this.celestials.computeIfAbsent(system.solarSystemID(), this::produceSet)
                                    .add(new Celestial(
                                        system.secondarySun().itemID(),
                                        type.typeID,
                                        type.groupID,
                                        type.name,
                                        null,
                                        null
                                    ));
                            }

                            for (Map.Entry<Integer, SdePlanet> planetEntry : system.planets().entrySet()) {
                                Type type = types.get(planetEntry.getValue().typeID());

                                String planetName = itemNames.get(planetEntry.getKey());
                                if (planetEntry.getValue().planetNameID() != null) {
                                    planetName = localizationStrings.get(planetEntry.getValue().planetNameID());
                                }

                                this.celestials.computeIfAbsent(system.solarSystemID(), this::produceSet)
                                    .add(new Celestial(
                                        planetEntry.getKey(),
                                        type.typeID,
                                        type.groupID,
                                        Objects.requireNonNull(planetName),
                                        planetEntry.getValue().celestialIndex(),
                                        null
                                    ));

                                if (planetEntry.getValue().asteroidBelts() != null) {
                                    AtomicInteger orbitIndex = new AtomicInteger();
                                    planetEntry.getValue().asteroidBelts()
                                        .entrySet()
                                        .stream()
                                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                                        .forEach(asteroidBeltEntry -> {
                                            String asteroidBeltName = itemNames.get(asteroidBeltEntry.getKey());
                                            if (asteroidBeltEntry.getValue().asteroidBeltNameID() != null) {
                                                asteroidBeltName = localizationStrings.get(asteroidBeltEntry.getValue().asteroidBeltNameID());
                                            }

                                            Type beltType = types.get(asteroidBeltEntry.getValue().typeID());
                                            this.celestials.computeIfAbsent(system.solarSystemID(), this::produceSet)
                                                .add(new Celestial(
                                                    asteroidBeltEntry.getKey(),
                                                    beltType.typeID,
                                                    beltType.groupID,
                                                    Objects.requireNonNull(asteroidBeltName),
                                                    planetEntry.getValue().celestialIndex(),
                                                    orbitIndex.incrementAndGet()
                                                ));
                                        });
                                }
                                if (planetEntry.getValue().moons() != null) {
                                    AtomicInteger orbitIndex = new AtomicInteger();
                                    planetEntry.getValue().moons()
                                        .entrySet()
                                        .stream()
                                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                                        .forEachOrdered(moonEntry -> {
                                            String moonName = itemNames.get(moonEntry.getKey());
                                            if (moonEntry.getValue().moonNameID() != null) {
                                                moonName = localizationStrings.get(moonEntry.getValue().moonNameID());
                                            }

                                            Type moonType = types.get(moonEntry.getValue().typeID());
                                            this.celestials.computeIfAbsent(system.solarSystemID(), this::produceSet)
                                                .add(new Celestial(
                                                    moonEntry.getKey(),
                                                    moonType.typeID,
                                                    moonType.groupID,
                                                    Objects.requireNonNull(moonName),
                                                    planetEntry.getValue().celestialIndex(),
                                                    orbitIndex.incrementAndGet()
                                                ));
                                        });
                                }
                            }

                            for (Map.Entry<Integer, SdeStargate> stargateEntry : system.stargates().entrySet()) {
                                stargateSystemMap.put(stargateEntry.getKey(), system.solarSystemID());
                                stargateDestinationMap.put(stargateEntry.getKey(), stargateEntry.getValue().destination());
                            }
                        },
                        runnable -> futures.addLast(executorService.submit(runnable))
                    );
                })
            );

            Future<?> polled;
            while ((polled = futures.pollFirst()) != null) {
                polled.get();
            }

            executorService.shutdown();


            for (Map.Entry<Integer, Integer> entry : stargateDestinationMap.entrySet()) {
                int k = stargateSystemMap.get(entry.getKey());
                int v = stargateSystemMap.get(entry.getValue());

                outJumps.computeIfAbsent(k, this::produceSet).add(v);
                inJumps.computeIfAbsent(v, this::produceSet).add(k);
            }
        } catch (ExecutionException e) {
            throw ExceptionUtil.<RuntimeException, RuntimeException>sneakyThrow(e.getCause());
        } catch (InterruptedException e) {
            throw ExceptionUtil.<RuntimeException, RuntimeException>sneakyThrow(e);
        }

        System.out.println("SDE loaded in: " + ((double) TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS)) / 1000.0);

        if (patch) this.patch();
        this.loadViews();
    }

    private IndustryActivity mapActivity(int bpTypeID, IndustryActivityType activityType, SdeBpActivity activity) {
        return new IndustryActivity(
            bpTypeID,
            activityType,
            activity.time(),
            activity.materials() != null
                ? activity.materials()
                .stream()
                .collect(Collectors.toMap(
                    SdeBpItem::typeID,
                    SdeBpItem::quantity,
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
                ? activity.products()
                .stream()
                .collect(Collectors.toMap(
                    SdeBpItem::typeID,
                    SdeBpItem::quantity,
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
                ? activity.products()
                .stream()
                .filter(item -> item.probability() != null)
                .collect(Collectors.toMap(
                    SdeBpItem::typeID,
                    SdeBpItem::probability,
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
                ? activity.skills()
                .stream()
                .collect(Collectors.toMap(
                    SdeBpSkill::typeID,
                    SdeBpSkill::level,
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
    public Map<Integer, Set<Celestial>> getCelestials() {
        return celestials;
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
}
