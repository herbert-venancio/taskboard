package objective.taskboard.controller;

import java.util.List;

import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.project.config.changeRequest.ChangeRequest;

@RestController
@RequestMapping(value = "/audit")
public class AuditController {

    private final Javers javers;

    @Autowired
    public AuditController(Javers javers) {
        this.javers = javers;
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

    @RequestMapping("/change-request/snapshot")
    public String getChangeRequestSnapshots() {
        return getSnapshots(ChangeRequest.class);
    }

    @RequestMapping("/project-filter-configuration/snapshot")
    public String getProjectSnapshots() {
        return getSnapshots(ProjectFilterConfiguration.class);
    }

    @RequestMapping("/sizing-cluster/snapshot")
    public String getSizingSnapshots() {
        return getSnapshots(SizingCluster.class);
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
}
