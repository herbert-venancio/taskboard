package objective.taskboard.controller;

import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.followup.FollowUpHelper;
import objective.taskboard.followup.FollowupDataProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.willReturn;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZoneId;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpTransitionsCsvControllerTest {

    @Mock
    private FollowupDataProvider provider;

    @Mock
    private FollowUpFacade followUpFacade;

    @InjectMocks
    private FollowUpTransitionsCsvController subject;

    @Before
    public void setupProvider() {
        willReturn(provider).given(followUpFacade).getProvider(any());
        willReturn(FollowUpHelper.getFromFile()).given(provider).getJiraData(any(), any());
    }

    @Test
    public void downloadTransitions() {
        ResponseEntity<Object> response = subject.transitions("TASKB", ZoneId.systemDefault().getId());
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void invalidZoneId() {
        ResponseEntity<Object> response = subject.transitions("TASKB", "invalid");
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
