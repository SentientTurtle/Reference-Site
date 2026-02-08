package net.sentientturtle.nee.data;

import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.sharedcache.SharedCache;

public record DataSources(
    SDEData sdeData,
    SharedCache sharedCache,
    String gameVersion
) {}
