package net.sentientturtle.nee.data.sde;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import net.sentientturtle.nee.data.datatypes.*;

import java.util.*;

/**
 * {@link SDEData} implementation that retrieves data from (an SQLite conversion of) the EVE Online Static Data Export
 *
 * @see SDEUtils
 */
public class SQLiteSDEData extends SDEData {
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

    public SQLiteSDEData(SQLiteConnection connection, boolean patch) throws SQLiteException {
        if (!connection.isOpen()) connection.open();

        categories = this.produceMap();
        SQLiteStatement st = connection.prepare("""
            SELECT
              categoryID,
              categoryName,
              iconID,
              published
            FROM invCategories
            ORDER BY categoryID
            """);
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(3);
            int categoryID = st.columnInt(0);
            categories.put(
                    categoryID,
                    new Category(
                            categoryID,
                            st.columnString(1),
                            st.columnNull(2) ? null : st.columnInt(2),
                            st.columnInt(3) != 0
                    )
            );
        }
        st.dispose();

        groups = this.produceMap();
        st = connection.prepare("""
            SELECT
              groupID,
              categoryID,
              groupName,
              iconID,
              published
            FROM invGroups
            ORDER BY groupID
            """);
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(4);
            int groupID = st.columnInt(0);
            groups.put(
                    groupID,
                    new Group(
                            groupID,
                            st.columnInt(1),
                            st.columnString(2),
                            st.columnNull(3) ? null : st.columnInt(3),
                            st.columnInt(4) == 1
                    )
            );
        }
        st.dispose();

        types = this.produceMap();
        st = connection.prepare("""
            SELECT
              typeID,
              groupID,
              typeName,
              description,
              mass,
              volume,
              capacity,
              published,
              iconID,
              graphicID,
              marketGroupID
            FROM invTypes
            ORDER BY typeID
            """);
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3) && !st.columnNull(4) && !st.columnNull(5) && !st.columnNull(6) && !st.columnNull(7);
            int typeID = st.columnInt(0);
            types.put(typeID,
                    new Type(
                            typeID,
                            st.columnInt(1),
                            st.columnString(2),
                            st.columnString(3),
                            st.columnDouble(4),
                            st.columnDouble(5),
                            st.columnDouble(6),
                            st.columnInt(7) == 1,
                            st.columnNull(8) ? null : st.columnInt(8),
                            st.columnNull(9) ? null : st.columnInt(9),
                            st.columnNull(10) ? null : st.columnInt(10)
                    )
            );
        }
        st.dispose();

        typeTraits = this.produceMap();
        st = connection.prepare("""
            SELECT
              typeID,
              skillID,
              bonus,
              bonusText,
              unitID
            FROM invTraits
            """);
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(3);
            TypeTraits traits = typeTraits.computeIfAbsent(st.columnInt(0), _ -> new TypeTraits(this.produceList(), this.produceList(), this.produceMap()));

            TypeTraits.Bonus bonus = new TypeTraits.Bonus(st.columnNull(2) ? null : st.columnDouble(2), st.columnString(3), st.columnNull(4) ? null : st.columnInt(4));

            int skillID = st.columnInt(1);
            switch (skillID) {
                case -1 -> traits.roleBonuses().add(bonus);
                case -2 -> traits.miscBonuses().add(bonus);
                default -> traits.skillBonuses().computeIfAbsent(skillID, this::produceList).add(bonus);
            }
        }
        st.dispose();

        typeAttributes = this.produceMap();
        st = connection.prepare("SELECT typeID, attributeID, valueFloat, valueInt FROM dgmTypeAttributes");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !(st.columnNull(2) && st.columnNull(3));
            typeAttributes.computeIfAbsent(st.columnInt(0), this::produceMap)
                            .put(st.columnInt(1), st.columnNull(2) ? st.columnDouble(3) : st.columnDouble(2));
        }
        st.dispose();

        typeEffects = this.produceMap();
        st = connection.prepare("SELECT typeID, effectID FROM dgmTypeEffects");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            typeEffects.computeIfAbsent(st.columnInt(0), this::produceSet).add(st.columnInt(1));
        }

        attributes = this.produceMap();
        st = connection.prepare("SELECT attributeID, categoryID, attributeName, displayName, unitID, iconID, published, highIsGood FROM dgmAttributeTypes ORDER BY attributeID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(6) && !st.columnNull(7);
            int attributeID = st.columnInt(0);
            attributes.put(
                    attributeID,
                    new Attribute(
                            attributeID,
                            st.columnNull(1) ? null : st.columnInt(1),
                            st.columnNull(2) ? null : st.columnString(2),
                            st.columnNull(3) ? null : st.columnString(3),
                            st.columnNull(4) ? null : st.columnInt(4),
                            st.columnNull(5) ? null : st.columnInt(5),
                            st.columnInt(6) == 1,
                            st.columnInt(7) == 1
                    )
            );
        }
        st.dispose();

        effects = this.produceMap();
        st = connection.prepare("SELECT effectID, effectName FROM dgmEffects ORDER BY effectID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            int effectID = st.columnInt(0);
            effects.put(effectID, new Effect(effectID, st.columnString(1)));
        }
        st.dispose();

        eveIcons = this.produceMap();
        st = connection.prepare("SELECT iconID, iconFile FROM eveIcons");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            eveIcons.put(st.columnInt(0), st.columnString(1));
        }
        st.dispose();

        bpActivities = this.produceMap();
        st = connection.prepare("SELECT industryActivity.typeID, industryActivity.activityID, industryActivity.time FROM industryActivity");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2);

            int bpTypeID = st.columnInt(0);

            int activityID = st.columnInt(1);
            IndustryActivityType activityType = switch (activityID) {
                case 1 -> IndustryActivityType.MANUFACTURING;
                case 3 -> IndustryActivityType.RESEARCH_TIME;
                case 4 -> IndustryActivityType.RESEARCH_MATERIAL;
                case 5 -> IndustryActivityType.COPYING;
                case 8 -> IndustryActivityType.INVENTION;
                case 11 -> IndustryActivityType.REACTIONS;
                default -> throw new IllegalStateException("Unknown activityType: " + activityID);
            };

            bpActivities.computeIfAbsent(bpTypeID, _ -> new EnumMap<>(IndustryActivityType.class))
                    .put(
                        activityType,
                        new IndustryActivity(
                                bpTypeID,
                                activityType,
                                st.columnInt(2),
                                this.produceMap(),
                                this.produceMap(),
                                this.produceMap(),
                                this.produceMap()
                        )
                    );
        }
        st.dispose();

        st = connection.prepare("SELECT typeID, activityID, materialTypeID, quantity FROM industryActivityMaterials ORDER BY typeID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);

            int typeID = st.columnInt(0);
            int activityID = st.columnInt(1);
            IndustryActivityType activityType = switch (activityID) {
                case 1 -> IndustryActivityType.MANUFACTURING;
                case 3 -> IndustryActivityType.RESEARCH_TIME;
                case 4 -> IndustryActivityType.RESEARCH_MATERIAL;
                case 5 -> IndustryActivityType.COPYING;
                case 8 -> IndustryActivityType.INVENTION;
                case 11 -> IndustryActivityType.REACTIONS;
                default -> throw new IllegalStateException("Unknown activityType: " + activityID);
            };
            IndustryActivity activity = bpActivities.get(typeID).get(activityType);
            if (activity == null) throw new IllegalStateException("ActivityMaterials defined for non-extant activity: " + typeID + ":" + activityType);

            activity.materialMap.put(st.columnInt(2), st.columnInt(3));
        }
        st.dispose();

        st = connection.prepare("SELECT typeID, activityID, productTypeID, probability FROM industryActivityProbabilities");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);

            int typeID = st.columnInt(0);
            int activityID = st.columnInt(1);
            IndustryActivityType activityType = switch (activityID) {
                case 1 -> IndustryActivityType.MANUFACTURING;
                case 3 -> IndustryActivityType.RESEARCH_TIME;
                case 4 -> IndustryActivityType.RESEARCH_MATERIAL;
                case 5 -> IndustryActivityType.COPYING;
                case 8 -> IndustryActivityType.INVENTION;
                case 11 -> IndustryActivityType.REACTIONS;
                default -> throw new IllegalStateException("Unknown activityType: " + activityID);
            };
            IndustryActivity activity = bpActivities.get(typeID).get(activityType);
            if (activity == null) throw new IllegalStateException("ActivityProbability defined for non-extant activity: " + typeID + ":" + activityType);

            activity.probabilityMap.put(st.columnInt(2), st.columnDouble(3));
        }
        st.dispose();

        st = connection.prepare("SELECT typeID, activityID, productTypeID, quantity FROM industryActivityProducts");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);

            int typeID = st.columnInt(0);
            int activityID = st.columnInt(1);
            IndustryActivityType activityType = switch (activityID) {
                case 1 -> IndustryActivityType.MANUFACTURING;
                case 3 -> IndustryActivityType.RESEARCH_TIME;
                case 4 -> IndustryActivityType.RESEARCH_MATERIAL;
                case 5 -> IndustryActivityType.COPYING;
                case 8 -> IndustryActivityType.INVENTION;
                case 11 -> IndustryActivityType.REACTIONS;
                default -> throw new IllegalStateException("Unknown activityType: " + activityID);
            };
            IndustryActivity activity = bpActivities.get(typeID).get(activityType);
            if (activity == null) throw new IllegalStateException("ActivityProduct defined for non-extant activity: " + typeID + ":" + activityType);

            activity.productMap.put(st.columnInt(2), st.columnInt(3));
        }
        st.dispose();

        st = connection.prepare("SELECT typeID, activityID, skillID, level FROM industryActivitySkills");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);

            int typeID = st.columnInt(0);
            int activityID = st.columnInt(1);
            IndustryActivityType activityType = switch (activityID) {
                case 1 -> IndustryActivityType.MANUFACTURING;
                case 3 -> IndustryActivityType.RESEARCH_TIME;
                case 4 -> IndustryActivityType.RESEARCH_MATERIAL;
                case 5 -> IndustryActivityType.COPYING;
                case 8 -> IndustryActivityType.INVENTION;
                case 11 -> IndustryActivityType.REACTIONS;
                default -> throw new IllegalStateException("Unknown activityType: " + activityID);
            };
            IndustryActivity activity = bpActivities.get(typeID).get(activityType);
            if (activity == null) throw new IllegalStateException("ActivitySkill defined for non-extant activity: " + typeID + ":" + activityType);

            activity.skillMap.put(st.columnInt(2), st.columnInt(3));
        }
        st.dispose();

        reprocessingMaterials = this.produceMap();
        st = connection.prepare("SELECT typeID, materialTypeID, quantity FROM invTypeMaterials");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2);
            reprocessingMaterials.computeIfAbsent(st.columnInt(0), this::produceMap).put(st.columnInt(1), st.columnInt(2));
        }
        st.dispose();

        planetSchematics = this.produceMap();
        st = connection.prepare("SELECT planetSchematics.schematicID, cycleTime, typeID, quantity FROM planetSchematics JOIN planetSchematicsTypeMap ON planetSchematics.schematicID = planetSchematicsTypeMap.schematicID WHERE isInput = 0");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);
            int schematicID = st.columnInt(0);
            planetSchematics.put(schematicID, new PlanetSchematic(schematicID, st.columnInt(1), st.columnInt(2), st.columnInt(3), this.produceMap()));
        }
        st.dispose();

        st = connection.prepare("SELECT schematicID, typeID, quantity FROM planetSchematicsTypeMap WHERE isInput = 1");
        assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2);
        while (st.step()) {
            planetSchematics.get(st.columnInt(0)).inputs.put(st.columnInt(1), st.columnInt(2));
        }
        st.dispose();

        metaGroups = this.produceMap();
        st = connection.prepare("SELECT metaGroupID, metaGroupName FROM invMetaGroups ORDER BY metaGroupID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            metaGroups.put(st.columnInt(0), new MetaGroup(st.columnInt(0), st.columnString(1)));
        }
        st.dispose();

        variants = this.produceMap();
        metaTypes = this.produceMap();
        st = connection.prepare("SELECT typeID, parentTypeID, metaGroupID FROM invMetaTypes ORDER BY parentTypeID");    // Order by parentTypeID so we encounter rows without parent type first
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(2);

            int typeID = st.columnInt(0);
            Integer parentTypeID = st.columnNull(1) ? null : st.columnInt(1);
            int metaGroupID = st.columnInt(2);

            if (parentTypeID != null) {
                Set<Integer> parentVariants = variants.computeIfAbsent(parentTypeID, this::produceSet);
                parentVariants.add(parentTypeID);
                parentVariants.add(typeID);
                variants.put(typeID, parentVariants);
            } else {
                variants.computeIfAbsent(typeID, this::produceSet).add(typeID);
            }
            metaTypes.put(typeID, metaGroupID);
        }
        st.dispose();
        variants.values().removeIf(s -> s.size() == 1);

        solarSystems = this.produceMap();
        st = connection.prepare("""
            SELECT
              regionID,
              constellationID,
              solarSystemID,
              solarSystemName,
              x,
              y,
              z,
              security,
              factionID,
              sunTypeID,
              coalesce(systemClasses.wormholeClassID, constellationClasses.wormholeClassID, regionClasses.wormholeClassID) AS wormholeClassID
            FROM mapSolarSystems
            LEFT JOIN mapLocationWormholeClasses AS systemClasses ON systemClasses.locationID = solarSystemID
            LEFT JOIN mapLocationWormholeClasses AS constellationClasses ON constellationClasses.locationID = constellationID
            LEFT JOIN mapLocationWormholeClasses AS regionClasses ON regionClasses.locationID = regionID
            """);
        while (st.step()) {
            assert !st.columnNull(0)
                   && !st.columnNull(1)
                   && !st.columnNull(2)
                   && !st.columnNull(3)
                   && !st.columnNull(4)
                   && !st.columnNull(5)
                   && !st.columnNull(6)
                   && !st.columnNull(7);
            solarSystems.put(
                st.columnInt(2),
                new SolarSystem(
                    st.columnInt(0),
                    st.columnInt(1),
                    st.columnInt(2),
                    st.columnString(3),
                    st.columnDouble(4),
                    st.columnDouble(5),
                    st.columnDouble(6),
                    st.columnDouble(7),
                    st.columnNull(8) ? null : st.columnInt(8),
                    st.columnNull(9) ? null : st.columnInt(9),
                    st.columnNull(10) ? null : st.columnInt(10)
            ));
        }
        st.dispose();

        constellations = this.produceMap();
        st = connection.prepare("""
            SELECT
            	regionID,
            	constellationID,
            	constellationName,
            	x, y, z,
            	xMin, yMin, zMin,
            	xMax, yMax, zMax,
            	factionID,
            	coalesce(constellationClasses.wormholeClassID, regionClasses.wormholeClassID) AS wormholeClassID
            FROM mapConstellations
            LEFT JOIN mapLocationWormholeClasses AS constellationClasses ON constellationClasses.locationID = constellationID
            LEFT JOIN mapLocationWormholeClasses AS regionClasses ON regionClasses.locationID = regionID
            """);
        while (st.step()) {
            assert !st.columnNull(0)
                   && !st.columnNull(1)
                   && !st.columnNull(2)
                   && !st.columnNull(3)
                   && !st.columnNull(4)
                   && !st.columnNull(5);
            constellations.put(
                st.columnInt(1),
                new Constellation(
                    st.columnInt(0),
                    st.columnInt(1),
                    st.columnString(2),
                    st.columnDouble(3),
                    st.columnDouble(4),
                    st.columnDouble(5),

                    st.columnDouble(6),
                    st.columnDouble(7),
                    st.columnDouble(8),
                    st.columnDouble(9),
                    st.columnDouble(10),
                    st.columnDouble(11),

                    st.columnNull(12) ? null : st.columnInt(12),
                    st.columnNull(13) ? null : st.columnInt(13)
            ));
        }
        st.dispose();

        regions = this.produceMap();
        st = connection.prepare("""
            SELECT
            	regionID,
            	regionName,
            	x, y, z,
            	xMin, yMin, zMin,
            	xMax, yMax, zMax,
            	factionID,
            	regionClasses.wormholeClassID
            FROM mapRegions
            LEFT JOIN mapLocationWormholeClasses AS regionClasses ON regionClasses.locationID = regionID
            """);
        while (st.step()) {
            assert !st.columnNull(0)
                   && !st.columnNull(1)
                   && !st.columnNull(2)
                   && !st.columnNull(3)
                   && !st.columnNull(4);
            regions.put(
                st.columnInt(0),
                new Region(
                    st.columnInt(0),
                    st.columnString(1),
                    st.columnDouble(2),
                    st.columnDouble(3),
                    st.columnDouble(4),

                    st.columnDouble(5),
                    st.columnDouble(6),
                    st.columnDouble(7),
                    st.columnDouble(8),
                    st.columnDouble(9),
                    st.columnDouble(10),

                    st.columnNull(11) ? null : st.columnInt(11),
                    st.columnNull(12) ? null : st.columnInt(12)
            ));
        }
        st.dispose();

        outJumps = this.produceMap();
        inJumps = this.produceMap();
        st = connection.prepare("SELECT fromSolarSystemID, toSolarSystemID, fromConstellationID, toConstellationID, fromRegionID, toRegionID FROM mapSolarSystemJumps");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3) && !st.columnNull(4) && !st.columnNull(5);
            int from = st.columnInt(0);
            int to = st.columnInt(1);
            outJumps.computeIfAbsent(from, this::produceSet).add(to);
            inJumps.computeIfAbsent(from, this::produceSet).add(from);
        }
        st.dispose();

        celestials = this.produceMap();
        st = connection.prepare("SELECT itemID, typeID, groupID, solarsystemID, itemName, celestialIndex, orbitIndex FROM mapDenormalize WHERE solarSystemID IS NOT NULL AND groupID IS NOT 10 AND groupID IS NOT 15");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3) && !st.columnNull(4);
            celestials.computeIfAbsent(st.columnInt(3), this::produceSet)
                .add(new Celestial(
                    st.columnInt(0),
                    st.columnInt(1),
                    st.columnInt(2),
                    st.columnString(4),
                    st.columnNull(5) ? null : st.columnInt(5),
                    st.columnNull(6) ? null : st.columnInt(6)
                ));
        }
        st.dispose();

        stations = this.produceMap();
        st = connection.prepare("SELECT stationID, operationID, stationTypeID, solarSystemID, stationName FROM staStations");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3) && !st.columnNull(4);
            stations.computeIfAbsent(st.columnInt(3), this::produceSet)
                .add(new Station(
                    st.columnInt(0),
                    st.columnInt(2),
                    st.columnString(4),
                    st.columnInt(1),
                    EnumSet.noneOf(Station.Service.class)
                ));
        }
        st.dispose();

        factions = this.produceMap();
        st = connection.prepare("SELECT factionID, factionName, iconID from chrFactions ORDER BY factionID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            int factionID = st.columnInt(0);
            factions.put(
                    factionID,
                    new Faction(
                            factionID,
                            st.columnString(1),
                            st.columnInt(2)
                    )
            );
        }
        st.dispose();

        marketGroups = this.produceMap();
        st = connection.prepare("SELECT marketGroupID, parentGroupID, marketGroupName, description FROM invMarketGroups ORDER BY marketGroupID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(2);
            int marketGroupID = st.columnInt(0);
            marketGroups.put(
                marketGroupID,
                new MarketGroup(
                    marketGroupID,
                    st.columnNull(1) ? null : st.columnInt(1),
                    st.columnString(2),
                    st.columnNull(3) ? null : st.columnString(3)
                )
            );
        }
        st.dispose();
        connection.dispose();

        if (patch) this.patch();
        this.loadViews();
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
    public Map<Integer, Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public Map<Integer, Effect> getEffects() {
        return effects;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getTypeAttributes() {
        return typeAttributes;
    }

    @Override
    public Map<Integer, Set<Integer>> getTypeEffects() {
        return typeEffects;
    }

    @Override
    public Map<Integer, TypeTraits> getTypeTraits() {
        return typeTraits;
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
