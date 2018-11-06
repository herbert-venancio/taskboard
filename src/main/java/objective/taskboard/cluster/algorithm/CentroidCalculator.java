package objective.taskboard.cluster.algorithm;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import org.apache.commons.lang3.Range;

public class CentroidCalculator<T> {

    private Map<String, Dimension<T>> dimensions = new LinkedHashMap<>();

    /**
     * Extract value for axis represented by "name" using valueExtractor and converts to 0..1 scale based on range
     * @param name name of the axis
     * @param valueExtractor extract axis dimension value from data point
     * @param range range with minimum and maximum value expected
     */
    public void addLinearDimension(String name, ToDoubleFunction<T> valueExtractor, Range<Double> range) {
        double min = range.getMinimum();
        double max = range.getMaximum();
        double scale = max - min;
        this.dimensions.put(name, new Dimension<>(valueExtractor, v -> (v - min) / scale));
    }

    /**
     * Extract value for axis represented by "name" using valueExtractor
     * @param name name of the axis
     * @param valueExtractor extract axis dimension value from data point
     */
    public void addDimension(String name, ToDoubleFunction<T> valueExtractor) {
        this.dimensions.put(name, new Dimension<>(valueExtractor));
    }

    public Centroid calculate(T point) {
        return calculate(singletonList(point))
                .orElseThrow(() -> new RuntimeException("Unexpected empty returned"));
    }

    public Optional<Centroid> calculate(List<T> points) {
        if(points.isEmpty())
            return Optional.empty();

        int length = dimensions.size();
        if(length == 0)
            throw new RuntimeException("Empty centroid calculator");

        Set<String> axes = dimensions.keySet();
        double[] values = new double[length];
        double[] scaledValues = new double[length];
        double value;

        // calculate average absolute position
        int i = 0;
        for(Dimension<T> dimension : dimensions.values()) {
            values[i] = points.stream().mapToDouble(dimension.valueExtractor).average().orElse(0.0);
            ++i;
        }
        // apply scale to average absolute position
        i = 0;
        for(Dimension<T> dimension : dimensions.values()) {
            scaledValues[i] = dimension.valueScale.applyAsDouble(values[i]);
            ++i;
        }
        // calculate
        if(length == 1) {
            value = scaledValues[0];
        } else {
            value = Math.sqrt(
                    Arrays.stream(scaledValues)
                            .map(v -> v * v)
                            .sum()
            );
        }
        return Optional.of(new Centroid(axes, values, scaledValues, value));
    }

    public static class Dimension<T> implements ToDoubleFunction<T> {
        public final ToDoubleFunction<T> valueExtractor;
        public final DoubleUnaryOperator valueScale;

        public Dimension(ToDoubleFunction<T> valueExtractor) {
            this(valueExtractor, DoubleUnaryOperator.identity());
        }

        public Dimension(ToDoubleFunction<T> valueExtractor, DoubleUnaryOperator valueScale) {
            this.valueExtractor = valueExtractor;
            this.valueScale = valueScale;
        }

        @Override
        public double applyAsDouble(T value) {
            return valueScale.applyAsDouble(valueExtractor.applyAsDouble(value));
        }
    }
}
