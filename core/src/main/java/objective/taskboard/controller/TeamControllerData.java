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
import java.util.List;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;

public class TeamControllerData {
    public String teamName;
    public String manager;
    public List<String> teamMembers = new ArrayList<>();
    public Long id;
    
    public TeamControllerData(Team team ) {
        teamName = team.getName();
        manager = team.getManager();
        id = team.getId();
        for (UserTeam member : team.getMembers()) {
            teamMembers.add(member.getUserName());
        }
    }
    
    public TeamControllerData() { }
}
