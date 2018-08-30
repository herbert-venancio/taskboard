package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;

@Service
public class IssueStatusFlowService {
    
    public List<IssueStatusFlow> getIssues(AnalyticsTransitionsDataSet ds){
        List<IssueStatusFlow> issues = new LinkedList<>();
        for (AnalyticsTransitionsDataRow row : ds.rows) {
            List<String> statusesheader = ds.getStatusHeader();
            List<ZonedDateTime> transitions = row.transitionsDates;
            
            StatusTransitionChain finalStatusTransition = new TerminalStateTransition();
            StatusTransitionChain next = finalStatusTransition;
            
            for (int i = 0; i < transitions.size(); i++) {
                String status = statusesheader.get(i);
                ZonedDateTime date = transitions.get(i);
                
                StatusTransitionChain statusTrasition = getStatusTransition(status, date,next);
                next = statusTrasition;
            }
            
            IssueStatusFlow issue = new IssueStatusFlow(row.issueKey,row.issueType, next);
            issues.add(issue);
        }
        return issues;
    }

    private StatusTransitionChain getStatusTransition(String status, ZonedDateTime date, StatusTransitionChain next) {
        return (date == null) ? new NoDateStatusTransition(status,next) : new StatusTransition(status, date,next);
    }

}
