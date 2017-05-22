package objective.taskboard.controller;

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
