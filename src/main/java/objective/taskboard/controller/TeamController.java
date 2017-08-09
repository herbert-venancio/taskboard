package objective.taskboard.controller;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.repository.TeamCachedRepository;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    
    @Autowired
    private TeamCachedRepository teamRepo;
    
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TeamControllerData>> getTeams() {
        List<Team> cache = teamRepo.getCache();
        ArrayList<TeamControllerData> availableTeams = new ArrayList<>();
        for (Team team : cache) {
            availableTeams.add(new TeamControllerData(team));
        }
        
        return new ResponseEntity<List<TeamControllerData>>(availableTeams, HttpStatus.OK);
    }
    
    @RequestMapping(path="{teamName}", method = RequestMethod.GET)
    public ResponseEntity<TeamControllerData> getTeamMembers(@PathVariable("teamName") String teamName) {
        Team team = teamRepo.findByName(teamName);
        if (team == null) 
            return new ResponseEntity<TeamControllerData>(HttpStatus.NOT_FOUND);
        
        TeamControllerData teamController = new TeamControllerData(team);
        
        return new ResponseEntity<TeamControllerData>(teamController, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.PATCH, consumes="application/json")
    public ResponseEntity<Void> updateTeamMembers(@RequestBody TeamControllerData [] data) {
        for (TeamControllerData teamData : data)
            updateTeamMembers(teamData.teamName, teamData);

        return ResponseEntity.ok().build();
    }
    
    @RequestMapping(path="{teamName}", method = RequestMethod.PATCH, consumes="application/json")
    public ResponseEntity<Void> updateTeamMembers(@PathVariable("teamName") String teamName, @RequestBody TeamControllerData data) {
        Team team = teamRepo.findByName(teamName);
        if (team == null) 
            return ResponseEntity.notFound().build();
        
        if (data.manager != null)
            team.setManager(data.manager);
        
        Iterator<UserTeam> it = team.getMembers().iterator();
        while(it.hasNext()) {
            UserTeam userInTeam = it.next();
            if (!data.teamMembers.contains(userInTeam.getUserName()))
                it.remove();
            else
                data.teamMembers.remove(userInTeam.getUserName());
        }
        for (String memberName : data.teamMembers) 
            team.getMembers().add(new UserTeam(memberName, teamName));
        
        teamRepo.save(team);
        
        return ResponseEntity.ok().build();
    }
            
}
