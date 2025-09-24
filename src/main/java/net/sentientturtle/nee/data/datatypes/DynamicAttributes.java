package net.sentientturtle.nee.data.datatypes;

import java.util.LinkedHashMap;
import java.util.List;

public record DynamicAttributes(List<IOMapping> inputOutputMapping, LinkedHashMap<Integer, DyAttribute> attributeIDs) {
    public record IOMapping(int resultingType, int[] applicableTypes) {}
    public record DyAttribute(double min, double max, boolean highIsGood) {}
}
