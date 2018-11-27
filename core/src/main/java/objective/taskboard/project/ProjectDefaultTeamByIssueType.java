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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((issueTypeId == null) ? 0 : issueTypeId.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        result = prime * result + ((teamId == null) ? 0 : teamId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectDefaultTeamByIssueType other = (ProjectDefaultTeamByIssueType) obj;
        if (issueTypeId == null) {
            if (other.issueTypeId != null)
                return false;
        } else if (!issueTypeId.equals(other.issueTypeId))
            return false;
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;
        if (teamId == null) {
            if (other.teamId != null)
                return false;
        } else if (!teamId.equals(other.teamId))
            return false;
        return true;
    }

}
