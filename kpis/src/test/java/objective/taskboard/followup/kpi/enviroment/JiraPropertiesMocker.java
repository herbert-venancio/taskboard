package objective.taskboard.followup.kpi.enviroment;

import org.mockito.Mockito;

import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;

public class JiraPropertiesMocker {
    private JiraProperties jiraProperties = Mockito.mock(JiraProperties.class);
    private String[] statusesOrdered;
    private KpiEnvironment fatherEnvironment;
    public JiraPropertiesMocker(KpiEnvironment kpiEnvironment) {
        this.fatherEnvironment = kpiEnvironment;
    }

    public JiraProperties getJiraProperties() {
        mockStatusPriorityOrder();
        return jiraProperties;
    }

    private void mockStatusPriorityOrder() {
        StatusPriorityOrder statusOrder = new StatusPriorityOrder();
        statusOrder.setSubtasks(statusesOrdered);
        Mockito.when(jiraProperties.getStatusPriorityOrder()).thenReturn(statusOrder);
    }

    public JiraPropertiesMocker withSubtaskStatusPriorityOrder(String ...statusesOrdered) {
        this.statusesOrdered = statusesOrdered;
        return this;
    }

    public KpiEnvironment eoJp() {
        return fatherEnvironment;
    }
}
