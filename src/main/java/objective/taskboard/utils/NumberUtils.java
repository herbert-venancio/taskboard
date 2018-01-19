package objective.taskboard.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static java.util.Arrays.stream;

public class NumberUtils {

    public static double cap(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double linearInterpolation(double min, double max, double factor) {
        double cappedFactor = cap(factor, 0.0, 1.0);
        return min + (max - min) * cappedFactor;
    }

    /**
     * Source: http://www.analyzemath.com/statistics/linear_regression.html
     * @param samples
     * @return
     */
    public static LineModel linearRegression(Point2D... samples) {
        if(samples.length == 0)
            return new LineModel(0.0, 0.0);
        if(samples.length == 1)
            return new LineModel(0.0, samples[0].y);

        double sumX = stream(samples).mapToDouble(s -> s.x).sum();
        double sumY = stream(samples).mapToDouble(s -> s.y).sum();
        double sumXY = stream(samples).mapToDouble(s -> s.x * s.y).sum();
        double sumXSquared = stream(samples).mapToDouble(s -> s.x * s.x).sum();
        double n = samples.length;
        // formula:
        //   a = (n*∑xy - ∑x*∑y) / (n*∑x² - (∑x)²)
        //   b = (1/n)*(∑y - a*∑x)
        //   y = a*x + b
        double a = (n*sumXY - sumX*sumY) / (n*sumXSquared - sumX * sumX);
        double b = (1.0/n)*(sumY - a*sumX);
        return new LineModel(a, b);
    }

    public static class FormattedNumberSerializer extends JsonSerializer<Double> {
        DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeRawValue(df.format(value));
        }
    }

    public static class Point2D {
        public final double x;
        public final double y;

        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class LineModel {
        public final double a;
        public final double b;

        public LineModel(double a, double b) {
            this.a = a;
            this.b = b;
        }

        public double y(double x) {
            return a*x + b;
        }
    }
}
