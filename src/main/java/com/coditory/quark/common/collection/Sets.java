package com.coditory.quark.common.collection;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.coditory.quark.common.check.Args.checkNotEmpty;
import static com.coditory.quark.common.check.Args.checkNotNull;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class Sets {
    private Sets() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static <T, R> Set<R> map(Set<T> set, Function<T, R> mapper) {
        checkNotNull(set, "set");
        checkNotNull(mapper, "mapper");
        return set.stream()
                .map(mapper)
                .collect(toUnmodifiableSet());
    }

    public static <T> Set<T> filter(Set<T> set, Predicate<T> predicate) {
        checkNotNull(set, "set");
        checkNotNull(predicate, "predicate");
        return set.stream()
                .filter(predicate)
                .collect(toUnmodifiableSet());
    }

    public static boolean isNullOrEmpty(@Nullable Set<?> set) {
        return set == null || set.isEmpty();
    }

    public static boolean isNotEmpty(@Nullable Set<?> set) {
        return set != null && !set.isEmpty();
    }

    public static <T> Set<T> nullToEmpty(@Nullable Set<T> set) {
        return set == null ? Set.of() : set;
    }

    public static <T> Set<T> toSetOrEmpty(@Nullable T[] array) {
        return array == null || array.length == 0
                ? Set.of()
                : Set.of(array);
    }

    @Nullable
    public static <T> Set<T> emptyToNull(@Nullable Set<T> set) {
        return set == null || set.isEmpty() ? null : set;
    }

    @Nullable
    public static <T> T anyItemOrNull(@Nullable Set<T> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    public static <T> T anyItem(Set<T> set) {
        checkNotEmpty(set, "set");
        return set.iterator().next();
    }

    public static <T> Set<T> add(Set<T> set, T element) {
        checkNotNull(set, "set");
        checkNotNull(element, "element");
        Set<T> result = new LinkedHashSet<>(set);
        result.add(element);
        return unmodifiableSet(result);
    }

    public static <T> Set<T> addAll(Set<T> set, Collection<T> others) {
        checkNotNull(set, "set");
        checkNotNull(others, "others");
        Set<T> result = new LinkedHashSet<>(set);
        result.addAll(others);
        return unmodifiableSet(result);
    }

    public static <T> Set<T> remove(Set<T> set, T element) {
        checkNotNull(set, "set");
        checkNotNull(element, "element");
        Set<T> result = new LinkedHashSet<>(set);
        result.remove(element);
        return unmodifiableSet(result);
    }

    public static <T> Set<T> removeAll(Set<T> set, Collection<T> others) {
        checkNotNull(set, "set");
        checkNotNull(others, "others");
        Set<T> result = new LinkedHashSet<>(set);
        result.removeAll(others);
        return unmodifiableSet(result);
    }

    public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        checkNotNull(setA, "setA");
        checkNotNull(setB, "setB");
        return setA.stream()
                .filter(setB::contains)
                .collect(toUnmodifiableSet());
    }

    public static <T> Set<T> disjunction(Set<T> setA, Set<T> setB) {
        checkNotNull(setA, "setA");
        checkNotNull(setB, "setB");
        Set<T> result = new LinkedHashSet<>(setA);
        result.addAll(setB);
        result.removeAll(intersection(setA, setB));
        return unmodifiableSet(result);
    }

    public static <T> Sets.SetBuilder<T> builder() {
        return new Sets.SetBuilder<>();
    }

    public static <T> Sets.SetBuilder<T> builder(int size) {
        return new Sets.SetBuilder<>(size);
    }

    public static <T> Sets.SetBuilder<T> builderWith(T item) {
        return new Sets.SetBuilder<T>()
                .add(item);
    }

    public static class SetBuilder<T> {
        private final LinkedHashSet<T> set;

        private SetBuilder(int size) {
            set = new LinkedHashSet<>(size);
        }

        private SetBuilder() {
            set = new LinkedHashSet<>();
        }

        public Sets.SetBuilder<T> add(T value) {
            set.add(value);
            return this;
        }

        public Set<T> build() {
            return unmodifiableSet(set);
        }
    }
}
