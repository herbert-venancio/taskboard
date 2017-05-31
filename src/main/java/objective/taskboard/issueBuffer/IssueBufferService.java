package objective.taskboard.issueBuffer;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.data.IssuePriorityOrderChanged;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.jira.JiraIssueService;
import objective.taskboard.jira.ProjectService;

@Service
public class IssueBufferService {

    @Autowired
    private JiraIssueToIssueConverter issueConverter;

    @Autowired
    private JiraIssueService jiraIssueService;

    @Autowired
    private ProjectService projectService;
    
    private Map<String, Issue> issueBuffer = new LinkedHashMap<>();

    public void updateIssueBuffer() {
        setIssues(issueConverter.convert(jiraIssueService.searchAll()));
    }

    public Issue updateIssueBuffer(final String key) {
        return updateIssueBuffer(IssueEvent.ISSUE_UPDATED, key);
    }
    
    public synchronized Issue updateIssueBuffer(IssueEvent event, final String key) {
        if (event == IssueEvent.ISSUE_DELETED)
            return issueBuffer.remove(key);

        List<com.atlassian.jira.rest.client.api.domain.Issue> jiraIssues = jiraIssueService.searchIssuesByKeys(asList(key));
        if (jiraIssues.isEmpty())
            return issueBuffer.remove(key);

        final Issue issue = issueConverter.convert(jiraIssues.get(0));
        putIssue(issue);

        updateSubtasks(issue.getIssueKey());

        return issue;
    }

    private void updateSubtasks(String key) {
        List<String> subtasksKeys = getSubtasksKeys(key);
        List<com.atlassian.jira.rest.client.api.domain.Issue> jiraSubtasks = jiraIssueService.searchIssuesByKeys(subtasksKeys);
        for (com.atlassian.jira.rest.client.api.domain.Issue jiraSubtask : jiraSubtasks) {
            Issue subtaskConverted = issueConverter.convert(jiraSubtask);
            putIssue(subtaskConverted);
        }
    }

    private List<String> getSubtasksKeys(String key) {
        List<String> subtasksKeys = issueBuffer.values().stream()
            .filter(i -> key.equals(i.getParent()))
            .map(i -> i.getIssueKey())
            .collect(toList());

        if (subtasksKeys.isEmpty())
            return newArrayList();

        List<String> allSubtasksKeys = newArrayList(subtasksKeys);
        for (String subtaskKey : subtasksKeys)
            allSubtasksKeys.addAll(getSubtasksKeys(subtaskKey));

        return allSubtasksKeys;
    }

    public synchronized List<Issue> getIssues() {
        return issueBuffer.values().stream()
                .filter(t -> projectService.isProjectVisible(t.getProjectKey()))
                .filter(t -> isParentVisible(t))
                .collect(toList());
    }

    private boolean isParentVisible(Issue issue) {
    	
    	boolean visible = false;
    	    	
    	if (issue.getParent() == null || "".equals(issue.getParent()))
    		return true;
    	
    	Issue findFirst = issueBuffer.values().stream()
		    .filter(t -> t.getIssueKey().equals(issue.getParent()))
		    .findFirst().orElse(null);
    	
    	if (findFirst != null)
    		visible = isParentVisible(findFirst);

		return visible;
    	
	}

    private synchronized void setIssues(List<Issue> issues) {
        issueBuffer.clear();
        for (Issue issue : issues)
            putIssue(issue);
    }

    private void putIssue(Issue issue) {
        issueBuffer.put(issue.getIssueKey(), issue);
    }

    @EventListener
    protected void onAfterSave(IssuePriorityOrderChanged event) {
        TaskboardIssue entity = event.getTarget();
        issueBuffer.get(entity.getProjectKey()).setPriorityOrder(entity.getPriority());
    }
}
