package net.sentientturtle.nee.data;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import net.sentientturtle.nee.data.datatypes.*;
import net.sentientturtle.nee.util.*;

import java.util.*;

/**
 * {@link DataSupplier} implementation that retrieves data from (an SQLite conversion of) the EVE Online Static Data Export
 *
 * @see SDEUtils
 */
public class SQLiteDataSupplier extends DataSupplier {
    private final Map<Integer, Category> categories;
    private final Map<Integer, Group> groups;

    private final Map<Integer, Attribute> attributes;
    private final Map<Integer, Effect> effects;

    private final Map<Integer, Type> types;
    private final Map<Integer, Map<Integer, Double>> typeAttributes;
    private final Map<Integer, Set<Integer>> typeEffects;
    private final Map<Integer, Map<Integer, List<TypeTraitBonus>>> typeTraits;

    private final Map<Integer, String> eveIcons;
    private final Map<Integer, IndustryActivityType> industryActivityTypes;
    private final Map<Integer, Map<Integer, IndustryActivity>> bpActivities;
    private final Map<Integer, Map<Integer, Integer>> reprocessingMaterials;
    private final Map<Integer, PlanetSchematic> planetSchematics;
    private final Map<Integer, MetaGroup> metaGroups;
    private final Map<Integer, Set<Integer>> variants;
    private final Map<Integer, Integer> parentTypes;
    private final Map<Integer, Integer> metaTypes;
    private final List<SolarSystem> solarSystems;   // TODO: Turn into map
    private final List<Constellation> constellations;
    private final List<Region> regions;
    private final Map<Integer, Set<Integer>> outJumps;
    private final Map<Integer, Set<Integer>> inJumps;
    private final Map<Integer, Set<Jump>> constellationJumps;
    private final Map<Integer, Set<Jump>> regionJumps;
    private final Map<Integer, Faction> factions;
    private final Map<Integer, MarketGroup> marketGroups;

    public SQLiteDataSupplier(SQLiteConnection connection) throws SQLiteException {
        if (!connection.isOpen()) connection.open();

        categories = produceMap();
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

        groups = produceMap();
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

        types = produceMap();
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

        typeTraits = produceMap();
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
            typeTraits.computeIfAbsent(st.columnInt(0), this::produceMap)
                    .computeIfAbsent(st.columnInt(1), this::produceList)
                    .add(new TypeTraitBonus(st.columnNull(2) ? null : st.columnDouble(2), st.columnString(3), st.columnNull(4) ? null : st.columnInt(4)));
        }
        st.dispose();

        typeAttributes = produceMap();
        st = connection.prepare("SELECT typeID, attributeID, valueFloat, valueInt FROM dgmTypeAttributes");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !(st.columnNull(2) && st.columnNull(3));
            typeAttributes.computeIfAbsent(st.columnInt(0), this::produceMap)
                            .put(st.columnInt(1), st.columnNull(2) ? st.columnDouble(3) : st.columnDouble(2));
        }
        st.dispose();

        typeEffects = produceMap();
        st = connection.prepare("SELECT typeID, effectID FROM dgmTypeEffects");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            typeEffects.computeIfAbsent(st.columnInt(0), this::produceSet).add(st.columnInt(1));
        }

        attributes = produceMap();
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

        effects = produceMap();
        st = connection.prepare("SELECT effectID, effectName FROM dgmEffects ORDER BY effectID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            int effectID = st.columnInt(0);
            effects.put(effectID, new Effect(effectID, st.columnString(1)));
        }
        st.dispose();

        eveIcons = produceMap();
        st = connection.prepare("SELECT iconID, iconFile FROM eveIcons");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            eveIcons.put(st.columnInt(0), st.columnString(1));
        }
        st.dispose();

        industryActivityTypes = produceMap();
        st = connection.prepare("SELECT activityID, activityName, published FROM ramActivities");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2);
            int activityID = st.columnInt(0);
            industryActivityTypes.put(activityID, new IndustryActivityType(activityID, st.columnString(1), st.columnInt(2) == 1));
        }
        st.dispose();

        bpActivities = produceMap();
        st = connection.prepare("SELECT industryActivity.typeID, industryActivity.activityID, industryActivity.time FROM industryActivity");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2);

            int bpTypeID = st.columnInt(0);
            int activityID = st.columnInt(1);

            bpActivities.computeIfAbsent(bpTypeID, this::produceMap)
                    .put(
                            activityID,
                            new IndustryActivity(
                                    bpTypeID,
                                    activityID,
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
            IndustryActivity activity = bpActivities.get(typeID).get(activityID);
            if (activity == null) throw new IllegalStateException("ActivityMaterials defined for non-extant activity: " + typeID + ":" + activityID);

            activity.materialMap.put(st.columnInt(2), st.columnInt(3));
        }
        st.dispose();

        st = connection.prepare("SELECT typeID, activityID, productTypeID, probability FROM industryActivityProbabilities");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);

            int typeID = st.columnInt(0);
            int activityID = st.columnInt(1);
            IndustryActivity activity = bpActivities.get(typeID).get(activityID);
            if (activity == null) throw new IllegalStateException("ActivityProbability defined for non-extant activity: " + typeID + ":" + activityID);

            activity.probabilityMap.put(st.columnInt(2), st.columnDouble(3));
        }
        st.dispose();

        st = connection.prepare("SELECT typeID, activityID, productTypeID, quantity FROM industryActivityProducts");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);

            int typeID = st.columnInt(0);
            int activityID = st.columnInt(1);
            IndustryActivity activity = bpActivities.get(typeID).get(activityID);
            if (activity == null) throw new IllegalStateException("ActivityProduct defined for non-extant activity: " + typeID + ":" + activityID);

            activity.productMap.put(st.columnInt(2), st.columnInt(3));
        }
        st.dispose();

        st = connection.prepare("SELECT typeID, activityID, skillID, level FROM industryActivitySkills");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3);

            int typeID = st.columnInt(0);
            int activityID = st.columnInt(1);
            IndustryActivity activity = bpActivities.get(typeID).get(activityID);
            if (activity == null) throw new IllegalStateException("ActivitySkill defined for non-extant activity: " + typeID + ":" + activityID);

            activity.skillMap.put(st.columnInt(2), st.columnInt(3));
        }
        st.dispose();

        reprocessingMaterials = produceMap();
        st = connection.prepare("SELECT typeID, materialTypeID, quantity FROM invTypeMaterials");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2);
            reprocessingMaterials.computeIfAbsent(st.columnInt(0), this::produceMap).put(st.columnInt(1), st.columnInt(2));
        }
        st.dispose();

        planetSchematics = produceMap();
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

        metaGroups = produceMap();
        st = connection.prepare("SELECT metaGroupID, metaGroupName FROM invMetaGroups ORDER BY metaGroupID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            metaGroups.put(st.columnInt(0), new MetaGroup(st.columnInt(0), st.columnString(1)));
        }
        st.dispose();

        variants = produceMap();
        parentTypes = produceMap();
        metaTypes = produceMap();
        st = connection.prepare("SELECT typeID, parentTypeID, metaGroupID FROM invMetaTypes ORDER BY parentTypeID");    // Order by parentTypeID so we encounter rows without parent type first
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(2);

            int typeID = st.columnInt(0);
            Integer parentTypeID = st.columnNull(1) ? null : st.columnInt(1);
            int metaGroupID = st.columnInt(2);

            if (parentTypeID != null) {
                parentTypes.put(typeID, parentTypeID);
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

        solarSystems = produceList();
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
              sunTypeID
            FROM mapSolarSystems
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
            solarSystems.add(new SolarSystem(
                    st.columnInt(0),
                    st.columnInt(1),
                    st.columnInt(2),
                    st.columnString(3),
                    st.columnDouble(4),
                    st.columnDouble(5),
                    st.columnDouble(6),
                    st.columnDouble(7),
                    st.columnNull(8) ? null : st.columnInt(8),
                    st.columnNull(9) ? null : st.columnInt(9)
            ));
        }
        st.dispose();

        constellations = produceList();
        st = connection.prepare("SELECT regionID, constellationID, constellationName, x, y, z, factionID FROM mapConstellations");
        while (st.step()) {
            assert !st.columnNull(0)
                   && !st.columnNull(1)
                   && !st.columnNull(2)
                   && !st.columnNull(3)
                   && !st.columnNull(4)
                   && !st.columnNull(5);
            constellations.add(new Constellation(
                    st.columnInt(0),
                    st.columnInt(1),
                    st.columnString(2),
                    st.columnDouble(3),
                    st.columnDouble(4),
                    st.columnDouble(5),
                    st.columnNull(6) ? null : st.columnInt(6)
            ));
        }
        st.dispose();

        regions = produceList();
        st = connection.prepare("SELECT regionID, regionName, x, y, z, factionID, xMin, yMin, zMin, xMax, yMax, zMax FROM mapRegions");
        while (st.step()) {
            assert !st.columnNull(0)
                   && !st.columnNull(1)
                   && !st.columnNull(2)
                   && !st.columnNull(3)
                   && !st.columnNull(4);
            regions.add(new Region(
                    st.columnInt(0),
                    st.columnString(1),
                    st.columnDouble(2),
                    st.columnDouble(3),
                    st.columnDouble(4),

                    st.columnDouble(6),
                    st.columnDouble(7),
                    st.columnDouble(8),
                    st.columnDouble(9),
                    st.columnDouble(10),
                    st.columnDouble(11),

                    st.columnNull(5) ? null : st.columnInt(5)
            ));
        }
        st.dispose();


        constellationJumps = this.produceMap();
        regionJumps = this.produceMap();
        outJumps = this.produceMap();
        inJumps = this.produceMap();
        st = connection.prepare("SELECT fromSolarSystemID, toSolarSystemID, fromConstellationID, toConstellationID, fromRegionID, toRegionID FROM mapSolarSystemJumps");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1) && !st.columnNull(2) && !st.columnNull(3) && !st.columnNull(4) && !st.columnNull(5);
            int from = st.columnInt(0);
            int to = st.columnInt(1);
            outJumps.computeIfAbsent(from, this::produceSet).add(to);
            inJumps.computeIfAbsent(from, this::produceSet).add(from);

            Jump jump;
            if (from > to) {
                jump = new Jump(to, from);
            } else {
                jump = new Jump(from ,to);
            }
            if (st.columnInt(2) == st.columnInt(3)) {
                constellationJumps.computeIfAbsent(st.columnInt(2), this::produceSet).add(jump);
            }
            if (st.columnInt(4) == st.columnInt(5)) {
                regionJumps.computeIfAbsent(st.columnInt(4), this::produceSet).add(jump);
            }
        }
        st.dispose();


        factions = produceMap();
        st = connection.prepare("SELECT factionID, factionName, corporationID from chrFactions ORDER BY factionID");
        while (st.step()) {
            assert !st.columnNull(0) && !st.columnNull(1);
            int factionID = st.columnInt(0);
            factions.put(
                    factionID,
                    new Faction(
                            factionID,
                            st.columnString(1),
                            st.columnNull(2) ? null : st.columnInt(2)
                    )
            );
        }
        st.dispose();

        marketGroups = produceMap();
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

        this.patch();
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
    public Map<Integer, Map<Integer, List<TypeTraitBonus>>> getTypeTraits() {
        return typeTraits;
    }

    @Override
    public Map<Integer, String> getEveIcons() {
        return eveIcons;
    }

    @Override
    public Map<Integer, IndustryActivityType> getIndustryActivityTypes() {
        return industryActivityTypes;
    }

    @Override
    public Map<Integer, Map<Integer, IndustryActivity>> getBpActivities() {
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
    public Map<Integer, Integer> getParentTypes() {
        return parentTypes;
    }

    @Override
    public Map<Integer, Integer> getMetaTypes() {
        return metaTypes;
    }

    @Override
    public List<SolarSystem> getSolarSystems() {
        return solarSystems;
    }

    @Override
    public List<Constellation> getConstellations() {
        return constellations;
    }

    @Override
    public List<Region> getRegions() {
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
    public Map<Integer, Set<Jump>> getConstellationJumps() {
        return constellationJumps;
    }

    @Override
    public Map<Integer, Set<Jump>> getRegionJumps() {
        return regionJumps;
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
