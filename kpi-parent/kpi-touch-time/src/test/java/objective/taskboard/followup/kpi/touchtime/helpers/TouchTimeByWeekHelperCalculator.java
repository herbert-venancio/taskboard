package objective.taskboard.followup.kpi.touchtime.helpers;

import java.util.LinkedList;
import java.util.List;

public class TouchTimeByWeekHelperCalculator {
    private List<Double> effortsByType = new LinkedList<>();
    private List<Double> effortsByStatus = new LinkedList<>();
    private int totalSelectedIssues;
    private TouchTimeByWeekHelperCalculator () {
    }
    public static TouchTimeByWeekHelperCalculator averageEffortByWeekCalculator() {
        return new TouchTimeByWeekHelperCalculator();
    }
    public TouchTimeByWeekHelperCalculator addEffortByType(double effort) {
        effortsByType.add(effort);
        return this;
    }
    public TouchTimeByWeekHelperCalculator addEffortByStatus(double effort) {
        effortsByType.add(effort);
        return this;
    }
    public TouchTimeByWeekHelperCalculator totalSelectedIssuesInWeek(int totalSelectedIssues) {
        this.totalSelectedIssues = totalSelectedIssues;
        return this;
    }
    public double calculate() {
        if (totalSelectedIssues <= 0)
            return 0;
        return (sumList(effortsByType) + sumList(effortsByStatus)) / totalSelectedIssues;
    }
    private double sumList(List<Double> doubles) {
        return doubles.stream().mapToDouble(d -> d).sum();
    }
}