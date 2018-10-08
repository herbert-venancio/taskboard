package objective.taskboard.followup.kpi.transformer;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.DatedStatusTransition;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.StatusTransition;

public class IssueKpiTransformer {
    
    private List<IssueKpi> issues;

    public IssueKpiTransformer(List<IssueKpiDataItemAdapter> items) {
        this.issues = map(items);
    }
    
    public IssueKpiTransformer mappingHierarchically(List<Issue> originalIssues) {
        Map<String,Issue> issuesByKey = originalIssues.stream().collect(Collectors.toMap(Issue::getIssueKey, Function.identity()));
        Map<String, IssueKpi> issueKpiByKey = issues.stream().collect(Collectors.toMap(IssueKpi::getIssueKey, Function.identity()));
        
        for (IssueKpi issueKpi : issues) {
            Issue parentIssue = issuesByKey.get(issueKpi.getIssueKey());
            String parentKey = parentIssue.getParent();
            Optional<IssueKpi> parentIssueKpi = Optional.ofNullable(issueKpiByKey.get(parentKey));
            parentIssueKpi.ifPresent(p -> p.addChild(issueKpi));
        }
        
        return this;
    }

    public List<IssueKpi> transform(){
        return issues;
    }

    private List<IssueKpi> map(List<IssueKpiDataItemAdapter> items){
        List<IssueKpi> issues = new LinkedList<>();
        for (IssueKpiDataItemAdapter itemProvider : items) {
            
            Optional<StatusTransition> finalStatusTransition = Optional.empty();
            Optional<StatusTransition> next = finalStatusTransition;
            
            for (Entry<String,ZonedDateTime> transition : itemProvider.getTransitions().entrySet()) {
                String status = transition.getKey();
                ZonedDateTime date = transition.getValue();
                
                StatusTransition statusTrasition = create(status, date,next);
                next = Optional.of(statusTrasition);
            }
            IssueKpi issue = new IssueKpi(itemProvider.getIssueKey(),itemProvider.getIssueType(), itemProvider.getLevel(), next);
            issues.add(issue);
        }
        return issues;
    }


    private StatusTransition create(String status, ZonedDateTime date, Optional<StatusTransition> next) { 
        return (date == null) ? new StatusTransition(status,next) : new DatedStatusTransition(status, date,next); 
    } 

}
