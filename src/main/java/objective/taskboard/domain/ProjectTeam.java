package objective.taskboard.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import objective.taskboard.Constants;

@Data
@Entity
@Table(schema = Constants.SCHEMA_MAD, name = "PROJECT_TEAM")
public class ProjectTeam {
    @Id
    @Column(name="project_key")
    private String projectKey;
    
    @Column(name="team_id")
    private Long teamId; 
}
