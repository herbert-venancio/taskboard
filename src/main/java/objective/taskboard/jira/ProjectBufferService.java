package objective.taskboard.jira;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import objective.taskboard.repository.ProjectFilterConfigurationRepository;

@Service
public class ProjectBufferService {

    @Autowired
    private ProjectFilterConfigurationRepository repository;

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @Cacheable(value = CacheConfiguration.ALL_PROJECTS)
    public List<JiraProject> getAllProjects() {
        Set<String> configuredProjects = repository.findAll().stream()
                .map(pfc -> pfc.getProjectKey())
                .collect(toSet());

        JiraProject.Service service = jiraEndpointAsMaster.request(JiraProject.Service.class);
        return service.all()
                .stream()
                .filter(project -> configuredProjects.contains(project.key))
                .map(project -> service.get(project.key))
                .collect(toList());
    }

    public Version getVersion(String versionId) {
        if(versionId == null)
            return null;

        return getAllProjects().stream()
                .filter(project -> project.versions != null)
                .flatMap(project -> project.versions.stream())
                .filter(version -> versionId.equals(version.id))
                .findFirst()
                .orElse(null);
    }
}
