package objective.taskboard.followup.kpi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;

public class ChildrenWorklogDistributor {

    private Map<String, TypeDistributor> types = new HashMap<>();
    private Map<String, StatusDistributor> statuses = new HashMap<>();
    private IssueKpi issueKpi;
    private Set<ZonedWorklog> worklogsAlreadyDistributed = new HashSet<>();

    public static void distributeWorklogs(IssueTypeChildrenStatusHierarchy hierarchies, IssueKpi issueKpi) {
        new ChildrenWorklogDistributor(hierarchies, issueKpi).distributeWorklogs();
    }

    private ChildrenWorklogDistributor(IssueTypeChildrenStatusHierarchy hierarchies, IssueKpi issueKpi) {
        initializeDistributorsUsingIssueStatuses(issueKpi);
        configureDistributorsUsingHiearchies(hierarchies);
        this.issueKpi = issueKpi;
    }

    private void initializeDistributorsUsingIssueStatuses(IssueKpi issueKpi) {
        issueKpi.getStatusChain().getStatusesAsList().forEach(s -> {
            types.put(s.getStatusName(), new TypeDistributor(s));
            statuses.put(s.getStatusName(), new StatusDistributor(s));
        });
    }

    private void configureDistributorsUsingHiearchies(IssueTypeChildrenStatusHierarchy hierarchies) {
        hierarchies.getHierarchies().stream().forEach(h -> {
           types.get(h.getFatherStatus()).addAll(h.getChildrenTypeIds());
           statuses.get(h.getFatherStatus()).addAll(h.getChildrenStatuses());
        });
    }

    private void distributeWorklogs() {
        types.values().stream().forEach(TypeDistributor::distribute);
        statuses.values().stream().forEach(StatusDistributor::distribute);
    }

    private class TypeDistributor {
        private StatusTransition statusTransition;
        private List<Long> childrenTypeIds = new LinkedList<>();
        private TypeDistributor(StatusTransition statusTransition) {
            this.statusTransition = statusTransition;
        }
        private void addAll(List<Long> childrenTypeIds) {
            this.childrenTypeIds.addAll(childrenTypeIds);
        }
        private void distribute() {
            for (long childTypeId : childrenTypeIds) {
                List<ZonedWorklog> worklogs = issueKpi.getWorklogFromChildrenTypeId(childTypeId);
                for (ZonedWorklog worklog : worklogs) {
                    statusTransition.putWorklog(worklog);
                    worklogsAlreadyDistributed.add(worklog);
                }
            }
        }
    }
   private class StatusDistributor {
        private StatusTransition statusTransition;
        private List<String> childrenStatuses = new LinkedList<>();
        private StatusDistributor(StatusTransition statusTransition) {
            this.statusTransition = statusTransition;
        }
        private void addAll(List<String> childrenStatuses) {
            this.childrenStatuses.addAll(childrenStatuses);
        }
        private void distribute() {
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
}
