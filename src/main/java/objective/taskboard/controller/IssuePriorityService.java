package objective.taskboard.controller;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;

import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.repository.TaskboardIssueRepository;

@Service
public class IssuePriorityService {
    @Autowired
    TaskboardIssueRepository repo;
    
    public Long determinePriority(Issue e) {
        TaskboardIssue priorityOrder = repo.findByIssueKey(e.getKey());
        if (priorityOrder == null) {
            repo.save(new TaskboardIssue(e.getKey(), e.getId()));
            return e.getId();
        }
            
        return priorityOrder.getPriority();
    }

    @Transactional
    public synchronized void reorder(String[] issueKeys) {
        List<TaskboardIssue> issues = repo.findByIssueKeyIn(Arrays.asList(issueKeys));
        Map<String, TaskboardIssue> issueByKey = issues.stream().collect(Collectors.toMap(TaskboardIssue::getProjectKey, Function.identity()));
        
        LinkedList<Long> priorities = new LinkedList<Long>(issues.stream().map(e -> e.getPriority()).collect(Collectors.toList()));
        Collections.sort(priorities);
        
        for (String pkey : issueKeys) {
            issueByKey.get(pkey).setPriority(priorities.poll());
            repo.save(issueByKey.get(pkey));
        }
        
    }
}
