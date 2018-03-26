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
package objective.taskboard.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.repository.TaskboardIssueRepository;

@Service
public class IssuePriorityService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssuePriorityService.class);
    @Autowired
    TaskboardIssueRepository repo;
    
    @Autowired
    IssueBufferService issueBufferService;
    
    Map<String, TaskboardIssue> cache = new LinkedHashMap<>();
    
    @PostConstruct
    public void loadCache() {
        log.debug("Loading IssuePriorityService");
        cache.clear();
        List<TaskboardIssue> all = repo.findAll();
        for (TaskboardIssue taskboardIssue : all) 
            cache.put(taskboardIssue.getIssueKey(), taskboardIssue);
        log.debug("DONE Loading IssuePriorityService");
    }
    
    public long determinePriority(Issue issue) {
        TaskboardIssue priorityOrder = cache.get(issue.getIssueKey());
        if (priorityOrder == null)
            return issue.getCreated();

        return priorityOrder.getPriority();
    }

    public Date priorityUpdateDate(Issue issue) {
        TaskboardIssue priorityOrder = cache.get(issue.getIssueKey());
        if (priorityOrder == null)
            return new Date(issue.getCreated());
        return priorityOrder.getUpdated();
    }

    public synchronized List<TaskboardIssue> reorder(String[] issueKeys) {
        List<TaskboardIssue> issues = reorderAndSave(issueKeys);

        for (TaskboardIssue taskboardIssue : issues)
            cache.put(taskboardIssue.getIssueKey(), taskboardIssue);

        return issues;
    }
    
    @Transactional
    private List<TaskboardIssue> reorderAndSave(String[] issueKeys) {
        List<TaskboardIssue> updatedIssues = new ArrayList<>();

        Map<String, TaskboardIssue> issueByKey = repo.findByIssueKeyIn(Arrays.asList(issueKeys))
                .stream()
                .collect(Collectors.toMap(TaskboardIssue::getIssueKey, Function.identity()));

        long[] priorities = Arrays.stream(issueKeys)
                .map(issueKey -> issueByKey.computeIfAbsent(issueKey, key -> new TaskboardIssue(key, issueBufferService.getIssueByKey(key).getCreated())))
                .mapToLong(TaskboardIssue::getPriority)
                .sorted()
                .toArray();

        fixClashingPriorities(priorities);

        for(int i = 0; i < issueKeys.length; ++i) {
            TaskboardIssue taskboardIssue = issueByKey.get(issueKeys[i]);
            long newPriority = priorities[i];
            long previousPriority = taskboardIssue.getPriority();
            if (previousPriority == newPriority)
                continue;
            taskboardIssue.setPriority(newPriority);
            updatedIssues.add(repo.save(taskboardIssue));
        }
        return updatedIssues;
    }

    private void fixClashingPriorities(long[] priorities) {
        for (int i = 1; i < priorities.length; i++) {
            long previous = priorities[i-1];
            long current = priorities[i];
            if (current <= previous) {
                current = previous + 1;
                priorities[i] = current;
            }
        }
    }

    public synchronized void reset() {
        loadCache();
    }
}
