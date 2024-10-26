package net.sentientturtle.nee.data.datatypes;

/// Data object to represent Stargate "jump" links between solarsystems
public record Jump(int fromSolarSystemID, int toSolarSystemID) {}
