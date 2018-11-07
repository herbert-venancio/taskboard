package objective.taskboard.cluster.algorithm;

import java.util.ArrayList;
import java.util.List;

public class Cluster<T> {

    private Centroid centroid;
    private List<T> points = new ArrayList<>();

    public Cluster(Centroid centroid) {
        this.centroid = centroid;
    }

    public Centroid getCentroid() {
        return centroid;
    }

    public void setCentroid(Centroid centroid) {
        this.centroid = centroid;
    }

    public List<T> getPoints() {
        return points;
    }

    public void setPoints(List<T> points) {
        this.points = points == null ? new ArrayList<>() : new ArrayList<>(points);
    }

}
