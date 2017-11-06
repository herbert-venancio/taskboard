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
package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "wip_config")
public class WipConfiguration extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = -7054505295750531721L;

    private String team;
    private Integer wip;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step")
    private Step step;

    protected WipConfiguration() {
    }

    public WipConfiguration(String team, Step step, Integer wip) {
        this.team = team;
        this.step = step;
        this.wip = wip;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Integer getWip() {
        return wip;
    }

    public void setWip(Integer wip) {
        this.wip = wip;
    }

    public Step getStep() {
        return step;
    }
    
    public boolean isApplicable(long issueTypeId, long statusId) {
        return getStep().getFilters().stream().anyMatch(f -> f.isApplicable(issueTypeId, statusId));
    }

}