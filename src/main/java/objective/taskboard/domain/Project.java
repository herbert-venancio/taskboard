package objective.taskboard.domain;

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

import java.util.List;

import com.atlassian.jira.rest.client.api.domain.BasicProject;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Project {
    
    private String key;
    private String name;
    private List<Long> teamsIds;
    
    public static Project from(BasicProject basicProject, ProjectFilterConfiguration projectFilterConfiguration) {
        Project project = new Project();
        project.setKey(basicProject.getKey());
        project.setName(basicProject.getName());
        project.setTeamsIds(projectFilterConfiguration.getTeamsIds());
        
        return project;
    }

}
