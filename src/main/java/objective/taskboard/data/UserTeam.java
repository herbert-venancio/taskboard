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
package objective.taskboard.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USER_TEAM")
public class UserTeam implements Serializable {
    public UserTeam(String memberName, String teamName) {
        this.userName = memberName;
        this.team = teamName;
    }

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    private String userName;

    private String team;

    private Date endDate;

    private Integer isEspecificador;

    private Date createdAt;

    private Date updatedAt;

    public Long getId() {
        return this.id;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getTeam() {
        return this.team;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public Integer getIsEspecificador() {
        return this.isEspecificador;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setTeam(final String team) {
        this.team = team;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public void setIsEspecificador(final Integer isEspecificador) {
        this.isEspecificador = isEspecificador;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserTeam userTeam = (UserTeam) o;

        if (id != null ? !id.equals(userTeam.id) : userTeam.id != null) return false;
        if (userName != null ? !userName.equals(userTeam.userName) : userTeam.userName != null) return false;
        if (team != null ? !team.equals(userTeam.team) : userTeam.team != null) return false;
        if (endDate != null ? !endDate.equals(userTeam.endDate) : userTeam.endDate != null) return false;
        if (isEspecificador != null ? !isEspecificador.equals(userTeam.isEspecificador) : userTeam.isEspecificador != null)
            return false;
        if (createdAt != null ? !createdAt.equals(userTeam.createdAt) : userTeam.createdAt != null) return false;
        return updatedAt != null ? updatedAt.equals(userTeam.updatedAt) : userTeam.updatedAt == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (team != null ? team.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (isEspecificador != null ? isEspecificador.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserTeam{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", team='" + team + '\'' +
                ", endDate=" + endDate +
                ", isEspecificador=" + isEspecificador +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public UserTeam() {
    }

    @java.beans.ConstructorProperties({"id", "userName", "team", "endDate", "isEspecificador", "createdAt", "updatedAt"})
    public UserTeam(final Long id, final String userName, final String team, final Date endDate, final Integer isEspecificador, final Date createdAt, final Date updatedAt) {
        this.id = id;
        this.userName = userName;
        this.team = team;
        this.endDate = endDate;
        this.isEspecificador = isEspecificador;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
