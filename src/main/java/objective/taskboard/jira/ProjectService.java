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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.domain.CimProject;

import objective.taskboard.domain.Project;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;

@Service
public class ProjectService {

    @Autowired
    private ProjectCache projectCache;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    public List<Project> getVisibleProjects() {
        return projectCache.getVisibleProjects()
                .values()
                .stream()
                .sorted(comparing(Project::getName))
                .collect(toList());
    }

    public Optional<CimProject> getProjectMetadata(String projectKey) {
        if (!isProjectVisible(projectKey))
            return Optional.empty();

        GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder()
                .withExpandedIssueTypesFields()
                .withProjectKeys(projectKey)
                .build();

        Iterable<CimProject> projects = jiraEndpointAsUser.executeRequest(c -> c.getIssueClient().getCreateIssueMetadata(options));

        return projects.iterator().hasNext() ?
                Optional.of(projects.iterator().next()) :
                Optional.empty();
    }

    public boolean isProjectVisible(String projectKey) {
        return projectCache.getVisibleProjects().containsKey(projectKey);
    }

    public Version getVersion(String versionId) {
        if(versionId == null)
            return null;

        return projectCache.getAllProjects().stream()
                .filter(project -> project.versions != null)
                .flatMap(project -> project.versions.stream())
                .filter(version -> versionId.equals(version.id))
                .findFirst()
                .orElse(null);
    }

    public Map<String, List<JiraProject.Role>> getUserRoles(String username) {
        Map<String, List<JiraProject.Role>> allProjectRoles = projectCache.getAllProjectsRoles();
        return allProjectRoles.entrySet()
                .stream()
                .peek(entry ->
                        entry.setValue(
                                entry.getValue()
                                        .stream()
                                        .filter(p -> p.actors.stream().anyMatch(actor -> username.equals(actor.name)))
                                        .collect(toList())
                        )
                )
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
