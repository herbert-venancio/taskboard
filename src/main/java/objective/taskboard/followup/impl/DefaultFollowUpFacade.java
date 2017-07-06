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

import objective.taskboard.controller.TemplateData;
import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.*;
import objective.taskboard.followup.data.Template;
import objective.taskboard.issueBuffer.IssueBufferState;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultFollowUpFacade implements FollowUpFacade {

    @Autowired
    private FollowUpTemplateStorage followUpTemplateStorage;

    @Autowired
    private FollowupDataProvider provider;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private Converter<Template, TemplateData> templateConverter;

    @Override
    public FollowUpGenerator getGenerator() {
        return new FollowUpGenerator(provider, followUpTemplateStorage.getDefaultTemplate());
    }

    @Override
    public FollowUpGenerator getGenerator(String templateName) {
        return new FollowUpGenerator(provider, followUpTemplateStorage.getTemplate(templateName));
    }

    @Override
    public IssueBufferState getFollowupState() {
        return provider.getFollowupState();
    }

    @Override
    public List<TemplateData> getTemplatesForCurrentUser() {
        List<String> projectKeys = projectService.getVisibleProjects()
                .stream()
                .map(Project::getKey)
                .collect(Collectors.toList());

        List<Template> templates = templateRepository.findTemplatesForProjectKeys(projectKeys);

        return templates
                .stream()
                .map(t -> templateConverter.convert(t))
                .collect(Collectors.toList());
    }

    @Override
    public void createTemplate(String templateName, String projects, MultipartFile file) throws IOException {
        String path = followUpTemplateStorage.storeTemplate(file.getInputStream(), new FollowUpTemplateValidator());

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
        System.out.println(templateRepository.count());
    }


}
