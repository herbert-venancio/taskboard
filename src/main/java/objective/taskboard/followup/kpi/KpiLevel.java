package objective.taskboard.followup.kpi;

import java.util.stream.Stream;

import objective.taskboard.data.Issue;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;

public enum KpiLevel {
    
    SUBTASKS("Subtasks") {
        @Override
        public String[] getStatusPriorityOrder(JiraProperties jiraProperties) {
            return jiraProperties.getStatusPriorityOrder().getSubtasksInOrder();
        }

        boolean accept(JiraProperties jiraProperties, JiraIssueTypeDto issueType) {
            return jiraProperties.getIssuetype().getSubtasks().stream().anyMatch(ft -> ft.getId() == issueType.getId());
        }
    },
    
    FEATURES("Features") {
        @Override
        public String[] getStatusPriorityOrder(JiraProperties jiraProperties) {
            return jiraProperties.getStatusPriorityOrder().getTasksInOrder();
        }

        boolean accept(JiraProperties jiraProperties, JiraIssueTypeDto issueType) {
            return jiraProperties.getIssuetype().getFeatures().stream().anyMatch(ft -> ft.getId() == issueType.getId());
        }

    },
    DEMAND("Demand") {
        @Override
        public String[] getStatusPriorityOrder(JiraProperties jiraProperties) {
            return jiraProperties.getStatusPriorityOrder().getDemandsInOrder();
        }

        boolean accept(JiraProperties jiraProperties, JiraIssueTypeDto issueType) {
            return jiraProperties.getIssuetype().getDemand().getId() == issueType.getId();
        }

    },
    UNMAPPED(""){
        @Override
        public String[] getStatusPriorityOrder(JiraProperties jiraProperties) {
            return new String[]{};
        }

        boolean accept(JiraProperties jiraProperties, JiraIssueTypeDto issueType) {
            return true;
        }
    };
    
    private String name;

    private KpiLevel(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public abstract String[] getStatusPriorityOrder(JiraProperties jiraProperties);

    public static KpiLevel of(Issue issue) {
        if(issue.isDemand())
            return KpiLevel.DEMAND;
        if(issue.isFeature())
            return KpiLevel.FEATURES;
        if(issue.isSubTask())
            return SUBTASKS;
        return UNMAPPED;
    }

    public static KpiLevel given(JiraProperties jiraProperties, JiraIssueTypeDto issueType) {
        return Stream.of(values()).filter(v-> v.accept(jiraProperties,issueType)).findFirst().orElse(UNMAPPED);
    }
    
    abstract boolean accept(JiraProperties jiraProperties, JiraIssueTypeDto issueType);
    
}
