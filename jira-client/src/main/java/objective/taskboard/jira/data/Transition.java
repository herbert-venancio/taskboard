package objective.taskboard.jira.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import objective.taskboard.jira.client.JiraCreateIssue;

public class Transition {

    public Long id;
    public String name;
    public Status to;
    private Map<String, JiraCreateIssue.FieldInfoMetadata> fields;

    public Transition(){}

    public Transition(Long id, String name, Status to, Map<String, JiraCreateIssue.FieldInfoMetadata> fields) {
        this.id = id;
        this.name = name;
        this.to = to;
        this.fields = fields;
    }

    public List<JiraCreateIssue.FieldInfoMetadata> getFields() {
        return fields.entrySet().stream()
                .map(entry -> {
                    entry.getValue().id = entry.getKey();
                    return entry.getValue();
                })
                .collect(Collectors.toList());
    }

}