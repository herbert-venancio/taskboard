package objective.taskboard.followup.kpi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy.Hierarchy;

public class ChildrenWorklogDistributor {

    private IssueTypeChildrenStatusHierarchy hierarchies;
    private IssueKpi issueKpi;
    private Set<ZonedWorklog> worklogsAlreadyDistributed = new HashSet<>();

    public static void distributeWorklogs(IssueTypeChildrenStatusHierarchy hierarchies, IssueKpi issueKpi) {
        new ChildrenWorklogDistributor(hierarchies, issueKpi).distributeWorklogs();
    }

    private ChildrenWorklogDistributor(IssueTypeChildrenStatusHierarchy hierarchies, IssueKpi issueKpi) {
        this.hierarchies = hierarchies;
        this.issueKpi = issueKpi;
    }

    private void distributeWorklogs() {
        for (Hierarchy hierarchy : hierarchies.getHierarchies()) {
            String fatherStatus = hierarchy.getFatherStatus();
            List<Long> childrenTypeIds = hierarchy.getChildrenTypeIds();
            List<String> childrenStatuses = hierarchy.getChildrenStatuses();
            issueKpi.findStatus(fatherStatus)
                    .ifPresent(statusTransition -> {
                        distributeWorklogsFromChildrenTypeIds(statusTransition, childrenTypeIds);
                        distributeWorklogsFromChildrenStatus(statusTransition, childrenStatuses);
                    });
        }
    }

    private void distributeWorklogsFromChildrenTypeIds(StatusTransition statusTransition, List<Long> childrenTypeIds) {
        for (long childTypeId : childrenTypeIds) {
            List<ZonedWorklog> worklogs = issueKpi.getWorklogFromChildrenTypeId(childTypeId);
            for (ZonedWorklog worklog : worklogs) {
                statusTransition.putWorklog(worklog);
                worklogsAlreadyDistributed.add(worklog);
            }
        }
    }

    private void distributeWorklogsFromChildrenStatus(StatusTransition statusTransition, List<String> childrenStatuses) {
        for (String childStatus : childrenStatuses) {
            List<ZonedWorklog> worklogs = issueKpi.getWorklogFromChildrenStatus(childStatus);
            for (ZonedWorklog worklog : worklogs) {
                if (!worklogsAlreadyDistributed.contains(worklog)) {
                    statusTransition.putWorklog(worklog);
                }
            }
        }
    }
}
