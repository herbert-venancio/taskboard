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
package objective.taskboard.followup;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import objective.taskboard.auth.Authorizer;
import objective.taskboard.controller.TemplateData;
import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.domain.Project;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.followup.data.Template;
import objective.taskboard.jira.FieldMetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor;
import objective.taskboard.utils.IOUtilities;

@Service
public class FollowUpFacade {

    private final static String SAMPLE_FOLLOWUP_TEMPLATE_PATH = "followup-generic/generic-followup-template.xlsm";

    @Autowired
    private FollowUpTemplateStorageInterface followUpTemplateStorage;

    @Autowired
    private FollowUpSnapshotService snapshotService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private Converter<Template, TemplateData> templateConverter;

    @Autowired
    private DataBaseDirectory dataBaseDirectory;
    
    @Autowired
    private Authorizer authorizer;

    @Autowired
    private FieldMetadataService fieldMetadataService;

    public Resource generateReport(String templateName, Optional<LocalDate> date, ZoneId timezone, String projectKey) 
            throws ClusterNotConfiguredException, ProjectDatesNotConfiguredException {

        FollowUpTemplate template = getTemplate(templateName);
        SimpleSpreadsheetEditor spreadsheetEditor = new SimpleSpreadsheetEditor(template);
        FollowUpSnapshot snapshot = snapshotService.get(date, timezone, projectKey);

        return new FollowUpReportGenerator(spreadsheetEditor, fieldMetadataService).generate(snapshot, timezone);
    }

    public List<TemplateData> getTemplates() {
        return templateService.getTemplates()
                .stream()
                .map(t -> templateConverter.convert(t))
                .sorted((t1, t2) -> t1.name.compareTo(t2.name))
                .collect(toList());
    }

    public List<TemplateData> getTemplatesForCurrentUser() {
        List<String> projectKeys = projectService.getNonArchivedJiraProjectsForUser()
                .stream()
                .map(Project::getKey)
                .collect(toList());

        return getTemplates()
                .stream()
                .filter(t -> authorizer.hasAnyRoleInProjects(t.roles, projectKeys))
                .collect(toList());
    }

    public Optional<TemplateData> getTemplate(Long id) {
        Template template = templateService.getTemplate(id);
        if (template == null)
            return Optional.empty();

        return Optional.of(templateConverter.convert(template));
    }

    public void createTemplate(String templateName, List<String> roles, MultipartFile file) throws IOException {
        if (templateService.getTemplate(templateName) != null)
            throw new RuntimeException("This template name is already in use");

        String path = followUpTemplateStorage
                .storeTemplate(file.getInputStream(), new FollowUpTemplateValidator());

        templateService.saveTemplate(templateName, roles, path);
    }

    public void deleteTemplate(Long id) throws IOException {
        Template template = templateService.getTemplate(id);
        followUpTemplateStorage.deleteFile(template.getPath());
        templateService.deleteTemplate(id);
    }

    public void updateTemplate(Long id, String templateName, List<String> roles,
                               Optional<MultipartFile> file) throws IOException {
        String path = null;
        String oldPath = null;
        if (file.isPresent()) {
            oldPath = templateService.getTemplate(id).getPath();
            path = followUpTemplateStorage
                    .storeTemplate(file.get().getInputStream(),
                            new FollowUpTemplateValidator());
        }

        try {
            templateService.updateTemplate(id, templateName, roles, path);
        } catch(Exception t) {
            if(path != null) {
                followUpTemplateStorage.deleteFile(path);
            }
            throw t;
        }
        if(oldPath != null) {
            followUpTemplateStorage.deleteFile(oldPath);
        }
    }

    public Resource getGenericTemplate() {
        return IOUtilities.asResource(dataBaseDirectory.path(SAMPLE_FOLLOWUP_TEMPLATE_PATH));
    }

    private FollowUpTemplate getTemplate(String templateName) {
        Template followUpConfiguration = templateService.getTemplate(templateName);
        return followUpTemplateStorage.getTemplate(followUpConfiguration.getPath());
    }

    public Resource getTemplateResource(String templateName) {
        return getTemplate(templateName).getPathFollowupTemplateXLSM();
    }

    public List<LocalDate> getHistoryGivenProject(String projectKey) {
        return snapshotService.getAvailableHistory(projectKey);
    }
}
