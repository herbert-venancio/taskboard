package objective.taskboard.database;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.controller.IssuePriorityService;
import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.repository.TaskboardIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class IssuePriorityServiceTest {
    @Mock
    private TaskboardIssueRepository issueOrderPriority;
    
    @InjectMocks
    IssuePriorityService subject;
    
    public IssuePriorityServiceTest() {
        MockitoAnnotations.initMocks(this);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void reorderTest() {
        when(issueOrderPriority.findByIssueKeyIn(Mockito.anyList()))
            .thenReturn(asList(
                    new TaskboardIssue("P-1", 100), 
                    new TaskboardIssue("P-2", 150),
                    new TaskboardIssue("P-3", 200)
                    ));
        
        subject.reorder(new String[]{"P-3", "P-1", "P-2"});
        
        ArgumentCaptor<TaskboardIssue> argument = ArgumentCaptor.forClass(TaskboardIssue.class);

        Mockito.verify(issueOrderPriority, Mockito.times(3)).save(argument.capture());
        LinkedList<TaskboardIssue> actual = new LinkedList<TaskboardIssue>(argument.getAllValues());
        TaskboardIssue e = actual.poll();
        assertEquals("P-3", e.getProjectKey());
        assertEquals(100L, e.getPriority());
        
        e = actual.poll();
        assertEquals("P-1", e.getProjectKey());
        assertEquals(150L, e.getPriority());
        
        e = actual.poll();
        assertEquals("P-2", e.getProjectKey());
        assertEquals(200L, e.getPriority());
    }
}