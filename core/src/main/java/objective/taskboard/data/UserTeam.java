package objective.taskboard.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "USER_TEAM")
public class UserTeam extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userName;

    private String team;

    private Date endDate;

    private Integer isEspecificador;

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

    public UserTeamRole getRole() {
        return role;
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

    public void setRole(UserTeamRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return userName + " - " + team + "(" + role + ")";
    }


    public enum UserTeamRole {
        MANAGER,
        MEMBER,
        VIEWER
    }

}
