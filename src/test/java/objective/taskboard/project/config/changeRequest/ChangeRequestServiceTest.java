package objective.taskboard.project.config.changeRequest;

import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.domain.ProjectFilterConfiguration;

public class ChangeRequestServiceTest {

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
    private List<ChangeRequest> changeRequests = new ArrayList<>();

    @Test
    public void listByProject_ifProjectExists_returnListWithAllProjectsChangeRequests() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .withChangeRequest("Baseline", "2018-12-28", 250, true)
                .withChangeRequest("Change Request 1", "2018-12-30", 30, false)
                .build();

        List<ChangeRequest> response = subject.listByProject(project);

        assertEquals(response.size(), changeRequests.size());
        for (int i = 0; i < response.size(); i++) {
            Assertions.assertThat(response.get(i))
                .isEqualToComparingFieldByFieldRecursively(changeRequests.get(i));
        }

    }

    @Test
    public void delete_ifChangeRequestNameIsBaseline_throwChangeRequestBaselineRemovalException() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .build();
        thrown.expect(ChangeRequestBaselineRemovalException.class);

        subject.delete(createChangeRequest(true));
    }

    @Test
    public void delete_ifChangeRequestIsValid_performsNormally() {
        ChangeRequestService subject = new ChangeRequestServiceBuilder()
                .build();

        thrown = ExpectedException.none();

        subject.delete(createChangeRequest(false));
    }

    private class ChangeRequestServiceBuilder {
        private ChangeRequestRepository changeRequestRepository = mock(ChangeRequestRepository.class);

        public ChangeRequestServiceBuilder withChangeRequest(String name, String date, int budgetIncrease, boolean isBaseline) {
            changeRequests.add(new ChangeRequest(project, name, parseStringToDate(date), budgetIncrease, isBaseline));
            return this;
        }

        public ChangeRequestService build() {
            when(changeRequestRepository.listByProject(project)).thenReturn(changeRequests);
            return new ChangeRequestService(changeRequestRepository);
        }
    }

    private ChangeRequest createChangeRequest(boolean isBaseline) {
        return new ChangeRequest(project, "Sample CR", new Date(), 200, isBaseline);
    }
}
