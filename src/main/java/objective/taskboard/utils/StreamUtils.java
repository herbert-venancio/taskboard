package objective.taskboard.utils;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class StreamUtils {

    public static <T> Stream<T> streamOf(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> Function<Object, Stream<T>> instancesOf(Class<T> cls) {
        return o -> cls.isInstance(o) ? Stream.of(cls.cast(o)) : Stream.empty();
    }
}
