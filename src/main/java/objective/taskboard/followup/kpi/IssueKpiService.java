package objective.taskboard.followup.kpi;

import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.followup.kpi.transformer.IssueKpiTransformer;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.properties.JiraProperties;

@Service
public class IssueKpiService {
        
    @Autowired
    private IssueBufferService issueBufferService;
    
    @Autowired
    private JiraProperties jiraProperties;
    
    @Autowired
    private IssueKpiDataItemAdapterFactory factory;

    public Map<KpiLevel,List<IssueKpi>> getIssuesFromCurrentState(String projectKey, ZoneId timezone){
        List<Issue> issuesVisibleToUser = issueBufferService.getAllIssues().stream()
                .filter(new FollowupIssueFilter(jiraProperties, projectKey))
                .collect(Collectors.toList());
        
        List<IssueKpiDataItemAdapter> items = factory.getItems(issuesVisibleToUser,timezone);
        List<IssueKpi> issuesKpi = new IssueKpiTransformer(items)
                                        .mappingHierarchically(issuesVisibleToUser)
                                        .transform();
        
        Map<KpiLevel,List<IssueKpi>> mappedIssues = new EnumMap<>(KpiLevel.class);
        for (KpiLevel level : KpiLevel.values()) {
            mappedIssues.put(level, issuesKpi.stream().filter(i -> i.getLevel() == level).collect(Collectors.toList()));
        }
        
        return mappedIssues;
    }

    public List<IssueKpi> getIssues(Optional<AnalyticsTransitionsDataSet> analyticSet){
        return new IssueKpiTransformer(factory.getItems(analyticSet)).transform();
    }
    
   
}
