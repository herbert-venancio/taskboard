package objective.taskboard.monitor;

class MonitorUtils {

    public static String removeDecimal(double expectedScopeWithRisk, String suffix) {
        return String.format("%.0f", expectedScopeWithRisk) + suffix;
    }

}
