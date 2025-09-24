package net.sentientturtle.nee.data;

import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;

public record DataSources(
    SDEData sdeData,
    SharedCacheReader sharedCache,
    String gameVersion
) {}
