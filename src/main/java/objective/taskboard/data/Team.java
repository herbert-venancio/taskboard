package objective.taskboard.data;

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

import java.io.Serializable;
import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "TEAM")
public class Team implements Serializable {
    private static final long serialVersionUID = 1794216649849732935L;

    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String coach;

    private String manager;

    private Date createdAt;

    private Date updatedAt;

    private String nickName;

    private String coachUserName;

    private String jiraEquipe;

    private String jiraSubequipe;
    
    private boolean globallyVisible;

    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="team", referencedColumnName="name")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<UserTeam> members;

    public Team() {
    }

    public Team(Long id, String name, String coach, String manager, Date createdAt, Date updatedAt, String nickName,
            String coachUserName, String jiraEquipe, String jiraSubequipe, List<UserTeam> members) {
        super();
        this.id = id;
        this.name = name;
        this.coach = coach;
        this.manager = manager;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.nickName = nickName;
        this.coachUserName = coachUserName;
        this.jiraEquipe = jiraEquipe;
        this.jiraSubequipe = jiraSubequipe;
        this.members = members;
    }
    public Team(String name, String manager, String coach, List<String> members) {
        this(name, manager, coach, members, false);
    }

    public Team(String name, String manager, String coach, List<String> members, boolean globallyVisible) {
        this.name = name;
        this.manager = manager;
        this.coach = coach;
        this.members = stringListToUserTeamList(members);
        this.globallyVisible = globallyVisible;
    }

    private List<UserTeam> stringListToUserTeamList(List<String> members) {
        return members.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(teamMember -> new UserTeam(teamMember, getName()))
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoach() {
        return coach;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getCoachUserName() {
        return coachUserName;
    }

    public void setCoachUserName(String coachUserName) {
        this.coachUserName = coachUserName;
    }

    public String getJiraEquipe() {
        return jiraEquipe;
    }

    public void setJiraEquipe(String jiraEquipe) {
        this.jiraEquipe = jiraEquipe;
    }

    public String getJiraSubequipe() {
        return jiraSubequipe;
    }

    public void setJiraSubequipe(String jiraSubequipe) {
        this.jiraSubequipe = jiraSubequipe;
    }

    public List<UserTeam> getMembers() {
        return members;
    }

    public void setMembers(List<UserTeam> members) {
        this.members = members;
    }
    
    public boolean isGloballyVisible() {
        return globallyVisible;
    }
    
    public void setGloballyVisible(boolean globallyVisible) {
        this.globallyVisible = globallyVisible;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Team other = (Team) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
