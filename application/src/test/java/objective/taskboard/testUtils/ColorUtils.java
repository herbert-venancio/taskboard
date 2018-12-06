package objective.taskboard.testUtils;

import static java.lang.Integer.toHexString;

public class ColorUtils {

    public static final String rgbToHex(String color) {
        String[] numbers = color.replace("rgb(", "").replace(")", "").split(",");
        int r = Integer.parseInt(numbers[0].trim());
        int g = Integer.parseInt(numbers[1].trim());
        int b = Integer.parseInt(numbers[2].trim());
        return "#" + toHexString(r) + toHexString(g) + toHexString(b);
    }

}
