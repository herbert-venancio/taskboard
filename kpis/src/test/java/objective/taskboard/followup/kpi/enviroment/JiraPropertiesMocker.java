package objective.taskboard.followup.kpi.enviroment;

import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.Followup;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;

public class JiraPropertiesMocker {
    private JiraProperties jiraProperties = new JiraProperties();
    private KpiEnvironment environment;
    private String[] subtasksStatusesOrdered;
    private String[] featuresStatusesOrdered;
    private String[] demandStatusesOrdered;

    public JiraPropertiesMocker(KpiEnvironment kpiEnvironment) {
        this.environment = kpiEnvironment;
    }

    public JiraProperties getJiraProperties() {
        mockStatusPriorityOrder();
        mockFollowup();
        return jiraProperties;
    }

    private void mockFollowup() {
        jiraProperties.setFollowup(new Followup());
    }

    private void mockStatusPriorityOrder() {
        StatusPriorityOrder statusOrder = new StatusPriorityOrder();
        statusOrder.setSubtasks(subtasksStatusesOrdered);
        statusOrder.setTasks(featuresStatusesOrdered);
        statusOrder.setDemands(demandStatusesOrdered);
        jiraProperties.setStatusPriorityOrder(statusOrder);
    }

    public JiraPropertiesMocker withSubtaskStatusPriorityOrder(String ...statusesOrdered) {
        this.subtasksStatusesOrdered = statusesOrdered;
        return this;
    }

    public JiraPropertiesMocker withFeaturesStatusPriorityOrder(String ...statusesOrdered) {
        this.featuresStatusesOrdered = statusesOrdered;
        return this;
    }

    public JiraPropertiesMocker withDemandStatusPriorityOrder(String ...statusesOrdered) {
        this.demandStatusesOrdered = statusesOrdered;
        return this;
    }

    public KpiEnvironment eoJp() {
        return environment;
    }


}
