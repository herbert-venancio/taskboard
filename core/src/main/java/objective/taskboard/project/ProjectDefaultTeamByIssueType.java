package objective.taskboard.project;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "project_default_team_issuetype")
public class ProjectDefaultTeamByIssueType extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch=FetchType.EAGER)
    private ProjectFilterConfiguration project;

    private Long teamId;

    private Long issueTypeId;

    protected ProjectDefaultTeamByIssueType() {} //JPA

    public ProjectDefaultTeamByIssueType(ProjectFilterConfiguration project, Long teamId, Long issueTypeId) {
        this.project = project;
        this.setTeamId(teamId);
        this.setIssueTypeId(issueTypeId);
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getIssueTypeId() {
        return issueTypeId;
    }

    public void setIssueTypeId(Long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

}
