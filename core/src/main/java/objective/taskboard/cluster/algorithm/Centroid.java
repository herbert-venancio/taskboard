package objective.taskboard.cluster.algorithm;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;

import javax.validation.constraints.NotNull;


public class Centroid implements Comparable<Centroid> {

    public final Set<String> axes;
    public final double[] values;
    public final double[] scaledValues;
    public final double value;

    public Centroid(Set<String> axes, double[] values, double[] scaledValues, double value) {
        this.axes = axes;
        this.values = values;
        this.scaledValues = scaledValues;
        this.value = value;
    }

    @Override
    public int compareTo(@NotNull Centroid o) {
        return Double.compare(value, o.value);
    }

    public double distance(Centroid o) {
        int length = values.length;
        if(length == 1)
            return Math.abs(values[0] - o.values[0]);
        return Math.sqrt(
                IntStream.range(0, values.length)
                        .mapToDouble(i -> values[i] - o.values[i])
                        .map(v -> v * v)
                        .sum()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Centroid centroid = (Centroid) o;
        return Arrays.equals(scaledValues, centroid.scaledValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(scaledValues);
    }
}
