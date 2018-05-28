package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static objective.taskboard.followup.FixedFollowUpSnapshotValuesProvider.emptyValuesProvider;
import static objective.taskboard.followup.FollowUpHelper.getFollowupData;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.followup.cluster.FollowupClusterImpl;

public class FollowUpSnapshotTest {
    
    private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
    
    private FollowupCluster cluster = new FollowupClusterImpl(asList(
            new FollowUpClusterItem(project, "UX", "na", "S", 1.0, 0.0),
            new FollowUpClusterItem(project, "UX", "na", "M", 2.0, 0.0)));
    
    @Test
    public void shouldReturnEffortHistoryRow() {
        FollowUpTimeline timeline = new FollowUpTimeline(LocalDate.parse("2018-04-10"));

        FollowUpData data = getFollowupData(
                dataRow("UX", "Open", "M"),
                dataRow("UX", "Open", "S"),
                dataRow("UX", "Done", "S"),
                dataRow("UX", "Done", "S"));

        FollowUpSnapshot subject = new FollowUpSnapshot(timeline, data, cluster, emptyValuesProvider());
        EffortHistoryRow effortHistoryRow = subject.getEffortHistoryRow();

        assertEquals(LocalDate.parse("2018-04-10"), effortHistoryRow.date);
        assertEquals(2.0, effortHistoryRow.sumEffortDone, 0.0);
        assertEquals(3.0, effortHistoryRow.sumEffortBacklog, 0.0);
    }
    
    @Test
    public void shouldReturnEffortHistory() {
        FollowUpTimeline timeline = new FollowUpTimeline(LocalDate.parse("2018-04-10"));
        List<EffortHistoryRow> effortHistory = asList(
                new EffortHistoryRow(LocalDate.parse("2018-04-03"), 2d, 8d),
                new EffortHistoryRow(LocalDate.parse("2018-04-04"), 3d, 7d));
        
        FollowUpData data = getFollowupData(
                dataRow("UX", "Open", "M"),
                dataRow("UX", "Open", "S"),
                dataRow("UX", "Done", "S"));

        
        FollowUpSnapshot subject = new FollowUpSnapshot(timeline, data, cluster, new FixedFollowUpSnapshotValuesProvider().effortHistory(effortHistory));
        
        assertEffortHistory(subject.getEffortHistory(), 
                "2018-04-03 | 2.0 | 8.0",
                "2018-04-04 | 3.0 | 7.0",
                "2018-04-10 | 1.0 | 3.0");
    }

    private static void assertEffortHistory(List<EffortHistoryRow> actual, String... expectedRows) {
        assertEquals(
                StringUtils.join(expectedRows, "\n"), 
                actual.stream().map(r -> r.date + " | " + r.sumEffortDone + " | " + r.sumEffortBacklog).collect(joining("\n")));
    }
    
    private static FromJiraDataRow dataRow(String subtaskType, String subtaskStatus, String tshirtSize) {
        FromJiraDataRow row = new FromJiraDataRow();
        row.subtaskStatus = subtaskStatus;
        row.subtaskType = subtaskType;
        row.tshirtSize = tshirtSize;
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        
        return row;
    }
}
