package objective.taskboard.followup;

import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.stream.Collectors.toList;

@Component
public class CumulativeFlowDiagramDataProvider {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private FollowUpDataProviderFromCurrentState followUpDataProviderFromCurrentState;

    public CumulativeFlowDiagramDataSet getCumulativeFlowDiagramDataSet(String project) {
        if(!belongsToAnyProject(project))
            throw new IllegalArgumentException(String.format("Unknown project <%s>", project));

        FollowUpDataSnapshot followupData = followUpDataProviderFromCurrentState.getJiraData(project);
        return transform(followupData.getData(), Lane.SUBTASK);
    }

    enum Lane {
        ALL(-1)
        , DEMAND(0)
        , FEATURE(1)
        , SUBTASK(2);

        public final int index;

        Lane(int index) {
            this.index = index;
        }

        public boolean includeLane(int index) {
            return this == ALL || this.index == index;
        }
    }

    private CumulativeFlowDiagramDataSet transform(FollowupData followupData, Lane selectedLane) {
        Set<String> lanes = new LinkedHashSet<>();
        Set<String> types = new LinkedHashSet<>();
        Set<String> labels = new LinkedHashSet<>();
        Set<ZonedDateTime> dates = new TreeSet<>();

        for (int i = 0; i < followupData.syntheticsTransitionsDsList.size(); ++i) {
            if(!selectedLane.includeLane(i))
                continue;
            SyntheticTransitionsDataSet ds = followupData.syntheticsTransitionsDsList.get(i);
            lanes.add(ds.issueType);
            int initial = ds.headers.size() - ds.rows.get(0).amountOfIssueInStatus.size();
            for (int headerIndex = ds.headers.size() - 1; headerIndex >= initial; --headerIndex) {
                labels.add(ds.headers.get(headerIndex));
            }
            for (SyntheticTransitionsDataRow row : ds.rows) {
                types.add(row.issueType);
                dates.add(row.date);
            }
        }

        List<String> laneList = new LinkedList<>(lanes);
        List<String> typeList = new LinkedList<>(types);
        List<String> labelList = new LinkedList<>(labels);
        List<ZonedDateTime> dateList = new LinkedList<>(dates);
        List<CumulativeFlowDiagramDataPoint> data = new LinkedList<>();

        for (int i = 0; i < followupData.syntheticsTransitionsDsList.size(); ++i) {
            if(!selectedLane.includeLane(i))
                continue;
            SyntheticTransitionsDataSet ds = followupData.syntheticsTransitionsDsList.get(i);
            int initial = ds.headers.size() - ds.rows.get(0).amountOfIssueInStatus.size();
            int lane = laneList.indexOf(ds.issueType);
            for (SyntheticTransitionsDataRow row : ds.rows) {
                int type = typeList.indexOf(row.issueType);
                int index = dateList.indexOf(row.date);

                for (int rowIndex = row.amountOfIssueInStatus.size() - 1; rowIndex >= 0; --rowIndex) {
                    int label = labelList.indexOf(ds.headers.get(rowIndex + initial));
                    int count = sum(row.amountOfIssueInStatus, 0, rowIndex);
                    data.add(new CumulativeFlowDiagramDataPoint(lane, type, label, index, count));
                }
            }
        }

        return new CumulativeFlowDiagramDataSet(
                laneList
                , typeList
                , labelList
                , dateList.stream().map(zdt -> Date.from(zdt.toInstant())).collect(toList())
                , data);
    }

    private boolean belongsToAnyProject(String projectKey) {
        return projectRepository.exists(projectKey);
    }

    private static int sum(List<Integer> amountOfIssueInStatus, int start, int end) {
        int sum = 0;
        for(int i = start; i <= end; ++i) {
            sum += amountOfIssueInStatus.get(i);
        }
        return sum;
    }
}
