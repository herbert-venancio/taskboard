package objective.taskboard.utils;

import static objective.taskboard.utils.NumberUtils.numberEquals;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NumberUtilsTest {

    public static final double HIGH_TOLERANCE = 0.1;
    public static final double LOW_TOLERANCE = 0.001;

    @Test
    public void givenHighDifference_givenHighTolerance_whenNumberEquals_shouldBeTrue() {
        double first = 0.5;
        double second = 0.55;

        assertThat(first).isNotEqualTo(second);
        assertThat(numberEquals(first, second, HIGH_TOLERANCE)).isTrue();
    }

    @Test
    public void givenHighDifference_givenLowTolerance_whenNumberEquals_shouldBeFalse() {
        double first = 0.5;
        double second = 0.55;

        assertThat(first).isNotEqualTo(second);
        assertThat(numberEquals(first, second, LOW_TOLERANCE)).isFalse();
    }

    @Test
    public void givenLowDifference_givenDefaultTolerance_whenNumberEquals_shouldBeTrue() {
        double first = Math.sin(45.0*Math.PI/180.0);
        double second = Math.sqrt(2.0) / 2.0;

        assertThat(first).isNotEqualTo(second);
        assertThat(numberEquals(first, second)).isTrue();
    }
}
