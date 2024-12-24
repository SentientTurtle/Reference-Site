package net.sentientturtle.nee.data;

import net.sentientturtle.nee.data.sharedcache.FSDData;
import net.sentientturtle.nee.data.sharedcache.SharedCacheReader;

public record DataSources(
    net.sentientturtle.nee.data.sde.SDEData SDEData,
    SharedCacheReader sharedCache,
    FSDData fsdData,
    String gameVersion
) {}
