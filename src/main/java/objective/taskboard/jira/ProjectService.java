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

package objective.taskboard.jira;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.project.ProjectBaselineProvider;
import objective.taskboard.project.ProjectProfileItem;
import objective.taskboard.project.ProjectProfileItemRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Service
public class ProjectService {
	private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectFilterConfigurationCachedRepository projectRepository;
    private final ProjectProfileItemRepository projectProfileItemRepository;
    private final JiraProjectService jiraProjectService;
    private final Authorizer authorizer;
    private final ProjectBaselineProvider baselineProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ProjectService(
            ProjectFilterConfigurationCachedRepository projectRepository,
            ProjectProfileItemRepository projectProfileItemRepository, 
            JiraProjectService jiraProjectService,
            Authorizer authorizer, 
            ProjectBaselineProvider baselineProvider,
            ApplicationEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.projectProfileItemRepository = projectProfileItemRepository;
        this.jiraProjectService = jiraProjectService;
        this.authorizer = authorizer;
        this.baselineProvider = baselineProvider;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void loadAllProjects() {
		log.info("Loading jira projects data in the cache...");
		jiraProjectService.getAllProjects();
		log.info("Load jira projects complete");
    }

    public List<Project> getNonArchivedJiraProjectsForUser() {
        return getUserProjects().values().stream()
                .filter(p -> !isArchived(p.getKey()))
                .sorted(comparing(Project::getName))
                .collect(toList());
    }
    
    public Optional<Project> getJiraProjectAsUser(String projectKey) {
        return Optional.ofNullable(getUserProjects().get(projectKey));
    }

    public List<ProjectFilterConfiguration> getTaskboardProjects() {
        return projectRepository.getProjects().stream()
                .sorted((p1, p2) -> p1.getProjectKey().compareTo(p2.getProjectKey()))
                .collect(toList());
    }

    public List<ProjectFilterConfiguration> getTaskboardProjects(String... permissions) {
        List<String> allowedProjectsKeys = authorizer.getAllowedProjectsForPermissions(permissions);

        return getTaskboardProjects().stream()
                .filter(projectFilterConfiguration -> allowedProjectsKeys.contains(projectFilterConfiguration.getProjectKey()))
                .collect(toList());
    }

    public List<ProjectFilterConfiguration> getTaskboardProjects(Predicate<String> filterProjectByKey) {
        return getTaskboardProjects().stream()
                .filter(projectFilterConfiguration -> filterProjectByKey.test(projectFilterConfiguration.getProjectKey()))
                .collect(toList());
    }

    public List<ProjectFilterConfiguration> getTaskboardProjects(Predicate<String> filterProjectByKey, String... permissions) {
        return getTaskboardProjects(permissions).stream()
                .filter(projectFilterConfiguration -> filterProjectByKey.test(projectFilterConfiguration.getProjectKey()))
                .collect(toList());
    }

    public Optional<JiraCreateIssue.ProjectMetadata> getProjectMetadata(String projectKey) {
        if (!isNonArchivedAndUserHasAccess(projectKey))
            return Optional.empty();

        return jiraProjectService.getCreateIssueMetadata(projectKey);
    }

    public boolean isNonArchivedAndUserHasAccess(String projectKey) {
        return !isArchived(projectKey) && getUserProjects().containsKey(projectKey);
    }

    public boolean jiraProjectExistsAndUserHasAccess(String projectKey) {
        return getUserProjects().containsKey(projectKey);
    }

    private boolean isArchived(String projectKey) {
        Optional<ProjectFilterConfiguration> projectOpt = projectRepository.getProjectByKey(projectKey);
        if (!projectOpt.isPresent())
            throw new IllegalArgumentException(projectKey + " doesn't exist");

        return projectOpt.get().isArchived();
    }

    public boolean taskboardProjectExists(String projectKey) {
        return projectRepository.exists(projectKey);
    }

    public Optional<ProjectFilterConfiguration> getTaskboardProject(String projectKey) {
        return projectRepository.getProjectByKey(projectKey);
    }
    
    public ProjectFilterConfiguration getTaskboardProjectOrCry(String projectKey) {
        return projectRepository.getProjectByKeyOrCry(projectKey);
    }

    public Optional<ProjectFilterConfiguration> getTaskboardProject(String projectKey, String... permissions) {
        List<String> allowedProjectsKeys = authorizer.getAllowedProjectsForPermissions(permissions);
        return allowedProjectsKeys.contains(projectKey) ? getTaskboardProject(projectKey) : Optional.empty();
    }

    public void saveTaskboardProject(ProjectFilterConfiguration project) {
        projectRepository.save(project);
        eventPublisher.publishEvent(new ProjectUpdateEvent(this, project.getProjectKey()));
    }

    public Version getVersion(String versionId) {
        if(versionId == null)
            return null;

        return jiraProjectService.getAllProjects().stream()
                .filter(project -> project.versions != null)
                .flatMap(project -> project.versions.stream())
                .filter(version -> versionId.equals(version.id))
                .findFirst()
                .orElse(null);
    }

    public Optional<ProjectFilterConfiguration> getAllowedTaskboardProject(String projectKey) {
        Optional<ProjectFilterConfiguration> optProject = getTaskboardProject(projectKey);
        if (!optProject.isPresent() || !isNonArchivedAndUserHasAccess(projectKey))
            return Optional.empty();
        return optProject;
    }

    public List<LocalDate> getAvailableBaselineDates(String projectKey) {
        return baselineProvider.getAvailableDates(projectKey);
    }
    
    public List<ProjectProfileItem> getProjectProfile(String projectKey) {
        ProjectFilterConfiguration project = projectRepository.getProjectByKeyOrCry(projectKey);
        return projectProfileItemRepository.listByProject(project);
    }

    private Map<String, Project> getUserProjects() {
        List<String> userProjectKeys = jiraProjectService.getUserProjectKeys();
        Map<String, JiraProject> projectByKey = jiraProjectService.getAllProjects()
            .stream()
            .collect(toMap(JiraProject::getKey, p -> p));

        return projectRepository.getProjects().stream()
                .filter(pf -> userProjectKeys.contains(pf.getProjectKey()))
                .map(pf -> Project.from(projectByKey.get(pf.getProjectKey()), pf))
                .collect(toMap(Project::getKey, p -> p));
    }
}
