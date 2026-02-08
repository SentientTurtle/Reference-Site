package net.sentientturtle.nee.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Position2D(
    @JsonProperty(required = true) double x,
    @JsonProperty(required = true) double y
) {}
