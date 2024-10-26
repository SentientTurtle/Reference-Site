package net.sentientturtle.util.tuple;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collector;

/**
 * 2 value tuple class
 * @param <T1> Type of {@link Tuple2#v1}
 * @param <T2> Type of {@link Tuple2#v2}
 */
public class Tuple2<T1, T2> {
    public final T1 v1;
    public final T2 v2;

    public Tuple2(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public String toString() {
        return "(" + v1 + ", " + v2 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

        if (!Objects.equals(v1, tuple2.v1)) return false;
        return Objects.equals(v2, tuple2.v2);
    }

    @Override
    public int hashCode() {
        int result = v1 != null ? v1.hashCode() : 0;
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        return result;
    }

    // Utility methods for use in streams, allows for easy stream collection to maps, at the cost of object thrashing.

    /**
     * Builds a collector that collects a Tuple2 stream to a {@link HashMap}
     * @param <T1> Type of {@link Tuple2#v1}
     * @param <T2> Type of {@link Tuple2#v2}
     * @return A collector that collects a Tuple2 stream to a {@link HashMap}
     */
    public static <T1, T2> Collector<Tuple2<T1, T2>, HashMap<T1, T2>, HashMap<T1, T2>> collectToMap() {
        return Collector.of(HashMap::new,
                (map, tuple) -> map.put(tuple.v1, tuple.v2),
                (map, map2) -> {
                    map.putAll(map2);
                    return map;
                },
                Collector.Characteristics.UNORDERED);
    }

}
