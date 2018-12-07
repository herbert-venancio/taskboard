package objective.taskboard.cluster.algorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws/cluster/algorithm/form")
public class ClusterAlgorithmFormController {

    @Autowired
    private ClusterAlgorithmProperties clusterAlgorithmProperties;

    @GetMapping("defaults")
    public ResponseEntity<ClusterAlgorithmRequest> fetchDefaults() {
        return ResponseEntity.ok(ClusterAlgorithmRequest.fromDefaults(clusterAlgorithmProperties.getDefaults()));
    }
}
