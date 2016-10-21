package objective.taskboard.jira;

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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.BasicProject;

import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Service
public class ProjectVisibilityService {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfiguration;

    @Autowired
    private ProjectMetadataService projectMetadataService;
    
//    @Autowired
//    private ProjectUsersVisibilityService projectUsersVisibilityService;

    public List<BasicProject> getProjectsVisibleToUser(String user) {
        return projectFilterConfiguration.getProjects().stream()
                    .map(t -> projectMetadataService.getProjectMetadata(t.getProjectKey()))
                    .filter(t -> isProjectVisibleForUser(t.getKey(), user))
                    .collect(Collectors.toList());
    }

    public boolean isProjectVisibleForUser(String projectKey, String user) {
        return true;
//        return projectUsersVisibilityService.getProjectUsers().get(projectKey).contains(user);
    }

}
