package objective.taskboard.controller;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.IOUtilities;

@RestController
@RequestMapping("/ws/avatar")
public class AvatarController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AvatarController.class);

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    @GetMapping
    public ResponseEntity<Object> getAvatar(@RequestParam("username") String username, HttpServletResponse response) {
        try {
            if (username.isEmpty())
                return ResponseEntity.notFound().build();

            JiraUser jiraUser = jiraService.getJiraUser(username);
            URL url = jiraUser.getAvatarUri24().toURL();
            String contentType = url.openConnection().getContentType();
            if (url.toString().contains(jiraProperties.getUrl()))
                return responseFromUrl(IOUtilities.asResource(jiraEndpointAsUser.readBytesFromURL(url)), contentType, response);
            
            return responseFromUrl(IOUtilities.asResource(IOUtils.toByteArray(url.openStream())), contentType, response);
        } catch (Exception e) {//NOSONAR
            log.warn("Error getting avatar for " + username + " " + e.getMessage());
            try {
                return responseFromUrl(
                        IOUtilities.asResource(AvatarController.class.getResource("/static/images/userwithoutavatar.svg")),
                        "image/svg+xml",
                        response);
            } catch (IOException e1) {//NOSONAR - this should never happen 
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    private ResponseEntity<Object>  responseFromUrl(Resource resource, String contentType, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "private, max-age=21600");
        response.setHeader("Pragma", "cache");
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .header("Content-Type", contentType)
                .body(resource);
    }
}
