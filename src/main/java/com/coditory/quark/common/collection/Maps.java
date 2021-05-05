package com.coditory.quark.common.collection;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.coditory.quark.common.check.Args.checkNotNull;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

public final class Maps {
    private Maps() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static <K, V> Map<V, K> invert(Map<K, V> map) {
        checkNotNull(map, "map");
        Map<V, K> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : map.entrySet()) {
            if (!result.containsKey(entry.getValue())) {
                result.put(entry.getValue(), entry.getKey());
            }
        }
        return unmodifiableMap(result);
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static <K, V> Map<K, V> nullToEmpty(@Nullable Map<K, V> map) {
        return map == null ? Map.of() : map;
    }

    @Nullable
    public static <K, V> Map<K, V> emptyToNull(@Nullable Map<K, V> map) {
        return map == null || map.isEmpty() ? null : map;
    }

    public static <K, V, K2, V2> Map<K2, V2> map(Map<K, V> map, Function<Map.Entry<K, V>, Map.Entry<K2, V2>> mapper) {
        checkNotNull(map, "map");
        checkNotNull(mapper, "mapper");
        return map.entrySet().stream()
                .map(mapper)
                .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <K, V, R> Map<R, V> mapKeys(Map<K, V> map, Function<K, R> mapper) {
        checkNotNull(map, "map");
        checkNotNull(mapper, "mapper");
        return map.entrySet().stream()
                .collect(toUnmodifiableMap(entry -> mapper.apply(entry.getKey()), Entry::getValue));
    }

    public static <K, V, R> Map<K, R> mapValues(Map<K, V> map, Function<V, R> mapper) {
        checkNotNull(map, "map");
        checkNotNull(mapper, "mapper");
        return map.entrySet().stream()
                .collect(toUnmodifiableMap(Entry::getKey, entry -> mapper.apply(entry.getValue())));
    }

    public static <K, V> Map<K, V> filter(Map<K, V> map, Predicate<Entry<K, V>> predicate) {
        checkNotNull(map, "map");
        checkNotNull(predicate, "predicate");
        return map.entrySet().stream()
                .filter(predicate)
                .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    public static <K, V> Map<K, V> filterByKey(Map<K, V> map, Predicate<K> predicate) {
        checkNotNull(map, "map");
        checkNotNull(predicate, "predicate");
        return map.entrySet().stream()
                .filter(entry -> predicate.test(entry.getKey()))
                .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    public static <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate) {
        checkNotNull(map, "map");
        checkNotNull(predicate, "predicate");
        return map.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    public static <K, V> Map<K, V> put(Map<K, V> map, K key, V value) {
        checkNotNull(map, "map");
        checkNotNull(key, "key");
        checkNotNull(value, "value");
        Map<K, V> result = new LinkedHashMap<>(map);
        result.put(key, value);
        return unmodifiableMap(result);
    }

    public static <K, V> Map<K, V> putAll(Map<K, V> mapA, Map<K, V> mapB) {
        checkNotNull(mapA, "mapA");
        checkNotNull(mapB, "mapB");
        Map<K, V> result = new LinkedHashMap<>(mapA);
        result.putAll(mapB);
        return unmodifiableMap(result);
    }

    public static <K, V> Map<K, V> remove(Map<K, V> map, K key) {
        checkNotNull(map, "map");
        checkNotNull(key, "key");
        Map<K, V> result = new LinkedHashMap<>(map);
        result.remove(key);
        return unmodifiableMap(result);
    }

    public static <K, V> Map<K, V> removeAll(Map<K, V> map, Collection<K> keys) {
        checkNotNull(map, "map");
        checkNotNull(keys, "keys");
        Map<K, V> result = new LinkedHashMap<>(map);
        keys.forEach(result::remove);
        return unmodifiableMap(result);
    }

    public static <K, V> MapBuilder<K, V> builder() {
        return new MapBuilder<>();
    }

    public static <K, V> MapBuilder<K, V> builder(int size) {
        return new MapBuilder<>(size);
    }

    public static <K, V> MapBuilder<K, V> builderWith(K key, V value) {
        return new MapBuilder<K, V>()
                .put(key, value);
    }

    public static class MapBuilder<K, V> {
        private final Map<K, V> map;

        private MapBuilder(int size) {
            map = new LinkedHashMap<>(size);
        }

        private MapBuilder() {
            map = new LinkedHashMap<>();
        }

        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return unmodifiableMap(map);
        }
    }
}
