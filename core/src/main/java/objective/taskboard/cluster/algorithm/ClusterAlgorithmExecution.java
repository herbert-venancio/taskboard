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
import java.util.Set;
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
        return executionId;
    }

    public ClusterAlgorithmRequest getRequest() {
        return this.context.request;
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
        Table<String, Issue, IssueModel> model = buildClusteringModel(context.request.getClusterGrouping(), allFeatures, allBugs);
        setProgress(0.6f);

        checkInterruption();
        CentroidCalculator<IssueModel> centroidCalculator = createCentroidCalculator(context.request.getClusteringType(), model.values());
        Map<String, ClusterAlgorithmResult<IssueModel>> featureClusters = runClustering(model, centroidCalculator);
        setProgress(0.8f);

        return featureClusters;
    }

    private static CentroidCalculator<IssueModel> createCentroidCalculator(ClusterAlgorithmRequest.ClusteringType clusteringType, Collection<IssueModel> models) {
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
                .filter(issue -> withinDateRange(issue, context.request.getDateRange(), context.request.getFeatureDoneStatuses()))
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
                .filter(issue -> withinDateRange(issue, context.request.getDateRange(), context.request.getSubtaskDoneStatuses()))
                .collect(Collectors.toList());
    }

    private Table<String, Issue, IssueModel> buildClusteringModel(ClusterAlgorithmRequest.ClusterGrouping clusterGrouping, List<Issue> allFeatures, Multimap<Issue, Issue> allBugs) {
        switch(clusterGrouping) {
            case BALLPARK:
                return buildFeatureClusteringModel(allFeatures, allBugs);
            case SUBTASK:
                return buildSubtaskClusteringModel(allFeatures, allBugs);
            default:
                throw new RuntimeException("Unknown cluster grouping: " + clusterGrouping.name());
        }
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

    private Table<String, Issue, IssueModel> buildSubtaskClusteringModel(List<Issue> allFeatures, Multimap<Issue, Issue> allBugs) {
        Set<Long> issueTypes = context.ballparkMappings.values()
                .stream()
                .flatMap(Collection::stream)
                .flatMap(ballpark -> ballpark.getJiraIssueTypes().stream())
                .collect(Collectors.toSet());

        return Stream.concat(allFeatures.stream(), allBugs.values().stream())
                .flatMap(feat -> getDoneSubtasks(feat).stream())
                .filter(subtask -> issueTypes.contains(subtask.getType()))
                .distinct()
                .collect(toTable(
                        Issue::getIssueTypeName
                        , Function.identity()
                        , subtask -> IssueModel.createForSubtask(subtask, context.request.getSubtaskCycleStatuses())
                        , HashBasedTable::create
                ));
    }

    private Map<String, ClusterAlgorithmResult<IssueModel>> runClustering(Table<String, Issue, IssueModel> modelTable, CentroidCalculator<IssueModel> centroidCalculator) {
        KMeans kmeans = new KMeans(CLUSTER_LABELS);
        Map<String, ClusterAlgorithmResult<IssueModel>> clusterResults = Collections.synchronizedMap(new HashMap<>());
        modelTable.rowMap().entrySet()
                .parallelStream()
                .forEach(cell -> {
                    String group = cell.getKey();
                    Collection<IssueModel> issues = cell.getValue().values();

                    // do 100 measurements
                    Map<ClusterAlgorithmResult<IssueModel>, Long> measurements = IntStream.range(0, 100)
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

    private boolean withinDateRange(Issue issue, ClusterAlgorithmRequest.DateRange dateRange, List<Long> statuses) {
        if(dateRange == null || (dateRange.getStartDate() == null && dateRange.getEndDate() == null))
            return true;

        // Reference JQLs:
        // - status was not in (statuses) before startDate
        // - status was not in (statuses) after endDate
        return issue.getChangelog().stream()
                .filter(entry -> "status".equals(entry.field))
                .filter(entry -> statuses.contains(Long.parseLong(entry.originalTo)))
                .noneMatch(entry -> {
                    boolean isBeforeStartDate = dateRange.getStartDate() != null
                            && entry.timestamp.isBefore(dateRange.getStartDate().atStartOfDay(entry.timestamp.getZone()));
                    boolean isAfterEndDate = dateRange.getEndDate() != null
                            && entry.timestamp.isAfter(dateRange.getEndDate().atStartOfDay(entry.timestamp.getZone()));
                    return isBeforeStartDate || isAfterEndDate;
                });
    }
}
