package objective.taskboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.cluster.algorithm.ClusterAlgorithmRequest;
import objective.taskboard.jira.properties.ClusterAlgorithmProperties;

@RestController
@RequestMapping("/ws/cluster/algorithm/form")
public class ClusterAlgorithmFormController {

    @Autowired
    private ClusterAlgorithmProperties clusterAlgorithmProperties;

    @GetMapping("defaults")
    public ResponseEntity<ClusterAlgorithmRequest> fetchDefaults() {
        ClusterAlgorithmRequest request = new ClusterAlgorithmRequest();

        request.setProjects(clusterAlgorithmProperties.getDefaults().getProjects());
        request.setFeatureIssueTypes(clusterAlgorithmProperties.getDefaults().getFeatureIssueTypes());
        request.setBugIssueTypes(clusterAlgorithmProperties.getDefaults().getBugIssueTypes());
        request.setFeatureDoneStatuses(clusterAlgorithmProperties.getDefaults().getFeatureDoneStatuses());
        request.setSubtaskDoneStatuses(clusterAlgorithmProperties.getDefaults().getSubtaskDoneStatuses());
        request.setSubtaskCycleStatuses(clusterAlgorithmProperties.getDefaults().getCycleStatuses());
        request.setClusteringType(clusterAlgorithmProperties.getDefaults().getClusteringType());
        request.setDateRange(clusterAlgorithmProperties.getDefaults().getDateRange());

        return ResponseEntity.ok(request);
    }
}
