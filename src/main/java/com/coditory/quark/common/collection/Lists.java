package com.coditory.quark.common.collection;

import com.coditory.quark.common.util.Objects;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.coditory.quark.common.check.Args.checkNotEmpty;
import static com.coditory.quark.common.check.Args.checkNotNull;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableList;

public final class Lists {
    private Lists() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        checkNotNull(list, "list");
        checkNotNull(mapper, "mapper");
        return list.stream()
                .map(mapper)
                .collect(toUnmodifiableList());
    }

    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        checkNotNull(list, "list");
        checkNotNull(predicate, "predicate");
        return list.stream()
                .filter(predicate)
                .collect(toUnmodifiableList());
    }

    public static <T> List<T> nullToEmpty(@Nullable List<T> list) {
        return list == null ? List.of() : list;
    }

    @Nullable
    public static <T> List<T> emptyToNull(@Nullable List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }

    public static boolean isNullOrEmpty(@Nullable List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNotEmpty(@Nullable List<?> list) {
        return list != null && !list.isEmpty();
    }

    public static <T> List<T> toListOrEmpty(@Nullable T[] array) {
        return array == null || array.length == 0
                ? List.of()
                : List.of(array);
    }

    @Nullable
    public static <T> T lastOrNull(@Nullable List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public static <T> T last(List<T> list) {
        checkNotEmpty(list, "list");
        return list.get(list.size() - 1);
    }

    public static <T> List<T> reverse(List<T> list) {
        checkNotNull(list, "list");
        List<T> result = new ArrayList<>(list);
        Collections.reverse(result);
        return unmodifiableList(result);
    }

    public static <T> List<T> shuffle(List<T> list) {
        checkNotNull(list, "list");
        List<T> result = new ArrayList<>(list);
        Collections.shuffle(list);
        return unmodifiableList(result);
    }

    public static <T> List<T> shuffle(List<T> list, Random random) {
        checkNotNull(list, "list");
        checkNotNull(random, "random");
        List<T> result = new ArrayList<>(list);
        Collections.shuffle(list, random);
        return unmodifiableList(result);
    }

    @Nullable
    public static <T> T firstOrNull(@Nullable List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public static <T> T first(List<T> list) {
        checkNotEmpty(list, "list");
        return list.get(0);
    }

    public static <T> List<T> distinct(List<T> list) {
        checkNotNull(list, "list");
        HashSet<T> set = new LinkedHashSet<>(list);
        return List.copyOf(set);
    }

    public static <T> List<T> addAll(List<T> list, Collection<T> elements) {
        checkNotNull(list, "list");
        checkNotNull(elements, "elements");
        List<T> result = new ArrayList<>(list);
        result.addAll(elements);
        return unmodifiableList(result);
    }

    public static <T> List<T> addAllMissing(List<T> list, Collection<T> elements) {
        checkNotNull(list, "list");
        checkNotNull(elements, "elements");
        List<T> result = new ArrayList<>(list.size() + elements.size());
        result.addAll(list);
        HashSet<T> set = new LinkedHashSet<>(list);
        for (T element : elements) {
            if (!set.contains(element)) {
                result.add(element);
            }
        }
        return unmodifiableList(result);
    }

    public static <T> List<T> add(List<T> list, T element) {
        checkNotNull(list, "list");
        checkNotNull(element, "element");
        List<T> result = new ArrayList<>(list);
        result.add(element);
        return unmodifiableList(result);
    }

    public static <T> List<T> addMissing(List<T> list, T element) {
        checkNotNull(list, "list");
        checkNotNull(element, "element");
        return list.contains(element)
                ? list
                : add(list, element);
    }

    public static <T> List<T> remove(List<T> list, T element) {
        checkNotNull(list, "list");
        checkNotNull(element, "element");
        List<T> result = new ArrayList<>(list);
        result.remove(element);
        return unmodifiableList(result);
    }

    public static <T> List<T> removeAll(List<T> list, Collection<T> elements) {
        checkNotNull(list, "list");
        checkNotNull(elements, "elements");
        List<T> result = new ArrayList<>(list);
        result.removeAll(elements);
        return unmodifiableList(result);
    }

    public static <T> List<T> intersection(List<T> listA, List<T> listB) {
        checkNotNull(listA, "listA");
        checkNotNull(listB, "listB");
        Set<T> setB = new HashSet<>(listB);
        return listA.stream()
                .filter(setB::contains)
                .collect(toUnmodifiableList());
    }

    public static <T> List<T> disjunction(List<T> listA, List<T> listB) {
        checkNotNull(listA, "listA");
        checkNotNull(listB, "listB");
        List<T> result = new ArrayList<>(listA);
        result.addAll(listB);
        result.removeAll(intersection(listA, listB));
        return unmodifiableList(result);
    }

    public static <T> boolean equalsIgnoreOrder(List<T> listA, List<T> listB) {
        if (listA == null && listB == null) {
            return true;
        }
        if (listA == null || listB == null) {
            return false;
        }
        if (listA.size() != listB.size()) {
            return false;
        }
        Map<T, Integer> countersA = countItems(listA);
        Map<T, Integer> countersB = countItems(listB);
        return countersA.equals(countersB);
    }

    public static <T> Map<T, Integer> countItems(List<T> list) {
        checkNotNull(list, "list");
        Map<T, Integer> counters = new LinkedHashMap<>(list.size());
        for (T item : list) {
            counters.compute(item, (k, v) -> (v == null) ? 1 : v + 1);
        }
        return counters;
    }

    public static <T> boolean equalsIgnoreOrderAndDuplicates(List<T> listA, List<T> listB) {
        if (listA == null && listB == null) {
            return true;
        }
        if (listA == null || listB == null) {
            return false;
        }
        HashSet<T> setA = new HashSet<>(listA);
        HashSet<T> setB = new HashSet<>(listB);
        return setA.equals(setB);
    }

    public static <T> boolean equalsIgnoreDuplicates(List<T> listA, List<T> listB) {
        if (listA == null && listB == null) {
            return true;
        }
        if (listA == null || listB == null) {
            return false;
        }
        LinkedHashSet<T> setA = new LinkedHashSet<>(listA);
        LinkedHashSet<T> setB = new LinkedHashSet<>(listB);
        if (!setA.equals(setB)) {
            return false;
        }
        Iterator<T> iterA = setA.iterator();
        Iterator<T> iterB = setB.iterator();
        while (iterA.hasNext()) {
            T itemA = iterA.next();
            T itemB = iterB.next();
            if (!Objects.equals(itemA, itemB)) {
                return false;
            }
        }
        return true;
    }

    public static <T> Lists.ListBuilder<T> builder() {
        return new Lists.ListBuilder<>();
    }

    public static <T> Lists.ListBuilder<T> builder(int size) {
        return new Lists.ListBuilder<>(size);
    }

    public static <T> Lists.ListBuilder<T> builderWith(T item) {
        return new Lists.ListBuilder<T>()
                .add(item);
    }

    public static class ListBuilder<T> {
        private final List<T> list;

        private ListBuilder(int size) {
            list = new ArrayList<>(size);
        }

        private ListBuilder() {
            list = new ArrayList<>();
        }

        public Lists.ListBuilder<T> add(T value) {
            list.add(value);
            return this;
        }

        public List<T> build() {
            return unmodifiableList(list);
        }
    }
}
