package objective.taskboard.utils;

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class StreamUtils {

    public static <T> Stream<T> streamOf(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> Function<Object, Stream<T>> instancesOf(Class<T> cls) {
        return o -> cls.isInstance(o) ? Stream.of(cls.cast(o)) : Stream.empty();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static <T> Collector<T, ?, Set<T>> toLinkedHashSet() {
        return toCollection(LinkedHashSet::new);
    }

    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(
            Function<? super T, ? extends K> keyMapper
            , Function<? super T, ? extends U> valueMapper) {
        return toLinkedHashMap(
                keyMapper,
                valueMapper,
                throwingMerger());
    }

    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(
            Function<? super T, ? extends K> keyMapper
            , Function<? super T, ? extends U> valueMapper
            , BinaryOperator<U> mergeFunction) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                mergeFunction,
                LinkedHashMap::new
        );
    }

    public static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

    public static <T> BinaryOperator<T> returnFirstMerger() {
        return (a, b) -> a;
    }
}
