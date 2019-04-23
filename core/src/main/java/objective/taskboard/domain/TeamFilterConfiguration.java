package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "team_filter_configuration")
public class TeamFilterConfiguration extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private long teamId;

    public long getTeamId() {
        return this.teamId;
    }

    public void setTeamId(final long teamId) {
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return "TeamFilterConfiguration{" +
                "teamId=" + teamId +
                '}';
    }

}
