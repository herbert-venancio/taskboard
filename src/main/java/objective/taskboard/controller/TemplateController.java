package objective.taskboard.controller;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.repository.PermissionRepository.ADMINISTRATIVE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import objective.taskboard.auth.Authorizer;
import objective.taskboard.followup.FollowUpFacade;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TemplateController.class);

    @Autowired
    private FollowUpFacade followUpFacade;

    @Autowired
    private Authorizer authorizer;

    @RequestMapping
    public List<TemplateData> get() {
        List<String> allowedProjects = authorizer.getAllowedProjectsForPermissions(ADMINISTRATIVE);
        List<TemplateData> templates = followUpFacade.getTemplatesForCurrentUser().stream()
            .filter(template -> allowedProjects.containsAll(template.projects))
            .collect(toList());

        return templates;
    }

    @RequestMapping(method = RequestMethod.POST, consumes="multipart/form-data")
    public void upload(@RequestParam("file") MultipartFile file
            , @RequestParam("name") String templateName
            , @RequestParam("projects") String projects) throws IOException {

        List<String> projectKeys = Arrays.asList(projects.split(","));
        if (!isTemplateProjectsInAllowedProjects(projectKeys))
            throw new ResourceNotFoundException();

        followUpFacade.createTemplate(templateName, projects, file);
    }
    
    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes="multipart/form-data")
    public void update(@PathVariable("id") Long id
            , @RequestParam("file") Optional<MultipartFile > file
            , @RequestParam("name") String templateName
            , @RequestParam("projects") String projects) throws IOException {

        List<String> projectKeys = Arrays.asList(projects.split(","));
        if (!isAllowedTemplate(id) || !isTemplateProjectsInAllowedProjects(projectKeys))
            throw new ResourceNotFoundException();

        followUpFacade.updateTemplate(id, templateName, projects, file);
    }
    
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") Long id) throws IOException {
        if (!isAllowedTemplate(id))
            throw new ResourceNotFoundException();

        followUpFacade.deleteTemplate(id);
    }

    @RequestMapping("{id}")
    public ResponseEntity<Object> downloadSavedTemplate(@PathVariable("id") Long id) {
        if (!isAllowedTemplate(id))
            return new ResponseEntity<>("Template not found.", NOT_FOUND);

        try {
            Optional<TemplateData> template = followUpFacade.getTemplate(id);
            if (!template.isPresent())
                return new ResponseEntity<>("Template not found.", NOT_FOUND);

            String templateName = template.get().name;
            Resource resource = followUpFacade.getSavedTemplate(templateName);
            return ResponseEntity.ok()
                      .contentLength(resource.contentLength())
                      .header("Content-Disposition","attachment; filename=" + templateName + "-followup-template.xlsm")
                      .body(resource);
        } catch (Exception e) {
            log.warn("Error while serving Template", e);
            return new ResponseEntity<>(e.getMessage() == null ? e.toString() : e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isAllowedTemplate(Long id) {
        Optional<TemplateData> template = followUpFacade.getTemplate(id);
        return template.isPresent() && isTemplateProjectsInAllowedProjects(template.get().projects);
    }

    private boolean isTemplateProjectsInAllowedProjects(List<String> projectKeys) {
        return authorizer.getAllowedProjectsForPermissions(ADMINISTRATIVE).containsAll(projectKeys);
    }
}
