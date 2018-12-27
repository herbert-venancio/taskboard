package objective.taskboard.cluster.base;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.TaskboardAdministrationPermission;
import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.jira.AuthorizedProjectsService;

@RestController
@RequestMapping("/ws/base-cluster")
public class BaseClusterController {

    private static final String CLUSTERS_NOT_FOUND = "Clusters not found.";
    private static final String CLUSTER_NOT_FOUND = "Cluster not found.";

    private final BaseClusterService service;
    private final TaskboardAdministrationPermission taskboardAdministrationPermission;
    private final AuthorizedProjectsService authorizedProjectsService;

    @Autowired
    public BaseClusterController(
        final BaseClusterService service,
        final TaskboardAdministrationPermission taskboardAdministrationPermission,
        final AuthorizedProjectsService authorizedProjectsService
    ) {
        this.service = service;
        this.taskboardAdministrationPermission = taskboardAdministrationPermission;
        this.authorizedProjectsService = authorizedProjectsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable final Long id) {
        if (!taskboardAdministrationPermission.isAuthorized())
            return clusterNotFoundResponse();

        Optional<BaseClusterDto> cluster = service.findById(id);

        if (!cluster.isPresent())
            return clusterNotFoundResponse();

        return ResponseEntity.ok(cluster.get());
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getAllItems(@PathVariable final Long id) {
        if (!taskboardAdministrationPermission.isAuthorized())
            return clusterNotFoundResponse();

        Optional<BaseClusterDto> cluster = service.findById(id);

        if (!cluster.isPresent())
            return clusterNotFoundResponse();

        return ResponseEntity.ok(cluster.get().getItems());
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        if (!taskboardAdministrationPermission.isAuthorized())
            return clusterNotFoundResponse();

        List<BaseClusterDto> clusters = service.findAll();

        if (clusters.isEmpty())
            return new ResponseEntity<>(CLUSTERS_NOT_FOUND, NOT_FOUND);

        return ResponseEntity.ok(clusters);
    }

    @GetMapping("/new")
    public ResponseEntity<?> getEmptyCluster() {
        if (!taskboardAdministrationPermission.isAuthorized())
            return clusterNotFoundResponse();

        BaseClusterDto creationModel = service.getNewModel();
        return ResponseEntity.ok(creationModel);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody final BaseClusterDto cluster) {
        if (!taskboardAdministrationPermission.isAuthorized())
            return clusterNotFoundResponse();

        SizingCluster savedCluster = service.create(cluster);

        URI uri = fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCluster.getId())
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable("id") final Long id, @RequestBody final BaseClusterDto cluster) {
        if (!taskboardAdministrationPermission.isAuthorized())
            return clusterNotFoundResponse();

        Optional<BaseClusterDto> savedCluster = service.update(id, cluster);
        if (!savedCluster.isPresent())
            return clusterNotFoundResponse();

        return ResponseEntity.ok(savedCluster.get());
    }

    @GetMapping("projects")
    public ResponseEntity<?> getAuthorizedProjectNames() {
        if (!taskboardAdministrationPermission.isAuthorized())
            return new ResponseEntity<>(NOT_FOUND);

        List<String> authorizedProjectNames = authorizedProjectsService.getTaskboardProjects(PROJECT_ADMINISTRATION).stream()
                .map(p -> p.getProjectKey())
                .collect(toList());

        return new ResponseEntity<>(authorizedProjectNames, OK);
    }

    private ResponseEntity<?> clusterNotFoundResponse() {
        return new ResponseEntity<>(CLUSTER_NOT_FOUND, NOT_FOUND);
    }
}
