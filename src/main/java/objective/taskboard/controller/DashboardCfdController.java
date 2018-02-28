package objective.taskboard.controller;

import objective.taskboard.followup.CumulativeFlowDiagramDataProvider;
import objective.taskboard.followup.CumulativeFlowDiagramDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardCfdController {

    @Autowired
    private CumulativeFlowDiagramDataProvider cumulativeFlowDiagramDataProvider;

    @RequestMapping(value = "/api/projects/{project}/followup/cfd", produces = MediaType.APPLICATION_JSON_VALUE)
    public CumulativeFlowDiagramDataSet data(@PathVariable("project") String project, @RequestParam("level") String level) {
        return cumulativeFlowDiagramDataProvider.getCumulativeFlowDiagramDataSet(project, level);
    }
}
