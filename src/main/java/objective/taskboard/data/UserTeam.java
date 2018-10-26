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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USER_TEAM")
public class UserTeam implements Serializable {

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

    @Enumerated(EnumType.STRING)
    private UserTeamRole role;

    protected UserTeam() {}

    public UserTeam(String memberName, String teamName) {
        this(memberName, teamName, UserTeamRole.MEMBER);
    }

    public UserTeam(String memberName, String teamName, UserTeamRole role) {
        this.userName = memberName;
        this.team = teamName;
        this.role = role;
    }

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

    public UserTeamRole getRole() {
        return role;
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

    public void setRole(UserTeamRole role) {
        this.role = role;
    }

    public static enum UserTeamRole {
        MANAGER,
        MEMBER,
        VIEWER
    }

}
