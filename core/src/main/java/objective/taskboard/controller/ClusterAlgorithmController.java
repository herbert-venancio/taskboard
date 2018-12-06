package objective.taskboard.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.cluster.algorithm.ClusterAlgorithmExecution;
import objective.taskboard.cluster.algorithm.ClusterAlgorithmRequest;
import objective.taskboard.cluster.algorithm.ClusterAlgorithmService;

@RestController
@RequestMapping("/ws/cluster/algorithm")
public class ClusterAlgorithmController {

    @Autowired
    private ClusterAlgorithmService clusterAlgorithmService;

    @GetMapping
    public ResponseEntity<List<ClusterAlgorithmExecution>> listExecutions() {
        return ResponseEntity.ok(clusterAlgorithmService.getExecutions());
    }

    @PostMapping
    public ResponseEntity<ClusterAlgorithmExecution> executeAlgorithm(@RequestBody ClusterAlgorithmRequest request) {
        return ResponseEntity.ok(clusterAlgorithmService.startExecution(request));
    }

    @GetMapping("{executionId}")
    public ResponseEntity<ClusterAlgorithmExecution> getExecution(@PathVariable("executionId") long executionId) {
        return clusterAlgorithmService.getExecution(executionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("{executionId}/stop")
    public ResponseEntity<ClusterAlgorithmExecution> stopExecution(@PathVariable("executionId") long executionId) {
        return clusterAlgorithmService.stopExecution(executionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{executionId}")
    public ResponseEntity<Void> deleteExecution(@PathVariable("executionId") long executionId) {
        Optional<ClusterAlgorithmExecution> execution = clusterAlgorithmService.deleteExecution(executionId);
        if(execution.isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
