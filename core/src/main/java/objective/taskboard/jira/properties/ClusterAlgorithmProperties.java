package objective.taskboard.jira.properties;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import objective.taskboard.cluster.algorithm.ClusterAlgorithmRequest;

@Component
@ConfigurationProperties(prefix = "cluster-algorithm")
@Validated
public class ClusterAlgorithmProperties {

    private Defaults defaults = new Defaults();

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    public static class Defaults {

        @NotNull
        private List<String> projects = Collections.emptyList();
        @NotNull
        private List<Long> featureIssueTypes = Collections.emptyList();
        @NotNull
        private List<Long> bugIssueTypes = Collections.emptyList();
        @NotNull
        private List<Long> featureDoneStatuses = Collections.emptyList();
        @NotNull
        private List<Long> subtaskDoneStatuses = Collections.emptyList();
        @NotNull
        private ClusterAlgorithmRequest.CycleStatuses cycleStatuses = new ClusterAlgorithmRequest.CycleStatuses();
        @NotNull
        private ClusterAlgorithmRequest.ClusteringType clusteringType = ClusterAlgorithmRequest.ClusteringType.EFFORT_AND_CYCLE;
        @NotNull
        private ClusterAlgorithmRequest.DateRange dateRange = new ClusterAlgorithmRequest.DateRange();

        public List<String> getProjects() {
            return projects;
        }

        public void setProjects(List<String> projects) {
            this.projects = projects;
        }

        public List<Long> getFeatureIssueTypes() {
            return featureIssueTypes;
        }

        public void setFeatureIssueTypes(List<Long> featureIssueTypes) {
            this.featureIssueTypes = featureIssueTypes;
        }

        public List<Long> getBugIssueTypes() {
            return bugIssueTypes;
        }

        public void setBugIssueTypes(List<Long> bugIssueTypes) {
            this.bugIssueTypes = bugIssueTypes;
        }

        public List<Long> getFeatureDoneStatuses() {
            return featureDoneStatuses;
        }

        public void setFeatureDoneStatuses(List<Long> featureDoneStatuses) {
            this.featureDoneStatuses = featureDoneStatuses;
        }

        public List<Long> getSubtaskDoneStatuses() {
            return subtaskDoneStatuses;
        }

        public void setSubtaskDoneStatuses(List<Long> subtaskDoneStatuses) {
            this.subtaskDoneStatuses = subtaskDoneStatuses;
        }

        public ClusterAlgorithmRequest.CycleStatuses getCycleStatuses() {
            return cycleStatuses;
        }

        public void setCycleStatuses(ClusterAlgorithmRequest.CycleStatuses cycleStatuses) {
            this.cycleStatuses = cycleStatuses;
        }

        public ClusterAlgorithmRequest.ClusteringType getClusteringType() {
            return clusteringType;
        }

        public void setClusteringType(ClusterAlgorithmRequest.ClusteringType clusteringType) {
            this.clusteringType = clusteringType;
        }

        public ClusterAlgorithmRequest.DateRange getDateRange() {
            return dateRange;
        }

        public void setDateRange(ClusterAlgorithmRequest.DateRange dateRange) {
            this.dateRange = dateRange;
        }
    }
}
