package objective.taskboard.followup.kpi.touchtime;

import java.time.Instant;
import java.util.List;

public class TouchTimeByIssueKpiDataPoint {
    public final String issueKey;
    public final String issueType;
    public final Instant startProgressingDate;
    public final Instant endProgressingDate;
    public final List<Stack> stacks;

    public TouchTimeByIssueKpiDataPoint(String issueKey, String issueType, Instant startProgressingDate, Instant endProgressingDate, List<Stack> stacks) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        this.stacks = stacks;
        this.startProgressingDate = startProgressingDate;
        this.endProgressingDate = endProgressingDate;
    }

    public static class Stack {
        public final String stackName;
        public final double effortInHours;
        public Stack(String stackName, double effortInHours) {
            this.stackName = stackName;
            this.effortInHours = effortInHours;
        }
    }
}