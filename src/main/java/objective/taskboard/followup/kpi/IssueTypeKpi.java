package objective.taskboard.followup.kpi;

import objective.taskboard.jira.client.JiraIssueTypeDto;

public class IssueTypeKpi {
    private Long id;
    private String type;

    public IssueTypeKpi(JiraIssueTypeDto dto) {
        this.id = dto.getId();
        this.type = dto.getName();
    }
    
    public IssueTypeKpi(Long id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }
    
}
