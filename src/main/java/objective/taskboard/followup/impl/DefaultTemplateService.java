package objective.taskboard.followup.impl;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.TemplateService;
import objective.taskboard.followup.data.Template;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultTemplateService implements TemplateService{

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Override
    public void saveTemplate(String templateName, String projects, String path) {
        Template template = new Template();
        template.setName(templateName);
        template.setPath(path);
        List<String> projectKeys = Arrays.asList(projects.split(","));
        List<ProjectFilterConfiguration> associatedProjects =
                projectRepository.getProjects()
                        .stream()
                        .filter(proj -> projectKeys.contains(proj.getProjectKey()))
                        .collect(Collectors.toList());
        template.setProjects(associatedProjects);

        templateRepository.save(template);
    }

    @Override
    public List<Template> findTemplatesForProjectKeys(List<String> projectKeys) {
        return templateRepository.findTemplatesForProjectKeys(projectKeys)
                .stream()
                .filter(template -> template.getProjects()
                        .stream()
                        .allMatch(project -> projectKeys.contains(project.getProjectKey())))
                .collect(Collectors.toList());
    }

    @Override
    public Template getTemplate(String templateName) {
        return templateRepository.findByName(templateName);
    }
}
