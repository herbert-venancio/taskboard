package objective.taskboard.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "TEAM")
public class Team extends TaskboardEntity implements Serializable {
    private static final long serialVersionUID = 1794216649849732935L;

    private String name;

    private String coach;

    private String manager;

    private String nickName;

    private String coachUserName;

    private String jiraEquipe;

    private String jiraSubequipe;
    
    private boolean globallyVisible;

    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="team", referencedColumnName="name")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<UserTeam> members = new ArrayList<>();

    public Team() {
    }

    public Team(Long id, String name, String coach, String manager, Instant createdAt, Instant updatedAt, String nickName,
                String coachUserName, String jiraEquipe, String jiraSubequipe, List<UserTeam> members) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.coach = coach;
        this.manager = manager;
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
        this.globallyVisible = globallyVisible;
        addMembers(members);
    }

    private void addMembers(List<String> membersName) {
        membersName.stream()
            .filter(Objects::nonNull)
            .distinct()
            .forEach(this::addMember);
    }

    public UserTeam addMember(String memberName) {
        UserTeam member = new UserTeam(memberName, getName(), UserTeamRole.MEMBER);
        this.members.add(member);
        return member;
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
    public String toString() {
        return name;
    }

}
