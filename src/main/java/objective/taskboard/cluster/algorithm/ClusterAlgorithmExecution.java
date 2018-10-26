package objective.taskboard.cluster.algorithm;

import static com.google.common.collect.Multimaps.toMultimap;
import static com.google.common.collect.Tables.toTable;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import objective.taskboard.data.Issue;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.task.BackgroundTask;
import objective.taskboard.utils.RangeUtils;

public class ClusterAlgorithmExecution extends BackgroundTask<Map<String, ClusterAlgorithmResult<IssueModel>>> {

    private static final List<String> CLUSTER_LABELS = asList("XS", "S", "M", "L", "XL");

    protected final long executionId;

    private transient final ClusterAlgorithmContext context;

    public ClusterAlgorithmExecution(ExecutorService executor, long executionId, ClusterAlgorithmContext context) {
        super(executor);
        this.executionId = executionId;
        this.context = context;
    }

    public long getExecutionId() {
        read.lock();
        try {
            return executionId;
        } finally {
            read.unlock();
        }
    }

    @Override
    protected Map<String, ClusterAlgorithmResult<IssueModel>> execute() throws Exception {
        return clusteringAlgorithm();
    }

    private Map<String, ClusterAlgorithmResult<IssueModel>> clusteringAlgorithm() throws Exception {
        setProgress(0);

        checkInterruption();
        List<Issue> allFeatures = getAllFeatures();
        setProgress(0.2f);

        checkInterruption();
        Multimap<Issue, Issue> allBugs = getAllBugs();
        setProgress(0.4f);

        checkInterruption();
        Table<String, Issue, IssueModel> featureModel = buildFeatureClusteringModel(allFeatures, allBugs);
        setProgress(0.6f);

        checkInterruption();
        CentroidCalculator<IssueModel> centroidCalculator = createCentroidCalculator(context.request.getClusteringType(), featureModel.values());
        Map<String, ClusterAlgorithmResult<IssueModel>> featureClusters = runClustering(featureModel, centroidCalculator);
        setProgress(0.8f);

        return featureClusters;
    }

    private CentroidCalculator<IssueModel> createCentroidCalculator(ClusterAlgorithmRequest.ClusteringType clusteringType, Collection<IssueModel> models) {
        CentroidCalculator<IssueModel> centroidCalculator = new CentroidCalculator<>();
        switch(clusteringType) {
            case EFFORT_ONLY:
                centroidCalculator.addDimension("effort", IssueModel::getWorkedTime);
                break;
            case CYCLE_ONLY:
                centroidCalculator.addDimension("cycle", IssueModel::getCycleDays);
                break;
            case EFFORT_AND_CYCLE: {
                Optional<Range<Double>> effortRange = models.stream()
                        .map(model -> Range.is(model.getWorkedTime()))
                        .reduce(RangeUtils::expand);
                if (effortRange.isPresent()) {
                    centroidCalculator.addLinearDimension("effort", IssueModel::getWorkedTime, effortRange.get());
                } else {
                    centroidCalculator.addDimension("effort", IssueModel::getWorkedTime);
                }

                Optional<Range<Double>> cycleDaysRange = models.stream()
                        .map(model -> Range.is(model.getCycleDays()))
                        .reduce(RangeUtils::expand);
                if (cycleDaysRange.isPresent()) {
                    centroidCalculator.addLinearDimension("cycle", IssueModel::getCycleDays, cycleDaysRange.get());
                } else {
                    centroidCalculator.addDimension("cycle", IssueModel::getCycleDays);
                }
                break;
            }
            default:
                throw new RuntimeException("Unknown clustering type: " + clusteringType.name());
        }
        return centroidCalculator;
    }

    private List<Issue> getAllFeatures() {
        return context.allIssues.stream()
                .filter(Issue::isFeature)
                .filter(issue -> context.request.getProjects().contains(issue.getProjectKey()))
                .filter(issue -> context.request.getFeatureIssueTypes().contains(issue.getType()))
                .filter(issue -> context.request.getFeatureDoneStatuses().contains(issue.getStatus()))
                .collect(Collectors.toList());
    }

    private Multimap<Issue, Issue> getAllBugs() {
        return getAllFeatures().stream()
                .filter(feature -> !feature.getBugs().isEmpty())
                .flatMap(feature -> feature.getBugs().stream()
                        .flatMap(bugKey -> Optional.ofNullable(context.allIssuesMap.get(bugKey)).map(Stream::of).orElseGet(Stream::empty))
                        .filter(bug -> context.request.getBugIssueTypes().contains(bug.getType()))
                        .map(bug -> Pair.of(feature, bug)))
                .collect(toMultimap(Pair::getLeft, Pair::getRight, HashMultimap::create));
    }

    private List<Issue> getDoneSubtasks(Issue feature) {
        return feature.getSubtaskCards().stream()
                .filter(issue -> context.request.getSubtaskDoneStatuses().contains(issue.getStatus()))
                .collect(Collectors.toList());
    }

    private Table<String, Issue, IssueModel> buildFeatureClusteringModel(List<Issue> allFeatures, Multimap<Issue, Issue> allBugs) {
        // create a table group x feature, and each cell contains a
        // list of sub-tasks that matches that group and parent
        Table<String, Issue, List<Issue>> table = HashBasedTable.create();
        allFeatures.forEach(feature -> {
            if(!context.ballparkMappings.containsKey(feature.getType()))
                return;

            List<JiraProperties.BallparkMapping> ballparks = context.ballparkMappings.get(feature.getType());

            // collect sub-tasks of same group
            Multimap<String, Issue> groupedSubTasks = groupIssues(getDoneSubtasks(feature));

            ballparks.forEach(ballparkMapping -> {
                String group = ballparkMapping.getIssueType();

                Optional.ofNullable(groupedSubTasks.get(group)).ifPresent(subtasks ->
                        table.put(group, feature, new ArrayList<>(subtasks))
                );
            });

            // include bugs
            Optional.ofNullable(allBugs.get(feature)).ifPresent(bugs -> bugs.forEach(bug -> {
                // collect sub-tasks of same group
                Multimap<String, Issue> groupedBugSubtasks = groupIssues(getDoneSubtasks(bug));

                ballparks.forEach(ballparkMapping -> {
                    String group = ballparkMapping.getIssueType();

                    Optional.ofNullable(groupedBugSubtasks.get(group)).ifPresent(bugSubtasks -> {
                        if(table.contains(group, feature))
                            table.get(group, feature).addAll(bugSubtasks);
                        else
                            table.put(group, feature, new ArrayList<>(bugSubtasks));
                    });
                });
            }));
        });

        return table.cellSet().stream()
                .collect(toTable(
                        Table.Cell::getRowKey
                        , Table.Cell::getColumnKey
                        , cell -> IssueModel.createForFeatureAndSubtasks(
                                cell.getColumnKey()
                                , cell.getValue()
                                , context.request.getSubtaskCycleStatuses())
                        , HashBasedTable::create));
    }

    private Map<String, ClusterAlgorithmResult<IssueModel>> runClustering(Table<String, Issue, IssueModel> modelTable, CentroidCalculator<IssueModel> centroidCalculator) {
        KMeans kmeans = new KMeans(CLUSTER_LABELS);
        Map<String, ClusterAlgorithmResult<IssueModel>> clusterResults = Collections.synchronizedMap(new HashMap<>());
        modelTable.rowMap().entrySet()
                .parallelStream()
                .forEach(cell -> {
                    String group = cell.getKey();
                    Collection<IssueModel> issues = cell.getValue().values();

                    // do 20 measurements
                    Map<ClusterAlgorithmResult<IssueModel>, Long> measurements = IntStream.range(0, 10)
                            .parallel()
                            .mapToObj(i -> kmeans.calculateClusters(issues, centroidCalculator))
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                    // get most common result
                    ClusterAlgorithmResult<IssueModel> result = measurements.entrySet().stream()
                            .max(Comparator.comparing(Map.Entry::getValue))
                            .get().getKey();

                    clusterResults.put(group, result);
                });

        return clusterResults;
    }

    private Multimap<String, Issue> groupIssues(Collection<Issue> allSubtasks) {
        Multimap<String, Issue> groups = HashMultimap.create();

        for(Issue issue : allSubtasks) {
            issue.getParentCard()
                    .flatMap(parent -> Optional.ofNullable(context.ballparkMappings.get(parent.getType())))
                    .ifPresent(ballparks -> ballparks.forEach(ballpark -> {
                        if(ballpark.getJiraIssueTypes().contains(issue.getType())) {
                            groups.put(ballpark.getIssueType(), issue);
                        }
                    }));
        }
        return groups;
    }

}
