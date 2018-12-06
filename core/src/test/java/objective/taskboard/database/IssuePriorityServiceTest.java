package objective.taskboard.database;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import objective.taskboard.data.Issue;
import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.repository.TaskboardIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class IssuePriorityServiceTest {
    @Mock
    private TaskboardIssueRepository issueOrderPriority;
    
    @Mock
    private IssueBufferService issueBuffer;
    
    @InjectMocks
    IssuePriorityService subject;
    
    public IssuePriorityServiceTest() {
        MockitoAnnotations.initMocks(this);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void reorderTest_onlyUpdatedIssuesShouldBeReturned() {
        when(issueOrderPriority.findByIssueKeyIn(Mockito.anyList()))
            .thenReturn(asList(
                    new TaskboardIssue("P-1", 100),
                    new TaskboardIssue("P-2", 150),
                    new TaskboardIssue("P-3", 200)
                    ));
        
        when(issueOrderPriority.save((TaskboardIssue)Mockito.anyObject())).thenAnswer(new Answer<TaskboardIssue>() {
            @Override
            public TaskboardIssue answer(InvocationOnMock invocation) throws Throwable {
                return (TaskboardIssue) invocation.getArguments()[0];
            }
        });
        
        Issue p4 = mock(Issue.class);
        when(p4.getCreated()).thenReturn(400L);
        when(issueBuffer.getIssueByKey("P-4")).thenReturn(p4);
        
        subject.reorder(new String[]{"P-3", "P-1", "P-2", "P-4"});
        
        ArgumentCaptor<TaskboardIssue> argument = ArgumentCaptor.forClass(TaskboardIssue.class);

        Mockito.verify(issueOrderPriority, Mockito.times(3)).save(argument.capture());
        LinkedList<TaskboardIssue> actual = new LinkedList<TaskboardIssue>(argument.getAllValues());
        TaskboardIssue e = actual.poll();
        assertEquals("P-3", e.getIssueKey());
        assertEquals(100L, e.getPriority());
        
        e = actual.poll();
        assertEquals("P-1", e.getIssueKey());
        assertEquals(150L, e.getPriority());
        
        e = actual.poll();
        assertEquals("P-2", e.getIssueKey());
        assertEquals(200L, e.getPriority());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenIssuesWithSamePriority_whenReorder_thenShouldSaveDifferentPriorities() {
        when(issueOrderPriority.findByIssueKeyIn(Mockito.anyList()))
            .thenReturn(asList(
                    new TaskboardIssue("P-1", 100),
                    new TaskboardIssue("P-3", 100)
                    ));

        when(issueOrderPriority.save((TaskboardIssue)Mockito.anyObject())).thenAnswer(new Answer<TaskboardIssue>() {
            @Override
            public TaskboardIssue answer(InvocationOnMock invocation) throws Throwable {
                return (TaskboardIssue) invocation.getArguments()[0];
            }
        });

        Issue p2 = mock(Issue.class);
        when(p2.getCreated()).thenReturn(100L);
        when(issueBuffer.getIssueByKey("P-2")).thenReturn(p2);

        Issue p4 = mock(Issue.class);
        when(p4.getCreated()).thenReturn(103L);
        when(issueBuffer.getIssueByKey("P-4")).thenReturn(p4);

        subject.reorder(new String[]{"P-4", "P-3", "P-2", "P-1"});

        ArgumentCaptor<TaskboardIssue> argument = ArgumentCaptor.forClass(TaskboardIssue.class);

        Mockito.verify(issueOrderPriority, Mockito.times(4)).save(argument.capture());
        LinkedList<TaskboardIssue> actual = new LinkedList<TaskboardIssue>(argument.getAllValues());
        TaskboardIssue e = actual.poll();
        assertEquals("P-4", e.getIssueKey());
        assertEquals(100L, e.getPriority());

        e = actual.poll();
        assertEquals("P-3", e.getIssueKey());
        assertEquals(101L, e.getPriority());

        e = actual.poll();
        assertEquals("P-2", e.getIssueKey());
        assertEquals(102L, e.getPriority());

        e = actual.poll();
        assertEquals("P-1", e.getIssueKey());
        assertEquals(103L, e.getPriority());
    }
}