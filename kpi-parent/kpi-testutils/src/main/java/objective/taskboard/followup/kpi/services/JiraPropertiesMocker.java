package objective.taskboard.followup.kpi.services;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.services.KpiEnvironment.IssueTypeDTO;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.Followup;
import objective.taskboard.jira.properties.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.jira.properties.StatusConfiguration.FinalStatuses;
import objective.taskboard.jira.properties.StatusConfiguration.StatusCountingOnWip;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;

public class JiraPropertiesMocker {
    private JiraProperties jiraProperties;
    private StatusPriorityOrder statusOrder = new StatusPriorityOrder();
    private KpiEnvironment environment;
    private FollowUpMocker followUpMocker = new FollowUpMocker();
    private FinalStatusesMocker finalStatusesMocker = new FinalStatusesMocker();
    private StatusCountingOnWipMocker statusCountingOnWipMocker = new StatusCountingOnWipMocker();

    public JiraPropertiesMocker(KpiEnvironment kpiEnvironment) {
        this.environment = kpiEnvironment;
    }

    public JiraProperties getJiraProperties() {
        if(jiraProperties == null) {
            jiraProperties = prepareJiraProperties();
        }
        return jiraProperties;
    }

    private JiraProperties prepareJiraProperties() {
        JiraProperties jiraProperties = new JiraProperties();
        jiraProperties.setIssuetype(buildTypes());
        jiraProperties.setStatusPriorityOrder(statusOrder);
        jiraProperties.setFinalStatuses(finalStatusesMocker.getFinalStatuses());
        jiraProperties.setStatusCountingOnWip(statusCountingOnWipMocker.getStatusCountingOnWip());
        configureFollowup(jiraProperties);
        return jiraProperties;
    }

    private void configureFollowup(JiraProperties jiraProperties) {
        Followup followup = new Followup();
        followup.setStatusExcludedFromFollowup(getStatusesIdsFromNames(followUpMocker.statusesNames));
        jiraProperties.setFollowup(followup);
    }

    private List<Long> getStatusesIdsFromNames(List<String> statusesNames) {
        return statusesNames.stream()
                .map(s -> environment.getStatus(s))
                .map(s -> s.id())
                .collect(Collectors.toList());
    }

    private JiraProperties.IssueType buildTypes() {
        JiraProperties.IssueType types = new JiraProperties.IssueType();

        List<IssueTypeDTO> demands = environment.types().configuredForLevel(KpiLevel.DEMAND);
        if(demands.size() > 1)
            Assert.fail("There should be only one Demand configured on Environment");
        demands.forEach( d-> types.setDemand(new IssueTypeDetails(d.id())));

        types.setFeatures(getIssueTypeDetails(KpiLevel.FEATURES));
        types.setSubtasks(getIssueTypeDetails(KpiLevel.SUBTASKS));
        return types;
    }

    private List<IssueTypeDetails> getIssueTypeDetails(KpiLevel level) {
        return environment.types()
                .configuredForLevel(level).stream()
                .map(t -> new IssueTypeDetails(t.id()))
                .collect(Collectors.toList());
    }
    
    public FinalStatusesMocker finalStatuses() {
        return finalStatusesMocker;
    }
    
    public StatusCountingOnWipMocker statusCountingOnWip() {
        return statusCountingOnWipMocker;
    }

    public JiraPropertiesMocker withSubtaskStatusPriorityOrder(String ...statusesOrdered) {
        this.statusOrder.setSubtasks(statusesOrdered);
        return this;
    }

    public JiraPropertiesMocker withDemandStatusPriorityOrder(String ...statusesOrdered) {
        this.statusOrder.setDemands(statusesOrdered);
        return this;
    }

    public JiraPropertiesMocker withFeaturesStatusPriorityOrder(String ...statusesOrdered) {
        this.statusOrder.setTasks(statusesOrdered);
        return this;
    }

    public KpiEnvironment eoJp() {
        return environment;
    }

    public FollowUpMocker followUp() {
        return followUpMocker;
    }
    
    public class StatusCountingOnWipMocker{
        private Map<KpiLevel,List<String>> statuses = new EnumMap<>(KpiLevel.class);
        
        private StatusCountingOnWipMocker() {
            Stream.of(KpiLevel.values()).forEach( l-> statuses.put(l,new LinkedList<>()));
        }
        
        public StatusCountingOnWipMocker onDemand(String...demand) {
            statuses.get(KpiLevel.DEMAND).addAll(asList(demand));
            return this;
        }
        
        public StatusCountingOnWipMocker onFeature(String...features) {
            statuses.get(KpiLevel.FEATURES).addAll(asList(features));
            return this;
        }

        public StatusCountingOnWipMocker onSubtasks(String...subtasks) {
            statuses.get(KpiLevel.SUBTASKS).addAll(asList(subtasks));
            return this;
        }

        private StatusCountingOnWip getStatusCountingOnWip() {
            StatusCountingOnWip configuration = new StatusCountingOnWip();
            configuration.setDemands(getValuesConfigured(KpiLevel.DEMAND));
            configuration.setTasks(getValuesConfigured(KpiLevel.FEATURES));
            configuration.setSubtasks(getValuesConfigured(KpiLevel.SUBTASKS));
            return configuration;
        }

        private String[] getValuesConfigured(KpiLevel level) {
            return statuses.get(level).toArray(new String[] {});
        }

        public JiraPropertiesMocker eoSCW() {
            return JiraPropertiesMocker.this;
        }

    }
    
    public class FinalStatusesMocker{
        private Map<KpiLevel,List<String>> statuses = new EnumMap<>(KpiLevel.class);

        public FinalStatusesMocker onDemand(String...demand) {
            statuses.putIfAbsent(KpiLevel.DEMAND, new LinkedList<>());
            statuses.get(KpiLevel.DEMAND).addAll(asList(demand));
            return this;
        }

        public FinalStatusesMocker onFeature(String...features) {
            statuses.putIfAbsent(KpiLevel.FEATURES, new LinkedList<>());
            statuses.get(KpiLevel.FEATURES).addAll(asList(features));
            return this;
        }

        public FinalStatusesMocker onSubtasks(String...subtasks) {
            statuses.putIfAbsent(KpiLevel.SUBTASKS, new LinkedList<>());
            statuses.get(KpiLevel.SUBTASKS).addAll(asList(subtasks));
            return this;
        }

        private FinalStatuses getFinalStatuses() {
            FinalStatuses configuration = new FinalStatuses();
            configuration.setDemands(statuses.getOrDefault(KpiLevel.DEMAND, Collections.emptyList()).toArray(new String[] {}));
            configuration.setTasks(statuses.getOrDefault(KpiLevel.FEATURES, Collections.emptyList()).toArray(new String[] {}));
            configuration.setSubtasks(statuses.getOrDefault(KpiLevel.SUBTASKS, Collections.emptyList()).toArray(new String[] {}));
            return configuration;
        }

        public JiraPropertiesMocker eoFS() {
            return JiraPropertiesMocker.this;
        }
    }

    public class FollowUpMocker {

        private List<String> statusesNames = new LinkedList<>();

        public FollowUpMocker withExcludedStatuses(String ...statuses) {
            this.statusesNames = Arrays.asList(statuses);
            return this;
        }
        
        public List<String> statusesExcluded(){
            return statusesNames;
        }

        public JiraPropertiesMocker eof() {
            return JiraPropertiesMocker.this;
        }
    }
}
