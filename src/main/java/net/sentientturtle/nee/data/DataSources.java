package net.sentientturtle.nee.data;

import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;

public record DataSources(
    SDEData sdeData,
    SharedCacheReader sharedCache,
    FSDData fsdData,
    String gameVersion
) {}
