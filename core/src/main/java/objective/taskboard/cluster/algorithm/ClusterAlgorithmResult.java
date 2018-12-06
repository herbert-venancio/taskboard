package objective.taskboard.cluster.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterAlgorithmResult<T> {

    private Map<String, Cluster<T>> clusters = new HashMap<>();
    private List<T> outliers = new ArrayList<>();

    public ClusterAlgorithmResult() {
    }

    public ClusterAlgorithmResult(Map<String, Cluster<T>> clusters, List<T> outliers) {
        this.clusters = clusters;
        this.outliers = outliers;
    }

    public Map<String, Cluster<T>> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, Cluster<T>> clusters) {
        this.clusters = clusters;
    }

    public List<T> getOutliers() {
        return outliers;
    }

    public void setOutliers(List<T> outliers) {
        this.outliers = outliers;
    }
}
