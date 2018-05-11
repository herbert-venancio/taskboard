package objective.taskboard.issue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.StatusPriorityOrder;
import objective.taskboard.jira.MetadataService;

@Service
public class CardStatusOrderCalculator {
    @Autowired
    private JiraProperties jiraPropeties;
    
    @Autowired
    private MetadataService metadataService;
    
    private Map<Long, List<Long>> statusOrderByIssueType = new LinkedHashMap<>();

    private List<Long> subtasks;
    
    @PostConstruct
    public void init() {
        StatusPriorityOrder statusPriorityOrder = jiraPropeties.getStatusPriorityOrder();
        List<Long> demands = statusNameToId(statusPriorityOrder.getDemandsInOrder());
        
        Collections.reverse(demands);
        statusOrderByIssueType.put(jiraPropeties.getIssuetype().getDemand().getId(), demands);
        
        List<Long> tasks = statusNameToId(statusPriorityOrder.getTasksInOrder());
        
        Collections.reverse(tasks);
        jiraPropeties.getIssuetype().getFeatures().stream().forEach(feature -> {
            statusOrderByIssueType.put(feature.getId(), tasks);
        });
        
        subtasks = statusNameToId(statusPriorityOrder.getSubtasksInOrder());
        Collections.reverse(subtasks);
    }
    
    public int computeStatusOrder(Long issueType, Long status) {
        if (statusOrderByIssueType.containsKey(issueType)) 
            return statusOrderByIssueType.get(issueType).indexOf(status);
        
        return subtasks.indexOf(status);
    }
    
    private List<Long> statusNameToId(String [] names) {
        return Stream.of(names)
                .map(statusName->metadataService.getIdOfStatusByName(statusName))
                .collect(Collectors.toList());
    }
}
