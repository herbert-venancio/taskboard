package objective.taskboard.utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class NumberUtils {

    public static final double DEFAULT_EQUALS_THRESHOLD = 1e-15;

    public static double cap(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double linearInterpolation(double min, double max, double factor) {
        double cappedFactor = cap(factor, 0.0, 1.0);
        return min + (max - min) * cappedFactor;
    }

    public static boolean numberEquals(double a, double b) {
        return numberEquals(a, b, DEFAULT_EQUALS_THRESHOLD);
    }

    public static boolean numberEquals(double a, double b, double threshold) {
        return Math.abs(a - b) < threshold;
    }

    public static class FormattedNumberSerializer extends JsonSerializer<Double> {
        DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeRawValue(df.format(value));
        }
    }
}
