package objective.taskboard.jira;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.BasicProject;

@Service
public class ProjectService {

    @Autowired
    private ProjectCache projectCache;

    public List<BasicProject> getProjects() {
        return projectCache.getProjects()
                .values()
                .stream()
                .sorted(comparing(BasicProject::getName))
                .collect(toList());
    }
    
    public Optional<BasicProject> getProject(String key) {
        return Optional.ofNullable(projectCache.getProjects().get(key));
    }

    public boolean isProjectVisible(String projectKey) {
        return projectCache.getProjects().containsKey(projectKey);
    }
}
