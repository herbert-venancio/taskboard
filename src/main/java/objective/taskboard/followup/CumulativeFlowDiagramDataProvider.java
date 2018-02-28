package objective.taskboard.followup;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Component
public class CumulativeFlowDiagramDataProvider {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private FollowUpDataProviderFromCurrentState followUpDataProviderFromCurrentState;

    public CumulativeFlowDiagramDataSet getCumulativeFlowDiagramDataSet(String project, String level) {
        if(!belongsToAnyProject(project))
            throw new IllegalArgumentException(String.format("Unknown project <%s>", project));

        FollowUpDataSnapshot followupData = followUpDataProviderFromCurrentState.getJiraData(project);
        return transform(followupData.getData(), Level.valueOf(level.toUpperCase()));
    }

    enum Level {
        ALL(-1)
        , DEMAND(0)
        , FEATURE(1)
        , SUBTASK(2);

        public final int index;

        Level(int index) {
            this.index = index;
        }

        public boolean includeLevel(int index) {
            return this == ALL || this.index == index;
        }
    }

    private CumulativeFlowDiagramDataSet transform(FollowupData followupData, Level selectedLevel) {
        LinkedHashMap<String, List<CumulativeFlowDiagramDataPoint>> dataByStatus = new LinkedHashMap<>();

        for (int i = 0; i < followupData.syntheticsTransitionsDsList.size(); ++i) {
            if(!selectedLevel.includeLevel(i))
                continue;
            SyntheticTransitionsDataSet ds = followupData.syntheticsTransitionsDsList.get(i);
            int initial = ds.getInitialIndexStatusHeaders();
            
            for (SyntheticTransitionsDataRow row : ds.rows) {
                for (int rowIndex = row.amountOfIssueInStatus.size() - 1; rowIndex >= 0; --rowIndex) {
                    String status = ds.headers.get(rowIndex + initial);
                    if (!dataByStatus.containsKey(status))//NOSONAR
                        dataByStatus.put(status, new LinkedList<CumulativeFlowDiagramDataPoint>());
                    
                    int count = row.amountOfIssueInStatus.get(rowIndex);
                    dataByStatus.get(status).add(new CumulativeFlowDiagramDataPoint(row.issueType, row.date, count));
                }
            }
        }

        return new CumulativeFlowDiagramDataSet(dataByStatus);
    }

    private boolean belongsToAnyProject(String projectKey) {
        return projectRepository.exists(projectKey);
    }
}
