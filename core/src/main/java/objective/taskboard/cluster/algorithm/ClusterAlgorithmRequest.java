package objective.taskboard.cluster.algorithm;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClusterAlgorithmRequest {

    private List<String> projects = Collections.emptyList();
    private List<Long> featureIssueTypes = Collections.emptyList();
    private List<Long> bugIssueTypes = Collections.emptyList();
    private List<Long> featureDoneStatuses = Collections.emptyList();
    private List<Long> subtaskDoneStatuses = Collections.emptyList();
    private CycleStatuses subtaskCycleStatuses = null;
    private ClusterGrouping clusterGrouping = ClusterGrouping.BALLPARK;
    private ClusteringType clusteringType = ClusteringType.EFFORT_AND_CYCLE;
    private DateRange dateRange = new DateRange();

    public ClusterAlgorithmRequest() {
    }

    public ClusterAlgorithmRequest(List<String> projects, List<Long> featureIssueTypes, List<Long> bugIssueTypes, List<Long> featureDoneStatuses, List<Long> subtaskDoneStatuses, CycleStatuses subtaskCycleStatuses, ClusteringType clusteringType) {
        this.projects = projects;
        this.featureIssueTypes = featureIssueTypes;
        this.bugIssueTypes = bugIssueTypes;
        this.featureDoneStatuses = featureDoneStatuses;
        this.subtaskDoneStatuses = subtaskDoneStatuses;
        this.subtaskCycleStatuses = subtaskCycleStatuses;
        this.clusteringType = clusteringType;
    }

    public static ClusterAlgorithmRequest fromDefaults(ClusterAlgorithmProperties.Defaults defaults) {
        if (defaults == null)
            return new ClusterAlgorithmRequest();

        return new ClusterAlgorithmRequest(
                defaults.getProjects()
                , defaults.getFeatureIssueTypes()
                , defaults.getBugIssueTypes()
                , defaults.getFeatureDoneStatuses()
                , defaults.getSubtaskDoneStatuses()
                , defaults.getCycleStatuses()
                , defaults.getClusteringType()
        );
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = Optional.ofNullable(projects).orElseGet(Collections::emptyList);
    }

    public List<Long> getFeatureIssueTypes() {
        return featureIssueTypes;
    }

    public void setFeatureIssueTypes(List<Long> featureIssueTypes) {
        this.featureIssueTypes = Optional.ofNullable(featureIssueTypes).orElseGet(Collections::emptyList);
    }

    public List<Long> getBugIssueTypes() {
        return bugIssueTypes;
    }

    public void setBugIssueTypes(List<Long> bugIssueTypes) {
        this.bugIssueTypes = Optional.ofNullable(bugIssueTypes).orElseGet(Collections::emptyList);
    }

    public List<Long> getFeatureDoneStatuses() {
        return featureDoneStatuses;
    }

    public void setFeatureDoneStatuses(List<Long> featureDoneStatuses) {
        this.featureDoneStatuses = Optional.ofNullable(featureDoneStatuses).orElseGet(Collections::emptyList);
    }

    public List<Long> getSubtaskDoneStatuses() {
        return subtaskDoneStatuses;
    }

    public void setSubtaskDoneStatuses(List<Long> subtaskDoneStatuses) {
        this.subtaskDoneStatuses = Optional.ofNullable(subtaskDoneStatuses).orElseGet(Collections::emptyList);
    }

    public CycleStatuses getSubtaskCycleStatuses() {
        return subtaskCycleStatuses;
    }

    public void setSubtaskCycleStatuses(CycleStatuses subtaskCycleStatuses) {
        this.subtaskCycleStatuses = subtaskCycleStatuses;
    }

    public ClusterGrouping getClusterGrouping() {
        return clusterGrouping;
    }

    public void setClusterGrouping(ClusterGrouping clusterGrouping) {
        this.clusterGrouping = clusterGrouping;
    }

    public ClusteringType getClusteringType() {
        return clusteringType;
    }

    public void setClusteringType(ClusteringType clusteringType) {
        this.clusteringType = Optional.ofNullable(clusteringType).orElse(ClusteringType.EFFORT_AND_CYCLE);
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public static class CycleStatuses {
        private long first;
        private long last;

        public long getFirst() {
            return first;
        }

        public void setFirst(long first) {
            this.first = first;
        }

        public long getLast() {
            return last;
        }

        public void setLast(long last) {
            this.last = last;
        }
    }

    public enum ClusterGrouping {
        BALLPARK,
        SUBTASK
    }

    public enum ClusteringType {

        EFFORT_ONLY("Effort only"),
        CYCLE_ONLY("Cycle days only"),
        EFFORT_AND_CYCLE("Square root of (effort² + cycle²)");

        public final String description;

        ClusteringType(String description) {
            this.description = description;
        }
    }

    public static class DateRange {
        private LocalDate startDate;
        private LocalDate endDate;

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }
    }
}
