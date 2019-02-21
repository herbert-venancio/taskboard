package objective.taskboard.jira.data;

import java.util.Map;

import objective.taskboard.jira.client.JiraCreateIssue;

public class Transition {

    public Long id;
    public String name;
    public Status to;
    public Map<String, JiraCreateIssue.FieldInfoMetadata> fields;

    public Transition(){}

    public Transition(Long id, String name, Status to, Map<String, JiraCreateIssue.FieldInfoMetadata> fields) {
        this.id = id;
        this.name = name;
        this.to = to;
        this.fields = fields;
    }

}