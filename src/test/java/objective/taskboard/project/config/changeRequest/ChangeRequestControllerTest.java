package objective.taskboard.project.config.changeRequest;

import static objective.taskboard.utils.DateTimeUtils.parseDateStringAsMilliseconds;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.AuthorizedProjectsService;
import objective.taskboard.project.config.changeRequest.ChangeRequestController.ChangeRequestDto;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

public class ChangeRequestControllerTest {

    @Test
    public void getAll_ifProjectExists_returnOkWithAllProjectsChangeRequests() {
        ChangeRequestController subject = new ChangeRequestControllerBuilder()
                .withAuthorizedProject("TASKB")
                .withChangeRequest("Baseline", "2018-12-28", 250, true)
                .withChangeRequest("Change Request 1", "2018-12-30", 30, false)
                .withChangeRequest("Change Request 2", "2019-02-30", 15, false)
                .build();

        AssertResponse.of(subject.getAll("TASKB"))
            .httpStatus(OK)
            .bodyClassWhenList(0, ChangeRequestDto.class)
            .bodyAsJson(
                    "["+
                        "{" +
                            "\"project\" : \"TASKB\","+    
                            "\"name\" : \"Baseline\"," +
                            "\"date\" : "+parseDateStringAsMilliseconds("2018-12-28")+"," +
                            "\"budgetIncrease\" : 250," +
                            "\"isBaseline\": true" +
                        "}," +
                        "{" +
                            "\"project\" : \"TASKB\","+    
                            "\"name\" : \"Change Request 1\"," +
                            "\"date\" : "+parseDateStringAsMilliseconds("2018-12-30")+"," +
                            "\"budgetIncrease\" : 30," +
                            "\"isBaseline\": false" +
                         "}," +
                         "{" +
                             "\"project\" : \"TASKB\","+    
                             "\"name\" : \"Change Request 2\"," +
                             "\"date\" : "+parseDateStringAsMilliseconds("2019-02-30")+"," +
                             "\"budgetIncrease\" : 15," +
                             "\"isBaseline\": false" +
                         "}" +
                    "]");
    }
    
    @Test
    public void getAll_ifUserNotAuthorized_returnNotFound() {
        ChangeRequestController subject = new ChangeRequestControllerBuilder()
                .withNonAuthorizedProject("TASKB")
                .build();
        
        AssertResponse.of(subject.getAll("TASKB"))
            .httpStatus(NOT_FOUND)
            .emptyBody();
        
    }

    private static class ChangeRequestControllerBuilder {
        private ChangeRequestService changeRequestService = mock(ChangeRequestService.class);
        private AuthorizedProjectsService authorizedProjectsService = mock(AuthorizedProjectsService.class);

        private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        List<ChangeRequest> changeRequests = new ArrayList<>();

        public ChangeRequestControllerBuilder withAuthorizedProject(String projectkey) {
            when(project.getProjectKey()).thenReturn(projectkey);
            when(authorizedProjectsService.getTaskboardProject(Mockito.eq(projectkey), Mockito.anyString())).thenReturn(Optional.of(project));
            return this;
        }

        public ChangeRequestControllerBuilder withNonAuthorizedProject(String projectkey) {
            when(authorizedProjectsService.getTaskboardProject(Mockito.eq(projectkey), Mockito.anyString())).thenReturn(Optional.empty());
            return this;
        }

        public ChangeRequestControllerBuilder withChangeRequest(String name, String date, int budgetIncrease, boolean isBaseline) {
            changeRequests.add(new ChangeRequest(project, name, parseStringToDate(date), budgetIncrease, isBaseline));
            return this;
        }

        public ChangeRequestController build() {
            when(changeRequestService.listByProject(Mockito.eq(project))).thenReturn(changeRequests);
            return new ChangeRequestController(changeRequestService, authorizedProjectsService);
        }
    }
}
