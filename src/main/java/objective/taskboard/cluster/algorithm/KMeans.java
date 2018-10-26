package objective.taskboard.cluster.algorithm;

import static objective.taskboard.utils.StreamUtils.toLinkedHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public class KMeans {

    public static final double DEFAULT_MINIMUM_CLUSTER_SIZE_PERCENT = 0.02;

    private final double minimumClusterSizePercent;
    private final List<String> groupsLabels;

    public KMeans(List<String> groupsLabels) {
        this(groupsLabels, DEFAULT_MINIMUM_CLUSTER_SIZE_PERCENT);
    }

    public KMeans(List<String> groupsLabels, double minimumClusterSizePercent) {
        this.groupsLabels = Collections.unmodifiableList(groupsLabels);
        this.minimumClusterSizePercent = minimumClusterSizePercent;
    }

    public <T> ClusterAlgorithmResult<T> calculateClusters(Collection<T> dataPoints, CentroidCalculator<T> centroidCalculator) {
        ClusterAlgorithmIteration<T> lastIterationResult;

        final List<T> points = new ArrayList<>(dataPoints);
        final List<T> outliers = new ArrayList<>();

        do {
            lastIterationResult = runIteration(points, centroidCalculator);
            points.removeAll(lastIterationResult.pointsToRemove);
            outliers.addAll(lastIterationResult.pointsToRemove);
        } while (!lastIterationResult.pointsToRemove.isEmpty());

        List<Cluster<T>> clusters = lastIterationResult.clusters;
        clusters.sort(Comparator.comparing(Cluster::getCentroid));
        Map<String, Cluster<T>> result = IntStream.range(0, groupsLabels.size()).boxed()
                .collect(toLinkedHashMap(groupsLabels::get, clusters::get));

        return new ClusterAlgorithmResult<>(result, outliers);
    }

    private <T> ClusterAlgorithmIteration<T> runIteration(final List<T> points, CentroidCalculator<T> centroidCalculator) {
        final ClusterAlgorithmIteration<T> iteration = new ClusterAlgorithmIteration<>(points, centroidCalculator);
        iteration.initializeClusters(groupsLabels.size());
        iteration.distribute();
        iteration.trimSmallClusters(minimumClusterSizePercent);
        return iteration;
    }

    private static class ClusterAlgorithmIteration<T> {

        private final CentroidCalculator<T> centroidCalculator;
        public List<Cluster<T>> clusters = new ArrayList<>();
        public List<T> pointsToRemove = new ArrayList<>();

        private List<T> points;

        public ClusterAlgorithmIteration(List<T> points, CentroidCalculator<T> centroidCalculator) {
            this.points = points;
            this.centroidCalculator = centroidCalculator;
        }

        public void initializeClusters(int size) {
            this.clusters = new ArrayList<>(size);
            Random random = new Random();
            for (int count = 0; clusters.size() < size; ++count) {
                T seed = points.get(random.nextInt(points.size()));
                Cluster<T> cluster = new Cluster<>(centroidCalculator.calculate(seed));
                if (!clusters.contains(cluster) || count > (size * 100)) {
                    clusters.add(cluster);
                }
            }
        }

        public void distribute() {
            boolean isStillMoving = true;

            while (isStillMoving) {
                refillClusters();

                isStillMoving = false;

                for (Cluster<T> cluster : clusters) {
                    Centroid oldCentroid = cluster.getCentroid();
                    centroidCalculator.calculate(cluster.getPoints()).ifPresent(cluster::setCentroid);
                    if (!oldCentroid.equals(cluster.getCentroid())) {
                        isStillMoving = true;
                    }
                }
            }
        }

        public void trimSmallClusters(double percentual) {
            pointsToRemove.clear();
            final double totalSize = points.size();
            for (Cluster<T> cluster : clusters) {
                double clusterSizePercent = cluster.getPoints().size() / totalSize;
                if (clusterSizePercent < percentual) {
                    pointsToRemove.addAll(cluster.getPoints());
                }
            }
        }

        private void refillClusters() {
            clearClusters();
            fillClusters();
        }

        private void clearClusters() {
            for (Cluster cluster : clusters) {
                cluster.getPoints().clear();
            }
        }

        private void fillClusters() {
            for (T point : points) {
                Cluster<T> cluster = getCluster(point);
                cluster.getPoints().add(point);
            }
        }

        private Cluster<T> getCluster(T point) {
            Cluster<T> selected = clusters.get(0);

            Cluster<T> cluster = selected;
            double minimum = centroidCalculator.calculate(point).distance(cluster.getCentroid());

            for(int i = 1; i < clusters.size(); ++i) {
                cluster = clusters.get(i);
                double distance = centroidCalculator.calculate(point).distance(cluster.getCentroid());
                if(distance < minimum) {
                    minimum = distance;
                    selected = clusters.get(i);
                }
            }

            return selected;
        }
    }
}
