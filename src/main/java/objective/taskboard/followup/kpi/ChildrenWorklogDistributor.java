package objective.taskboard.followup.kpi;

import java.util.List;

import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;

public class ChildrenWorklogDistributor {

    private IssueTypeChildrenStatusHierarchy hierarchies;

    public ChildrenWorklogDistributor(IssueTypeChildrenStatusHierarchy hierarchies) {
        this.hierarchies = hierarchies;
    }

    public void distributeWorklogs(IssueKpi kpi) {
        this.hierarchies.getHierarchies().stream().forEach( h -> {
            String status = h.getFatherStatus();
            List<Long> subtasks = h.getChildrenTypeId();
            List<String> childrenStatuses = h.getChildrenStatus();
            kpi.findStatus(status).ifPresent(s -> {
                setupWorklog(kpi,s,subtasks);
                setupWorklogs(kpi,s,childrenStatuses);
            });
        });
    }

    private void setupWorklogs(IssueKpi kpi, StatusTransition status, List<String> childrenStatuses) {
        childrenStatuses.stream()
            .map( s -> kpi.getWorklogFromChildrenStatus(s))
            .flatMap(List::stream)
            .forEach(w -> status.addWorklog(w));
    }

    private void setupWorklog(IssueKpi kpi, StatusTransition status, List<Long> subtasks) {
        subtasks.stream()
            .map( typeId -> kpi.getWorklogFromChildren(typeId))
            .flatMap(List::stream)
            .forEach(w -> status.addWorklog(w));        
    }
    
}
