package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import objective.taskboard.Constants;

@Entity
@Table(schema = Constants.SCHEMA_MAD, name = "PROJECT_TEAM")
public class ProjectTeam {
    @EmbeddedId
    ProjectTeamId id;
    
    public ProjectTeam() {
        id = new ProjectTeamId();
    }
    
    public String getProjectKey() {
        return id.projectKey;
    }
    
    public void setProjectKey(String key) {
        id.projectKey = key;
    }

    public Long getTeamId() { 
        return id.teamId; 
    }
    
    public void setTeamId(Long teamId) {
        id.teamId = teamId;
    }

}
    
@Embeddable
class ProjectTeamId implements Serializable {
    private static final long serialVersionUID = -7879137502491962905L;

    String projectKey;
    
    Long teamId;
}