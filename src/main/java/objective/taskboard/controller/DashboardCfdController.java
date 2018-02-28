package objective.taskboard.controller;

import static objective.taskboard.repository.PermissionRepository.DASHBOARD_OPERATIONAL;
import static objective.taskboard.repository.PermissionRepository.DASHBOARD_TACTICAL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.Authorizer;
import objective.taskboard.followup.CumulativeFlowDiagramDataProvider;
import objective.taskboard.followup.CumulativeFlowDiagramDataSet;

@RestController
public class DashboardCfdController {

    @Autowired
    private CumulativeFlowDiagramDataProvider cumulativeFlowDiagramDataProvider;

    @Autowired
    private Authorizer authorizer;

    @RequestMapping(value = "/api/projects/{project}/followup/cfd", produces = MediaType.APPLICATION_JSON_VALUE)
    public CumulativeFlowDiagramDataSet data(@PathVariable("project") String project, @RequestParam("level") String level) {
        if (!authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, project) &&
                !authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, project))
            throw new ResourceNotFoundException();

        return cumulativeFlowDiagramDataProvider.getCumulativeFlowDiagramDataSet(project, level);
    }
}
