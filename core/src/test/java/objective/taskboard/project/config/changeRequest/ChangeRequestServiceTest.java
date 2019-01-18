package objective.taskboard.project.config.changeRequest;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import objective.taskboard.auth.authorizer.permission.ProjectAdministrationPermission;
import objective.taskboard.domain.ProjectFilterConfiguration;

public class ChangeRequestServiceTest {

	@Rule
    public ExpectedException thrown= ExpectedException.none();

    private ChangeRequestMockRepository changeRequestRepository = mock(ChangeRequestMockRepository.class); 
    private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);

    @Test
    public void listByProject_ifProjectExists_returnListWithAllProjectsChangeRequests() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withAuthorizedProject()
                .withChangeRequest("First", "2018-12-28", 25, true)
                .withChangeRequest("Second", "2019-02-28", 25, false)
                .build();

        List<ChangeRequest> response = subject.listByProject(project);

        assertChangeRequests(response,
            "TASKB | First | 2018-12-28 | 25 | true",
            "TASKB | Second | 2019-02-28 | 25 | false");       
    }

    @Test
    public void add_ifChangeRequestListIsValid_changeRequestsAreAdded() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withAuthorizedProject()
                .build();

        subject.updateItems(project, asList(
                changeRequest("First", "2018-12-28", 25, true),
                changeRequest("Second", "2019-02-28", 25, false)));

        assertChangeRequestsFromRepo(
                "TASKB | First | 2018-12-28 | 25 | true",
                "TASKB | Second | 2019-02-28 | 25 | false");
    }

    @Test
    public void updateAndRemove_ifChangeRequestListIsValid_changeRequestsAreModified() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withAuthorizedProject()
                .withChangeRequest("First", "2018-12-28", 25, true)
                .withChangeRequest("Second", "2019-02-28", 25, false)
                .build();

        List<ChangeRequest> response = subject.listByProject(project);

        response.get(0).setName("New name for changeRequest");
        response.remove(1);

        subject.updateItems(project, response);

        assertChangeRequestsFromRepo(
                "TASKB | New name for changeRequest | 2018-12-28 | 25 | true");
    }

    @Test
    public void remove_ifChangeRequestIsBaseline_changeRequestBaselineRemovalExceptionIsThrown() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withAuthorizedProject()
                .withChangeRequest("First", "2018-12-28", 25, true)
                .withChangeRequest("Second", "2019-02-28", 25, false)
                .build();

        List<ChangeRequest> response = subject.listByProject(project);
        response.remove(0);

        thrown.expect(ChangeRequestBaselineRemovalException.class);
        subject.updateItems(project, response);
    }

    @Test
    public void listByProject_ifUserNotAuthorized_throwsAccessDeniedException() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withNonAuthorizedProject()
                .build();

        thrown.expect(AccessDeniedException.class);
        subject.listByProject(project);       
    }

    @Test
    public void update_ifUserNotAuthorized_throwsAccessDeniedException() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withNonAuthorizedProject()
                .build();

        thrown.expect(AccessDeniedException.class);
        subject.updateItems(project, anyChangeRequestList() );       
    }

    @Test
    public void updateBaselineChangeRequest_ifUserNotAuthorized_throwsAccessDeniedException(){
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withNonAuthorizedProject()
                .build();

        thrown.expect(AccessDeniedException.class);
        subject.updateBaselineChangeRequest(project, LocalDate.of(2019, 12, 10));
    }

    @Test
    public void updateBaselineChangeRequest_ifUserAuthorizedAndNonExistantBaseline_baselineIsCreated(){
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withAuthorizedProject()
                .build();

        subject.updateBaselineChangeRequest(project, LocalDate.of(2019, 12, 10));
        assertChangeRequestsFromRepo("TASKB | Baseline | 2019-12-10 | 0 | true");
    }

    @Test
    public void updateBaselineChangeRequest_ifUserAuthorizedAndExistantBaseline_baselineIsUpdated(){
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withAuthorizedProject()
                .withChangeRequest("Baseline", "2018-12-25", 540, true)
                .withProjectNewStartDate("2019-05-26")
                .build();
        
        subject.updateBaselineChangeRequest(project, LocalDate.of(2019, 05, 30));
        assertChangeRequestsFromRepo("TASKB | Baseline | 2019-05-30 | 540 | true");
    }

    private List<ChangeRequest> anyChangeRequestList(){
        ChangeRequest anyCr = new ChangeRequest();
        anyCr.setId(1L);
        anyCr.setName("Change Request");
        anyCr.setRequestDate(LocalDate.now());

        return Arrays.asList(anyCr);
    }

    private void assertChangeRequests(List<ChangeRequest> actualList, String... expectedChangeRequests) {
        String actual = actualList.stream()
                .map(i -> changeRequestToString(i))
                .collect(joining("\n"));

        String expected = Stream.of(expectedChangeRequests)
                .map(i -> i.replaceAll("\\s+", " "))
                .collect(joining("\n"));

        assertEquals(expected, actual);
    }
    
    private void assertChangeRequestsFromRepo(String... expectedChangeRequests) {    
        when(changeRequestRepository.findBaselineIsTrueByProject(any(ProjectFilterConfiguration.class))).thenCallRealMethod();
        assertChangeRequests(changeRequestRepository.findByProjectOrderByRequestDateDesc(project), expectedChangeRequests);
    }

    private ChangeRequest changeRequest(String name, String date, int budgetIncrease, boolean isBaseline) {
        return new ChangeRequest(project, name, LocalDate.parse(date), budgetIncrease, isBaseline);
    }

    private String changeRequestToString(ChangeRequest i) {
        return String.format("%s | %s | %s | %s | %s", 
                i.getProject().getProjectKey(),
                i.getName(),
                i.getRequestDate(),
                i.getBudgetIncrease(), 
                i.isBaseline());
    }

    private class ChangeRequestServiceBuilder {
        private ProjectAdministrationPermission projectAdministrationPermission = mock(ProjectAdministrationPermission.class);
        private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        int id = 0;

        public ChangeRequestServiceBuilder(){
            when(changeRequestRepository.save(any(ChangeRequest.class))).thenCallRealMethod();
            when(changeRequestRepository.findBaselineIsTrueByProject(any(ProjectFilterConfiguration.class))).thenCallRealMethod();
            when(changeRequestRepository.findByProjectOrderByRequestDateDesc(any(ProjectFilterConfiguration.class))).thenCallRealMethod();
            doCallRealMethod().when(changeRequestRepository).delete(any(ChangeRequest.class));         
        
        }

        public ChangeRequestServiceBuilder withAuthorizedProject() {
            when(project.getProjectKey()).thenReturn("TASKB");
            when(project.getStartDate()).thenReturn(Optional.empty());
            when(projectAdministrationPermission.isAuthorizedFor(Mockito.eq("TASKB"))).thenReturn(true);
            return this;
        }

        public ChangeRequestServiceBuilder withNonAuthorizedProject() {
            when(projectAdministrationPermission.isAuthorizedFor(Mockito.eq("TASKB"))).thenReturn(false);
            return this;
        }

        public ChangeRequestServiceBuilder withProjectNewStartDate(String newDate){
            when(project.getStartDate()).thenReturn(Optional.of(LocalDate.parse(newDate)));
            return this;
        }

        public ChangeRequestServiceBuilder withChangeRequest(String name, String date, int budgetIncrease, boolean isBaseline) {
            ChangeRequest cr = new ChangeRequest(project, name, LocalDate.parse(date), budgetIncrease, isBaseline);
            cr.setId((long) (id+1));
            changeRequestRepository.save(cr);
            id++;
            return this;
        }

        public ChangeRequestService build(){
            return new ChangeRequestService(changeRequestRepository, projectAdministrationPermission, eventPublisher);
        }
    }
}
