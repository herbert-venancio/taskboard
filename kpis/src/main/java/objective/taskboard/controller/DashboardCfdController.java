package objective.taskboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.auth.authorizer.permission.ProjectDashboardTacticalPermission;
import objective.taskboard.followup.CumulativeFlowDiagramDataProvider;
import objective.taskboard.followup.CumulativeFlowDiagramDataSet;

@RestController
public class DashboardCfdController {

    @Autowired
    private CumulativeFlowDiagramDataProvider cumulativeFlowDiagramDataProvider;

    @Autowired
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;

    @Autowired
    private ProjectDashboardTacticalPermission dashboardTacticalPermission;

    @RequestMapping(value = "/api/projects/{project}/followup/cfd", produces = MediaType.APPLICATION_JSON_VALUE)
    public CumulativeFlowDiagramDataSet data(@PathVariable("project") String project, @RequestParam("level") String level) {
        if (!dashboardTacticalPermission.isAuthorizedFor(project) &&
                !projectDashboardOperationalPermission.isAuthorizedFor(project))
            throw new ResourceNotFoundException();

        return cumulativeFlowDiagramDataProvider.getCumulativeFlowDiagramDataSet(project, level);
    }
}
