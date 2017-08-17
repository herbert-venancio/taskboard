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

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "wip_config")
public class WipConfiguration extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = -7054505295750531721L;

    protected String team;
    protected String status;
    protected Integer wip;

    public WipConfiguration() {
    }

    public WipConfiguration(String team, String status, Integer wip) {
        super();
        this.team = team;
        this.status = status;
        this.wip = wip;
    }

    public String getTeam() {
        return team;
    }
    public void setTeam(String team) {
        this.team = team;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Integer getWip() {
        return wip;
    }
    public void setWip(Integer wip) {
        this.wip = wip;
    }

}
