package objective.taskboard.domain;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PROJECT_TEAM")
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