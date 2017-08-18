/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
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

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

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

    public TeamFilterConfiguration() {
    }

    @java.beans.ConstructorProperties({"teamId"})
    public TeamFilterConfiguration(final long teamId) {
        this.teamId = teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TeamFilterConfiguration that = (TeamFilterConfiguration) o;

        return teamId == that.teamId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (teamId ^ (teamId >>> 32));
        return result;
    }
}
