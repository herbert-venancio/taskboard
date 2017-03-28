package objective.taskboard.controller;

import java.util.ArrayList;
import java.util.List;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;

public class TeamControllerData {
    public String teamName;
    public String manager;
    public List<String> teamMembers = new ArrayList<>();
    
    public TeamControllerData(Team team ) {
        teamName = team.getName();
        manager = team.getManager();
        for (UserTeam member : team.getMembers()) {
            teamMembers.add(member.getUserName());
        }
    }
    
    public TeamControllerData() { }
}
