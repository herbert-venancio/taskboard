package objective.taskboard.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class NumberUtilsTest {

    @Test
    public void linearRegression1() {
        // given
        NumberUtils.Point2D sample1 = new NumberUtils.Point2D(-2, -1);
        NumberUtils.Point2D sample2 = new NumberUtils.Point2D(1, 1);
        NumberUtils.Point2D sample3 = new NumberUtils.Point2D(3, 2);

        // when
        NumberUtils.LineModel result = NumberUtils.linearRegression(sample1, sample2, sample3);

        // then
        assertThat(result.a).isEqualTo(23.0/38.0, within(0.000001));
        assertThat(result.b).isEqualTo(5.0/19.0, within(0.000001));
    }

    @Test
    public void linearRegression2() {
        // given
        NumberUtils.Point2D sample1 = new NumberUtils.Point2D(-1, 0);
        NumberUtils.Point2D sample2 = new NumberUtils.Point2D(0, 2);
        NumberUtils.Point2D sample3 = new NumberUtils.Point2D(1, 4);
        NumberUtils.Point2D sample4 = new NumberUtils.Point2D(2, 5);

        // when
        NumberUtils.LineModel result = NumberUtils.linearRegression(sample1, sample2, sample3, sample4);

        // then
        assertThat(result.a).isEqualTo(1.7, within(0.000001));
        assertThat(result.b).isEqualTo(1.9, within(0.000001));
    }

    @Test
    public void twoSamples() {
        // given
        NumberUtils.Point2D sample1 = new NumberUtils.Point2D(2, 0);
        NumberUtils.Point2D sample2 = new NumberUtils.Point2D(4, 1);

        // when
        NumberUtils.LineModel result = NumberUtils.linearRegression(sample1, sample2);

        // then
        assertThat(result.a).isEqualTo(0.5, within(0.000001));
        assertThat(result.b).isEqualTo(-1.0, within(0.000001));
    }

    @Test
    public void singleSample() {
        // given
        NumberUtils.Point2D sample1 = new NumberUtils.Point2D(1, 1);

        // when
        NumberUtils.LineModel result = NumberUtils.linearRegression(sample1);

        // then
        assertThat(result.a).isEqualTo(0, within(0.000001));
        assertThat(result.b).isEqualTo(1, within(0.000001));
    }

    @Test
    public void noSamples() {
        // given
        NumberUtils.Point2D[] samples = new NumberUtils.Point2D[0];

        // when
        NumberUtils.LineModel result = NumberUtils.linearRegression(samples);

        // then
        assertThat(result.a).isEqualTo(0, within(0.000001));
        assertThat(result.b).isEqualTo(0, within(0.000001));
    }
}
