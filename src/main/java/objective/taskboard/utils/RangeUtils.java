package objective.taskboard.utils;

import org.apache.commons.lang3.Range;

public class RangeUtils {

    @SuppressWarnings("unchecked")
    public static <T extends P, P extends Comparable<P>> Range<T> expand(Range<T> r1, Range<T> r2) {
        final T min = r1.getMinimum().compareTo(r2.getMinimum()) < 0 ? r1.getMinimum() : r2.getMinimum();
        final T max = r1.getMaximum().compareTo(r2.getMaximum()) > 0 ? r1.getMaximum() : r2.getMaximum();
        return (Range<T>) Range.between(min, max);
    }

    @SuppressWarnings("unchecked")
    public static <T extends P, P extends Comparable<P>> Range<T> between(T fromInclusive, T toInclusive) {
        return (Range<T>) Range.between(fromInclusive, toInclusive);
    }

}
