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
    public final List<SyntheticTransitionsDataSet> syntheticsTransitionsDsList;

    public FollowupData(FromJiraDataSet fromJiraDs, List<AnalyticsTransitionsDataSet> analyticsTransitionsDsList, List<SyntheticTransitionsDataSet> syntheticsTransitionsDsList) {
        this(Version.VERSION_1, fromJiraDs, analyticsTransitionsDsList, syntheticsTransitionsDsList);
    }

    public FollowupData(Version version, FromJiraDataSet fromJiraDs, List<AnalyticsTransitionsDataSet> analyticsTransitionsDsList, List<SyntheticTransitionsDataSet> syntheticsTransitionsDsList) {
        this.followupDataVersion = version;
        this.fromJiraDs = fromJiraDs;
        this.analyticsTransitionsDsList = analyticsTransitionsDsList;
        this.syntheticsTransitionsDsList = syntheticsTransitionsDsList;
    }
}