package objective.taskboard.followup;

import static java.util.Collections.emptySet;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_DEMAND;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_FEATURES;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_SUBTASKS;
import static objective.taskboard.utils.StreamUtils.toLinkedHashSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import objective.taskboard.Constants;
import objective.taskboard.data.Issue;
import objective.taskboard.data.Worklog;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.followup.cluster.FollowupClusterProvider;
import objective.taskboard.followup.kpi.FollowupIssueFilter;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.BallparkMapping;

@Component
public class FollowUpDataGenerator {

    private final JiraProperties jiraProperties;
    private final MetadataService metadataService;
    private final IssueBufferService issueBufferService;
    private final FollowupClusterProvider clusterProvider;
    private final IssueTransitionService transitionService;

    @Autowired
    public FollowUpDataGenerator(
            JiraProperties jiraProperties, 
            MetadataService metadataService, 
            IssueBufferService issueBufferService,
            FollowupClusterProvider clusterProvider,
            IssueTransitionService transitionService) {
        this.jiraProperties = jiraProperties;
        this.metadataService = metadataService;
        this.issueBufferService = issueBufferService;
        this.clusterProvider = clusterProvider;
        this.transitionService = transitionService;
    }

    public FollowUpData generate(ZoneId timezone, FollowupCluster cluster, String projectKey) {
        return new GenerationScope(timezone, cluster, projectKey).generate();
    }

    public FollowUpData generate(ZoneId timezone, String projectKey) {
        FollowupCluster cluster = clusterProvider.getForProject(projectKey);
        return generate(timezone, cluster, projectKey);
    }

    private class GenerationScope {

        private final String projectKey;
        private final ZoneId timezone;
        private final FollowupCluster cluster;
        private final FromJiraRowCalculator fromJiraRowCalculator;
        
        private Map<String, Issue> demandsByKey;
        private Map<String, Issue> featuresByKey;
        private Map<String, FromJiraDataRow> followUpBallparks;

        private GenerationScope(ZoneId timezone, FollowupCluster cluster, String projectKey) {
            this.projectKey = projectKey;
            this.timezone = timezone;
            this.cluster = cluster;
            this.fromJiraRowCalculator = new FromJiraRowCalculator(cluster); 
        }

        public FollowUpData generate() {
            
            List<Issue> issuesVisibleToUser = issueBufferService.getAllIssues().stream()
                    .filter(new FollowupIssueFilter(jiraProperties, projectKey))
                    .collect(Collectors.toList());

            FromJiraDataSet fromJiraDs = getFromJiraDs(issuesVisibleToUser, timezone);

            FollowUpTransitionsDataProvider transitions = new FollowUpTransitionsDataProvider(jiraProperties, transitionService,metadataService);
            List<AnalyticsTransitionsDataSet> analyticsTransitionsDsList = transitions.getAnalyticsTransitionsDsList(issuesVisibleToUser, timezone);
            List<SyntheticTransitionsDataSet> syntheticsTransitionsDsList = transitions.getSyntheticTransitionsDsList(analyticsTransitionsDsList);

            return new FollowUpData(fromJiraDs, analyticsTransitionsDsList, syntheticsTransitionsDsList);
        }

        private FromJiraDataSet getFromJiraDs(List<Issue> issuesVisibleToUser, ZoneId timezone) {
            LinkedList<Issue> issues = new LinkedList<>(issuesVisibleToUser);

            followUpBallparks = new LinkedHashMap<>();
            demandsByKey = makeDemandBallparks(issues, timezone);
            featuresByKey = makeFeatureBallparks(issues, timezone);

            final Map<String, Set<String>> extraFieldsHeaders = collectExtraFieldsHeaders();

            final List<FromJiraDataRow> result = makeSubtasks(issues, timezone);

            result.addAll(followUpBallparks.values());

            result.sort(Comparator.comparingInt((FromJiraDataRow f) -> f.demandStatusPriority)
                    .thenComparingLong(f -> f.demandPriorityOrder)
                    .thenComparingInt(f -> f.taskStatusPriority)
                    .thenComparingLong(f -> f.taskPriorityOrder)
                    .thenComparingInt(f -> f.subtaskStatusPriority)
                    .thenComparingLong(f -> f.subtaskPriorityOrder));

            return new FromJiraDataSet(Constants.FROMJIRA_HEADERS, extraFieldsHeaders, result);
        }

        private Map<String, Set<String>> collectExtraFieldsHeaders() {
            final Predicate<JiraCreateIssue.IssueTypeMetadata> filterBySubtask = t -> t.subtask;
            final Predicate<JiraCreateIssue.IssueTypeMetadata> filterByDemand = t -> jiraProperties.getIssuetype().getDemand().getId() == t.id;
            final Predicate<JiraCreateIssue.IssueTypeMetadata> filterByFeature = t -> !filterBySubtask.test(t) && !filterByDemand.test(t);

            final Map<String, Set<String>> extraFieldsHeaders = new LinkedHashMap<>();
            extraFieldsHeaders.put(TYPE_DEMAND, collectExtraFieldsHeaders(filterByDemand));
            extraFieldsHeaders.put(TYPE_FEATURES, collectExtraFieldsHeaders(filterByFeature));
            extraFieldsHeaders.put(TYPE_SUBTASKS, collectExtraFieldsHeaders(filterBySubtask));

            return extraFieldsHeaders;
        }

        private Set<String> collectExtraFieldsHeaders(Predicate<JiraCreateIssue.IssueTypeMetadata> featureTypeFilter) {
            final List<String> fields = jiraProperties.getExtraFields().getFieldIds();
            if(fields.isEmpty())
                return emptySet();

            return metadataService.getCreateIssueMetadata().values().stream()
                    .filter(featureTypeFilter)
                    .flatMap(t -> t.getFields().stream()
                            .map(f -> f.id)
                            .filter(fields::contains))
                    .collect(toLinkedHashSet());
        }
        
        private Map<String, Issue> makeDemandBallparks(LinkedList<Issue> issues, ZoneId timezone) {
            Map<String, Issue> demands = new LinkedHashMap<>();
            Iterator<Issue> it = issues.iterator();
            while(it.hasNext()) {
                Issue issue = it.next();
                if (issue.isDemand()) {
                    followUpBallparks.put(issue.getIssueKey(), createBallparkDemand(issue, timezone));
                    demands.put(issue.getIssueKey(), issue);
                    it.remove();
                }
            }
            return demands;
        }

        private Map<String, Issue> makeFeatureBallparks(LinkedList<Issue> issues, ZoneId timezone) {
            Map<String, Issue> features = new LinkedHashMap<String, Issue>();

            Iterator<Issue> it = issues.iterator();
            while(it.hasNext()) {
                Issue feature = it.next();
                if (!feature.isFeature())
                    continue;
                it.remove();

                Issue demand = demandsByKey.get(feature.getParent());
                if (demand != null)
                    followUpBallparks.remove(demand.getIssueKey());

                features.put(feature.getIssueKey(), feature);

                if (jiraProperties.getFollowup().getFeatureStatusThatDontGenerateBallpark().contains(feature.getStatus()))
                    continue;

                List<BallparkMapping> mappingList = getBallparksOrCry(feature);

                for (BallparkMapping mapping : mappingList) {
                    followUpBallparks.put(featureBallparkKey(feature, mapping), createBallparkFeature(demand, feature, mapping, timezone));
                }
            }
            return features;
        }


        private List<FromJiraDataRow> makeSubtasks(LinkedList<Issue> issues, ZoneId timezone) {

            final List<FromJiraDataRow> subtasksFollowups = new LinkedList<FromJiraDataRow>();
            Iterator<Issue> it = issues.iterator();
            while(it.hasNext()) {
                Issue issue = it.next();

                Issue feature = featuresByKey.get(issue.getParent());
                if (feature == null)
                    continue;

                Issue demand  = demandsByKey.get(feature.getParent());
                if (demand != null)
                    followUpBallparks.remove(demand.getIssueKey());

                FromJiraDataRow subtaskRow = createSubTaskFollowup(demand, feature, issue, timezone);
                subtasksFollowups.add(subtaskRow);

                if (jiraProperties.getFollowup().getSubtaskStatusThatDontPreventBallparkGeneration().contains(issue.getStatus()))
                    continue;

                List<BallparkMapping> mappingList = getBallparksOrCry(feature);
                for (BallparkMapping mapping : mappingList) {
                    if (mapping.getJiraIssueTypes().contains(issue.getType())) {
                        ensureMinimalFeatureEstimation(feature, mapping, subtaskRow);
                    }
                }
            }
            return subtasksFollowups;
        }

        /**
         * Ensure that the total estimation of a subtask type in a given feature never be less than the 
         * sizing originally estimated in this feature.<br>
         * 
         * E.g., a feature with "Dev" estimated as M. When a subtask of type "Dev" and size P is created 
         * the total estimation of this feature remains M. 
         */
        private void ensureMinimalFeatureEstimation(Issue feature, BallparkMapping mapping, FromJiraDataRow subtaskRow) {
            String featureBallparkKey = featureBallparkKey(feature, mapping);
            FromJiraDataRow featureBallpark = followUpBallparks.get(featureBallparkKey);
            
            if (featureBallpark == null)
                return;

            double effortEstimate = fromJiraRowCalculator.getEffortEstimate(subtaskRow);
            featureBallpark.taskBallpark -= effortEstimate;
            
            if (featureBallpark.taskBallpark <= 0)
                followUpBallparks.remove(featureBallparkKey);
        }

        private List<BallparkMapping> getBallparksOrCry(Issue issue) {
            List<BallparkMapping> mappings = issue.getActiveBallparkMappings();
            if (mappings == null) {
                throw new IllegalStateException(
                        "Ballpark mapping for issue type '"+issue.getIssueTypeName()+"' (id "+issue.getType()+") missing in configuration");
            }
            return mappings;
        }

        private FromJiraDataRow createBallparkDemand(Issue demand, ZoneId timezone) {
            FromJiraDataRow followUpData = new FromJiraDataRow();

            setDemandFields(followUpData, demand, timezone);

            followUpData.planningType = FromJiraDataRow.PLANNING_TYPE_BALLPARK;
            followUpData.project = demand.getProject();//NOSONAR demand is never null here
            followUpData.tshirtSize = "M";
            followUpData.worklog = 0.0;
            followUpData.wrongWorklog = timeSpentInHour(demand);
            followUpData.queryType = FromJiraDataRow.QUERY_TYPE_DEMAND_BALLPARK;

            followUpData.demandDescription = issueDescription("M", demand);

            followUpData.taskType = "BALLPARK - Demand";
            followUpData.taskStatus = getBallparkStatus();
            followUpData.taskId = 0L;
            followUpData.taskNum = followUpData.demandNum;
            followUpData.taskSummary="Dummy Feature";
            followUpData.taskDescription = issueDescription(0, demand.getSummary());
            followUpData.taskFullDescription = issueFullDescription("BALLPARK - Demand", "M", 0, demand.getSummary());
            followUpData.taskRelease = (String) defaultIfNull(getRelease(demand), "No release set");
            followUpData.taskBallpark = 0.0;

            followUpData.subtaskType = "BALLPARK - Demand";
            followUpData.subtaskStatus = followUpData.demandStatus;
            followUpData.subtaskId = 0L;
            followUpData.subtaskNum = demand.getProjectKey()+"-0";
            followUpData.subtaskSummary = followUpData.demandSummary;
            followUpData.subtaskDescription = issueDescription("M", 0, demand.getSummary());
            followUpData.subtaskFullDescription = issueFullDescription("BALLPARK - Demand", "M", 0, demand.getSummary());

            return followUpData;
        }

        private Double timeSpentInHour(Issue issue) {
            if (issue == null)
                return 0.0;
            
            Integer timeInSeconds = issue.getWorklogs().stream().map(w -> w.timeSpentSeconds).reduce(0, (a,b)->a+b);
            
            return toHour(timeInSeconds/60);
        }

        private Double originalEstimateInHour(Issue issue) {
            if (issue == null)
                return 0.0;
            if (issue.getTimeTracking() == null)
                return 0.0;
            if (!issue.getTimeTracking().getOriginalEstimateMinutes().isPresent())
                return 0.0;
            
            Integer minutes = issue.getTimeTracking().getOriginalEstimateMinutes().orElse(0);
            return toHour(minutes);
        }

        private FromJiraDataRow createBallparkFeature(Issue demand, Issue task, BallparkMapping ballparkMapping, ZoneId timezone) {
            FromJiraDataRow followUpData = new FromJiraDataRow();

            setDemandFields(followUpData, demand, timezone);
            setTaskFields(followUpData, task, timezone);

            followUpData.planningType = FromJiraDataRow.PLANNING_TYPE_BALLPARK;
            followUpData.project = task.getProject();
            followUpData.tshirtSize = task.getTshirtSizeOfSubtaskForBallpark(ballparkMapping);
            followUpData.worklog = 0.0;
            followUpData.wrongWorklog = timeSpentInHour(demand) + timeSpentInHour(task);
            followUpData.queryType = FromJiraDataRow.QUERY_TYPE_FEATURE_BALLPARK;

            if (demand != null) {
                followUpData.demandDescription = issueDescription("", demand);
            }

            followUpData.taskRelease = coalesce(getRelease(task), getRelease(demand) ,"No release set");

            followUpData.subtaskType = ballparkMapping.getIssueType();
            followUpData.subtaskStatus = getBallparkStatus();
            followUpData.subtaskId = 0L;
            followUpData.subtaskNum = task.getProjectKey()+"-0";
            followUpData.subtaskSummary = ballparkMapping.getIssueType();
            followUpData.subtaskDescription = issueDescription(0, task.getSummary());
            followUpData.subtaskFullDescription = issueFullDescription(ballparkMapping.getIssueType(), "", 0, task.getSummary());

            followUpData.taskBallpark = cluster.getClusterFor(followUpData.subtaskType, followUpData.tshirtSize)
                    .map(ci -> ci.getEffort())
                    .orElse(originalEstimateInHour(task));

            return followUpData;
        }

        private String getBallparkStatus() {
            return metadataService.getStatusById(jiraProperties.getFollowup().getBallparkDefaultStatus()).name;
        }

        private FromJiraDataRow createSubTaskFollowup(Issue demand, Issue task, Issue subtask, ZoneId timezone) {
            FromJiraDataRow followUpData = new FromJiraDataRow();

            setDemandFields(followUpData, demand, timezone);
            setTaskFields(followUpData, task, timezone);

            followUpData.planningType = FromJiraDataRow.PLANNING_TYPE_PLAN;
            followUpData.project = task.getProject();
            followUpData.tshirtSize = subtask.getTShirtSize() == null? "": subtask.getTShirtSize();
            followUpData.worklog = timeSpentInHour(subtask);
            followUpData.wrongWorklog = timeSpentInHour(demand) + timeSpentInHour(task);
            followUpData.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;

            if (demand != null) {
                followUpData.demandDescription = issueDescription("",demand);
            }

            followUpData.taskRelease = coalesce(getRelease(subtask), getRelease(task), getRelease(demand), "No release set");
            followUpData.taskBallpark = 0.0;
            followUpData.subtaskId = subtask.getId();
            followUpData.subtaskType = subtask.getIssueTypeName();
            followUpData.subtaskStatus = subtask.getStatusName();
            followUpData.subtaskNum = subtask.getIssueKey();
            followUpData.subtaskSummary = subtask.getSummary();
            followUpData.subtaskStatusPriority = subtask.getStatusPriority();
            followUpData.subtaskPriorityOrder = subtask.getPriorityOrder();
            followUpData.subtaskDescription = issueDescription(subtask);
            followUpData.subtaskFullDescription = subtask.getStatusName() + " > " + issueFullDescription(subtask);
            followUpData.subtaskStartDateStepMillis = subtask.getStartDateStepMillis();
            followUpData.subtaskAssignee = subtask.getAssignee().name;
            followUpData.subtaskDueDate = subtask.getDueDateByTimezoneId(timezone);
            followUpData.subtaskCreated = subtask.getCreatedDateByTimezoneId(timezone);
            followUpData.subtaskLabels = subtask.getLabels() != null ? subtask.getLabels().stream().collect(Collectors.joining(",")) : "";
            followUpData.subtaskComponents = subtask.getComponents() != null ? subtask.getComponents().stream().collect(Collectors.joining(",")) : "";
            followUpData.subtaskReporter = subtask.getReporter();
            followUpData.subtaskCoAssignees = subtask.getCoAssignees().stream().map(c -> c.name).collect(Collectors.joining(","));
            followUpData.subtaskClassOfService = subtask.getClassOfServiceValue();
            followUpData.subtaskUpdatedDate = subtask.getUpdatedDateByTimezoneId(timezone);
            followUpData.subtaskCycletime = subtask.getCycleTime(timezone).orElse(0D);
            followUpData.subtaskIsBlocked = subtask.isBlocked();
            followUpData.subtaskLastBlockReason = subtask.getLastBlockReason();
            followUpData.subtaskExtraFields = subtask.getExtraFields();
            followUpData.worklogs = subtask.getWorklogs();

            return followUpData;
        }

        private void setDemandFields(FromJiraDataRow followUpData, Issue demand, ZoneId timezone) {
            if (demand == null) return;

            followUpData.demandId = demand.getId();
            followUpData.demandType = demand.getIssueTypeName();
            followUpData.demandStatus = demand.getStatusName();
            followUpData.demandNum = demand.getIssueKey();
            followUpData.demandSummary = demand.getSummary();
            followUpData.demandStatusPriority = demand.getStatusPriority();
            followUpData.demandPriorityOrder = demand.getPriorityOrder();
            followUpData.demandUpdatedDate = demand.getUpdatedDateByTimezoneId(timezone);
            followUpData.demandBallpark = originalEstimateInHour(demand);
            followUpData.demandStartDateStepMillis = demand.getStartDateStepMillis();
            followUpData.demandAssignee = demand.getAssignee().name;
            followUpData.demandDueDate = demand.getDueDateByTimezoneId(timezone);
            followUpData.demandCreated = demand.getCreatedDateByTimezoneId(timezone);
            followUpData.demandLabels = demand.getLabels() != null ? demand.getLabels().stream().collect(Collectors.joining(",")) : "";
            followUpData.demandComponents = demand.getComponents() != null ? demand.getComponents().stream().collect(Collectors.joining(",")) : "";
            followUpData.demandReporter = demand.getReporter();
            followUpData.demandCoAssignees = demand.getCoAssignees().stream().map(c -> c.name).collect(Collectors.joining(","));
            followUpData.demandClassOfService = demand.getClassOfServiceValue();
            followUpData.demandCycletime = demand.getCycleTime(timezone).orElse(0D);
            followUpData.demandIsBlocked = demand.isBlocked();
            followUpData.demandLastBlockReason = demand.getLastBlockReason();
            followUpData.demandExtraFields = demand.getExtraFields();
        }

        private void setTaskFields(FromJiraDataRow followUpData, Issue task, ZoneId timezone) {
            followUpData.taskId = task.getId();
            followUpData.taskType = task.getIssueTypeName();
            followUpData.taskStatus = task.getStatusName();
            followUpData.taskNum = task.getIssueKey();
            followUpData.taskSummary = task.getSummary();
            followUpData.taskStatusPriority = task.getStatusPriority();
            followUpData.taskPriorityOrder = task.getPriorityOrder();
            followUpData.taskDescription = issueDescription(task);
            followUpData.taskFullDescription = issueFullDescription(task);
            followUpData.taskAdditionalEstimatedHours = task.getAdditionalEstimatedHours();
            followUpData.taskStartDateStepMillis = task.getStartDateStepMillis();
            followUpData.taskAssignee = task.getAssignee().name;
            followUpData.taskDueDate = task.getDueDateByTimezoneId(timezone);
            followUpData.taskCreated = task.getCreatedDateByTimezoneId(timezone);
            followUpData.taskLabels = task.getLabels() != null ? task.getLabels().stream().collect(Collectors.joining(",")) : "";
            followUpData.taskComponents = task.getComponents() != null ? task.getComponents().stream().collect(Collectors.joining(",")) : "";
            followUpData.taskReporter = task.getReporter();
            followUpData.taskCoAssignees = task.getCoAssignees().stream().map(c -> c.name).collect(Collectors.joining(","));
            followUpData.taskClassOfService = task.getClassOfServiceValue();
            followUpData.taskUpdatedDate = task.getUpdatedDateByTimezoneId(timezone);
            followUpData.taskCycletime = task.getCycleTime(timezone).orElse(0D);
            followUpData.taskIsBlocked = task.isBlocked();
            followUpData.taskLastBlockReason = task.getLastBlockReason();
            followUpData.taskExtraFields = task.getExtraFields();
        }

        private String getTshirtSize(Issue i) {
            if (i.isFeature()) return "";
            if (i.isDemand()) return "M";
            if (i.isSubTask())return i.getTShirtSize();
            return "";
        }

        private String getRelease(Issue i) {
            if (i == null || i.getRelease() == null)
                return null;

            return i.getRelease().name; 
        }

        private String issueDescription(Issue issue) {
            return issueDescription(getTshirtSize(issue), issue.getIssueKeyNum(), issue.getSummary());
        }

        private String issueDescription(String size, Issue issue) {
            return issueDescription(size, issue.getIssueKeyNum(), issue.getSummary());
        }

        private String issueDescription(Integer issueNum, String description) {
            return issueDescription(null, issueNum, description);
        }

        private String issueDescription(String size, Integer issueNum, String description) {
            String sizePart = StringUtils.isEmpty(size)?"":size+" | ";
            return String.format("%s%05d - %s", sizePart, issueNum, description);
        }

        private String issueFullDescription(Issue issue) {
            return issue.getIssueTypeName() +
                    " | " + issueDescription(getTshirtSize(issue), issue.getIssueKeyNum(), issue.getSummary());
        }

        private String issueFullDescription(String issueType, String size, Integer issueNum, String description) {
            return issueType + " | " +issueDescription(size, issueNum, description);
        }

        private <T> T coalesce(@SuppressWarnings("unchecked") T ...items) {
            for(T i : items) if(i != null) return i;
            return null;
        }
    }

    private static String featureBallparkKey(Issue feature, BallparkMapping mapping) {
        return feature.getIssueKey() + mapping.getTshirtCustomFieldId();
    }

    private Double toHour(Integer minutes) {
        return minutes / 60.0;
    }

}
