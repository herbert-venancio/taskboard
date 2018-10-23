package objective.taskboard.followup.kpi.transformer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;

@Service
public class IssueKpiDataItemAdapterFactory {

    @Autowired
    private MetadataService metadataService;
    
    @Autowired
    private JiraProperties jiraProperties;
    
    @Autowired
    private IssueTransitionService transitionService;
    
    public List<IssueKpiDataItemAdapter> getItems(Optional<AnalyticsTransitionsDataSet> dataSet){
        if(!dataSet.isPresent())
            return Arrays.asList();
        
        AnalyticsTransitionsDataSet ds = dataSet.get();
        if(ds.rows.isEmpty())
            return Arrays.asList();
        
        List<String> headers = ds.getStatusHeader();
        return ds.rows.stream().map(r -> makeItem(r,headers)).collect(Collectors.toList());
    }
    
    public List<IssueKpiDataItemAdapter> getItems(List<Issue> issues,ZoneId timezone){
     
        return issues.stream().map(i -> makeItem(i,timezone)).collect(Collectors.toList());
    }
    
    private IssueKpiDataItemAdapter makeItem(Issue issue,ZoneId timezone) {
        KpiLevel level = KpiLevel.of(issue);
        Map<String, ZonedDateTime> transitions = transitionService.getTransitions(issue,timezone,level.getStatusPriorityOrder(jiraProperties));
        
        return new IssueDataItemAdapter(issue, getType(issue), level, transitions);
    }

    private IssueKpiDataItemAdapter makeItem(AnalyticsTransitionsDataRow row, List<String> headers) {

        Optional<JiraIssueTypeDto> type = metadataService.getIssueTypeByName(row.issueType); 

        return new AnalyticDataRowAdapter(row,getType(type), headers,getLevel(type));
    }
    
    private Optional<IssueTypeKpi> getType(Optional<JiraIssueTypeDto> type) {
        return type.flatMap(t -> Optional.of(new IssueTypeKpi(t.getId(), t.getName())));
    }
    
    private Optional<IssueTypeKpi> getType(Issue issue) {
        return Optional.of(new IssueTypeKpi(issue.getType(), issue.getIssueTypeName()));
    }

    private KpiLevel getLevel(Optional<JiraIssueTypeDto> type) {
        if(!type.isPresent())
            return KpiLevel.UNMAPPED;
        return KpiLevel.given(jiraProperties,type.get());
    }
    
}
