package objective.taskboard.followup.kpi.enviroment;

import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.Followup;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;

public class JiraPropertiesMocker {
    private JiraProperties jiraProperties = new JiraProperties();
    private String[] statusesOrdered;
    private KpiEnvironment environment;
    
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
        statusOrder.setSubtasks(statusesOrdered);
        jiraProperties.setStatusPriorityOrder(statusOrder);
    }

    public JiraPropertiesMocker withSubtaskStatusPriorityOrder(String ...statusesOrdered) {
        this.statusesOrdered = statusesOrdered;
        return this;
    }

    public KpiEnvironment eoJp() {
        return environment;
    }

    
}
