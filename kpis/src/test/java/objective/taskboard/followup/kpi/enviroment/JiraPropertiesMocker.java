package objective.taskboard.followup.kpi.enviroment;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment.IssueTypeDTO;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.Followup;
import objective.taskboard.jira.properties.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;

public class JiraPropertiesMocker {
    private JiraProperties jiraProperties;
    private StatusPriorityOrder statusOrder = new StatusPriorityOrder();
    private KpiEnvironment environment;
    private FollowUpMocker followUpMocker = new FollowUpMocker();

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

    public class FollowUpMocker {

        private List<String> statusesNames = new LinkedList<>();

        public FollowUpMocker withExcludedStatuses(String ...statuses) {
            this.statusesNames = Arrays.asList(statuses);
            return this;
        }

        public JiraPropertiesMocker eof() {
            return JiraPropertiesMocker.this;
        }
    }
}
