package objective.taskboard.controller;

import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.followup.FollowUpDateRangeProvider;
import objective.taskboard.jira.FrontEndMessageException;
import objective.taskboard.jira.ProjectService;

@RestController
public class FollowUpDateRangeController {

    @Autowired
    private Authorizer authorizer;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FollowUpDateRangeProvider provider;

    @RequestMapping(value = "/api/projects/{projectKey}/followup/date-range", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDateRangeByProjectKey(@PathVariable("projectKey") String projectKey) {
        if (!authorizer.hasPermission(PROJECT_DASHBOARD_TACTICAL, projectKey) || !authorizer.hasPermission(PROJECT_DASHBOARD_OPERATIONAL, projectKey))
            return new ResponseEntity<>("Resource not found.", HttpStatus.NOT_FOUND);

        if (!projectService.taskboardProjectExists(projectKey))
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);

        try {
            return new ResponseEntity<>(provider.getDateRangeData(projectKey), OK);
        } catch(FrontEndMessageException e) {//NOSONAR
            return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

}
