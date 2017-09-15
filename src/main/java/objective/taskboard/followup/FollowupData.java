package objective.taskboard.followup;

import java.util.List;

public class FollowupData {

    public enum Version {

        VERSION_1("1");

        public final String value;

        Version(String value) {
            this.value = value;
        }
    }

    public final Version followupDataVersion;
    public final FromJiraDataSet fromJiraDs;
    public final List<AnalyticsTransitionsDataSet> analyticsTransitionsDsList;
    public final List<SyntheticTransitionsDataSet> cfdTransitionsDsList;

    public FollowupData(FromJiraDataSet fromJiraDs, List<AnalyticsTransitionsDataSet> analyticsTransitionsDsList, List<SyntheticTransitionsDataSet> cfdTransitionsDsList) {
        this(Version.VERSION_1, fromJiraDs, analyticsTransitionsDsList, cfdTransitionsDsList);
    }

    public FollowupData(Version version, FromJiraDataSet fromJiraDs, List<AnalyticsTransitionsDataSet> analyticsTransitionsDsList, List<SyntheticTransitionsDataSet> cfdTransitionsDsList) {
        this.followupDataVersion = version;
        this.fromJiraDs = fromJiraDs;
        this.analyticsTransitionsDsList = analyticsTransitionsDsList;
        this.cfdTransitionsDsList = cfdTransitionsDsList;
    }

    @Override
    public String toString() {
        return "FollowupData [followupDataVersion=" + followupDataVersion + ", fromJiraDs=" + fromJiraDs
                + ", analyticsTransitionsDsList=" + analyticsTransitionsDsList + ", cfdTransitionsDsList="
                + cfdTransitionsDsList + "]";
    }
}