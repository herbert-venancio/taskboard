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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.repository.TaskboardIssueRepository;

@Slf4j
@Service
public class IssuePriorityService  {
    @Autowired
    TaskboardIssueRepository repo;
    
    @Autowired
    IssueBufferService issueBufferService;
    
    Map<String, TaskboardIssue> cache = new LinkedHashMap<>();
    
    @PostConstruct
    public void loadCache() {
        log.debug("Loading IssuePriorityService");
        List<TaskboardIssue> all = repo.findAll();
        for (TaskboardIssue taskboardIssue : all) 
            cache.put(taskboardIssue.getProjectKey(), taskboardIssue);
        log.debug("DONE Loading IssuePriorityService");
    }
    
    public Long determinePriority(Issue e) {
        TaskboardIssue priorityOrder = cache.get(e.getKey());
        if (priorityOrder == null) 
            return e.getId();

        return priorityOrder.getPriority();
    }

    @Transactional
    public synchronized void reorder(String[] issueKeys) {
        List<TaskboardIssue> issues =new ArrayList<>(repo.findByIssueKeyIn(Arrays.asList(issueKeys)));
        Map<String, TaskboardIssue> issueByKey = issues.stream().collect(Collectors.toMap(TaskboardIssue::getProjectKey, Function.identity()));
        
        for (String ti : issueKeys) {
            if (issueByKey.get(ti) == null){
                TaskboardIssue taskboardIssue = new TaskboardIssue(ti, issueBufferService.getIssueByKey(ti).getId());
                issueByKey.put(ti, taskboardIssue);
                issues.add(taskboardIssue);
            }
        }
        
        LinkedList<Long> priorities = new LinkedList<Long>(issues.stream().map(e -> e.getPriority()).collect(Collectors.toList()));
        Collections.sort(priorities);
        
        for (String pkey : issueKeys) {
            issueByKey.get(pkey).setPriority(priorities.poll());
            save(issueByKey.get(pkey));
        }
    }
    
    private void save(TaskboardIssue taskboardIssue) {
        repo.save(taskboardIssue);
        cache.put(taskboardIssue.getProjectKey(), taskboardIssue);
    }
}
