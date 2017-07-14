package objective.taskboard.followup.impl;

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
    public void saveTemplate(String templateName, List<String> projectKeys, String path) {
        List<ProjectFilterConfiguration> associatedProjects = findAssociatedProjects(projectKeys);
        
        Template template = new Template();
        template.setName(templateName);
        template.setPath(path);
        template.setProjects(associatedProjects);

        templateRepository.save(template);
    }
    
    public void updateTemplate(Long id, String templateName, String projects, String path) {
        List<String> projectKeys = Arrays.asList(projects.split(","));
        List<ProjectFilterConfiguration> associatedProjects = findAssociatedProjects(projectKeys);
        
        Template template = new Template();
        template.setId(id);
        template.setName(templateName);
        template.setProjects(associatedProjects);
        
        if (path != null)
            template.setPath(path);

        Template templateWithSameName = getTemplate(template.getName());
        if (templateWithSameName != null && templateWithSameName.getId() != template.getId())
            throw new RuntimeException("This template name is already in use");
        
        Template templateWithTheSameProjectKey = findATemplateOnlyMatchedWithThisProjectKey(projectKeys);
        if (templateWithTheSameProjectKey != null && templateWithTheSameProjectKey.getId() != template.getId())
            throw new RuntimeException("This match of projects is already used by other template");
        
        templateRepository.save(template);
    }
    
    public void deleteTemplate(Long id) {
        Template template = new Template();
        template.setId(id);
        templateRepository.delete(template);
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
    
    @Override
    public Template getTemplate(Long id) {
        return templateRepository.findById(id);
    }
    
    public Template findATemplateOnlyMatchedWithThisProjectKey(List<String> projectKeys) {
        if (projectKeys.size() != 1)
            return null;
        
        String projectKey = projectKeys.get(0);
        List<Template> templates = templateRepository.findTemplatesWithProjectKey(projectKey)
                .stream()
                .filter(template -> template.getProjects().size() == 1)
                .collect(Collectors.toList());
        
        if (templates.size() > 0) {
            return templates.get(0);
        } else {
            return null;
        }
    }
    
    private List<ProjectFilterConfiguration> findAssociatedProjects(List<String> projectKeys) {
        return projectRepository.getProjects()
            .stream()
            .filter(proj -> projectKeys.contains(proj.getProjectKey()))
            .collect(Collectors.toList());
    }
    
}
