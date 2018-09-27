package objective.taskboard.cluster.base;


import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.notFound;
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

import objective.taskboard.auth.Authorizer;
import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.repository.PermissionRepository;

@RestController
@RequestMapping("/ws/base-cluster")
public class BaseClusterController {

    private static final String CLUSTERS_NOT_FOUND = "Clusters not found.";
    private static final String CLUSTER_NOT_FOUND = "Cluster not found.";
    private final BaseClusterService service;
    private final Authorizer authorizer;

    @Autowired
    public BaseClusterController(
            BaseClusterService service,
            Authorizer authorizer
            ) {
        this.service = service;
        this.authorizer = authorizer;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable final Long id) {
        if (userWithoutAdministrativePermissions())
            return notFound().build();

        Optional<BaseClusterDto> cluster = service.findById(id);
        if (!cluster.isPresent())
            return new ResponseEntity<>(CLUSTER_NOT_FOUND, NOT_FOUND);

        return ResponseEntity.ok(cluster.get());
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<Object> getAllItems(@PathVariable final Long id) {
        if (userWithoutAdministrativePermissions())
            return notFound().build();

        Optional<BaseClusterDto> cluster = service.findById(id);

        if (!cluster.isPresent())
            return new ResponseEntity<>(CLUSTER_NOT_FOUND, NOT_FOUND);

        return ResponseEntity.ok(cluster.get().getItems());
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        if (userWithoutAdministrativePermissions())
            return notFound().build();

        List<BaseClusterDto> clusters = service.findAll();

        if (clusters.isEmpty())
            return new ResponseEntity<>(CLUSTERS_NOT_FOUND, NOT_FOUND);

        return ResponseEntity.ok(clusters);
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody final BaseClusterDto cluster) {
        if (userWithoutAdministrativePermissions())
            return notFound().build();

        SizingCluster savedCluster = service.create(cluster);

        URI uri = fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCluster.getId())
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> update(@PathVariable("id") final Long id, @RequestBody final BaseClusterDto cluster) {
        if (userWithoutAdministrativePermissions())
            return notFound().build();

        Optional<BaseClusterDto> savedCluster = service.update(id, cluster);
        if (!savedCluster.isPresent())
            return new ResponseEntity<>(CLUSTER_NOT_FOUND, NOT_FOUND);

        return ResponseEntity.ok(savedCluster.get());
    }

    private boolean userWithoutAdministrativePermissions() {
        return !authorizer.hasPermissionInAnyProject(PermissionRepository.ADMINISTRATIVE);
    }
}
