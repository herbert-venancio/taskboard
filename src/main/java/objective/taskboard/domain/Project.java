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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Project {

    private String key;
    private String name;
    private List<Long> teamsIds;
    private List<String> versions;

    public static Project from(com.atlassian.jira.rest.client.api.domain.Project jiraProject,
            ProjectFilterConfiguration projectFilterConfiguration) {
        Project project = new Project();
        project.setKey(jiraProject.getKey());
        project.setName(jiraProject.getName());
        project.setTeamsIds(projectFilterConfiguration.getTeamsIds());

        List<String> versions = newArrayList();
        if (jiraProject.getVersions() != null)
            versions = newArrayList(jiraProject.getVersions()).stream()
                            .map(v -> v.getName())
                            .collect(toList());

        project.setVersions(versions);
        return project;
    }

}
