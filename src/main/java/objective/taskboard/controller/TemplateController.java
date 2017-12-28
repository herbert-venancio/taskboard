package objective.taskboard.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import objective.taskboard.followup.FollowUpFacade;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private FollowUpFacade followUpFacade;

    @RequestMapping
    public List<TemplateData> get() {
        return followUpFacade.getTemplatesForCurrentUser();
    }

    @RequestMapping(method = RequestMethod.POST, consumes="multipart/form-data")
    public void upload(@RequestParam("file") MultipartFile file
            , @RequestParam("name") String templateName
            , @RequestParam("projects") String projects) throws IOException {

        followUpFacade.createTemplate(templateName, projects, file);
    }
    
    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes="multipart/form-data")
    public void update(@PathVariable("id") Long id
            , @RequestParam("file") Optional<MultipartFile > file
            , @RequestParam("name") String templateName
            , @RequestParam("projects") String projects) throws IOException {

        followUpFacade.updateTemplate(id, templateName, projects, file);
    }
    
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") Long id) throws IOException {
        followUpFacade.deleteTemplate(id);
    }
}
