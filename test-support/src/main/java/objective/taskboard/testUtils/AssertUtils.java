package objective.taskboard.testUtils;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.function.Function;

public abstract class AssertUtils {

    public static <T> String collectionToString(Collection<T> list, Function<T, String> itemToString, String delimiter) {
        if (list == null)
            return "<null>";
        else if (list.isEmpty())
            return "<empty>";
        else
            return list.stream().map(itemToString).collect(joining(delimiter));
    }

    public static String collectionToString(Collection<String> list, String delimiter) {
        return collectionToString(list, Function.identity(), delimiter);
    }
}
