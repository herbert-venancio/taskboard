package objective.taskboard.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.project.config.changeRequest.ChangeRequest;
import objective.taskboard.repository.ProjectFilterConfigurationRepository;
import objective.taskboard.repository.TaskboardIssueRepository;

@RestController
@RequestMapping(value = "/audit")
public class AuditController {

    private final Javers javers;
    private TaskboardIssueRepository issueRepository;
    private ProjectFilterConfigurationRepository projectRepository;

    @Autowired
    public AuditController(Javers javers, TaskboardIssueRepository issueRepository, ProjectFilterConfigurationRepository projectRepository) {
        this.javers = javers;
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
    }

    @RequestMapping("/change-request/changes")
    public String getChangeRequestChanges() {
        return getChanges(ChangeRequest.class);
    }

    @RequestMapping("/project-filter-configuration/changes")
    public String getProjectChanges() {
        return getChanges(ProjectFilterConfiguration.class);
    }

    @RequestMapping("/sizing-cluster/changes")
    public String getSizingChanges() {
        return getChanges(SizingCluster.class);
    }

    @RequestMapping("/taskboard-issue/changes")
    public String getTaskboardIssueChnages() {
        return getChanges(TaskboardIssue.class);
    }

    @RequestMapping("/change-request/snapshots")
    public String getChangeRequestSnapshots() {
        return getSnapshots(ChangeRequest.class);
    }

    @RequestMapping("/project-filter-configuration/snapshots")
    public String getProjectSnapshots() {
        return getSnapshots(ProjectFilterConfiguration.class);
    }

    @RequestMapping("/sizing-cluster/snapshots")
    public String getSizingSnapshots() {
        return getSnapshots(SizingCluster.class);
    }

    @RequestMapping("/taskboard-issue/snapshots")
    public String getTaskboardIssueSnapshots() {
        return getSnapshots(TaskboardIssue.class);
    }

    @RequestMapping("/project-filter-configuration/blah")
    public String restoreProject(@RequestBody RestoreRequest<Long> request) {
        Optional.ofNullable(projectRepository.getOne(request.getId()))
        .ifPresent(projectFilterConfiguration -> {
            restore(request, projectFilterConfiguration, ProjectFilterConfiguration.class);
        });
        return "ok";
    }

    @RequestMapping("/taskboard-issue/blah")
    public String restoreFromHistory(@RequestBody RestoreRequest<String> request) {
        issueRepository.findByIssueKey(request.getId())
        .ifPresent(taskboardIssue -> {
            restore(request, taskboardIssue, TaskboardIssue.class);
        });
        return "ok";
    }

    public static class RestoreRequest<T> {

        private T id;
        private BigDecimal commitId;

        public T getId() {
            return id;
        }
        public void setId(T id) {
            this.id = id;
        }
        public BigDecimal getCommitId() {
            return commitId;
        }
        public void setCommitId(BigDecimal commitId) {
            this.commitId = commitId;
        }
    }

    private String getChanges(Class<?> type) {
        QueryBuilder jqlQuery = QueryBuilder.byClass(type);

        List<Change> changes = javers.findChanges(jqlQuery.build());

        return javers.getJsonConverter().toJson(changes);
    }

    private String getSnapshots(Class<?> type) {
        QueryBuilder jqlQuery = QueryBuilder.byClass(type);

        List<CdoSnapshot> changes = javers.findSnapshots(jqlQuery.build());

        return javers.getJsonConverter().toJson(changes);
    }
    
    private <T> void restore(RestoreRequest<T> request, Object entity, Class<?> type) {
        QueryBuilder jqlQuery = QueryBuilder.byInstanceId(request.getId(), type)
                .withCommitId(request.getCommitId());

        List<CdoSnapshot> snaps = javers.findSnapshots(jqlQuery.build());
        if(snaps.size() != 1)
            return;

        BeanWrapper bean = new BeanWrapperImpl(entity);
        snaps.get(0).getState().forEachProperty((property, value) -> {
            bean.setPropertyValue(property, value);
        });
    }
}
