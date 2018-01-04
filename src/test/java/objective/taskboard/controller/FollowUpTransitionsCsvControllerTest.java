package objective.taskboard.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.any;

import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.followup.FollowUpHelper;
import objective.taskboard.followup.FollowupCluster;
import objective.taskboard.followup.FollowupDataProvider;
import objective.taskboard.issueBuffer.IssueBufferState;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpTransitionsCsvControllerTest {

    private FollowupDataProvider provider;

    @Mock
    private FollowUpFacade followUpFacade;

    @InjectMocks
    private FollowUpTransitionsCsvController subject;

    @Before
    public void setupProvider() {
        provider = new FollowupDataProvider() {
            @Override
            public FollowUpDataSnapshot getJiraData(FollowupCluster cluster, String[] includeProjects,
                    ZoneId timezone) {
                return new FollowUpDataSnapshot(null, FollowUpHelper.getFromFile());
            }
            
            @Override
            public IssueBufferState getFollowupState() {
                throw new RuntimeException("NOT IMPLEMENTED");
            }
        };
        willReturn(provider).given(followUpFacade).getProvider(any());
    }

    @Test
    public void downloadTransitions() {
        ResponseEntity<Object> response = subject.transitions("TASKB", ZoneId.systemDefault().getId());
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

}
