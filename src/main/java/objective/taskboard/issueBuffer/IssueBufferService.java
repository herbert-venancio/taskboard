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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.Authenticator;
import objective.taskboard.data.Issue;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.jira.JiraIssueService;
import objective.taskboard.jira.ProjectVisibilityService;

@Service
public class IssueBufferService {

    @Autowired
    private JiraIssueToIssueConverter issueConverter;

    @Autowired
    private JiraIssueService jiraIssueService;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private ProjectVisibilityService projectService;

    private Map<String, Issue> issueBuffer = new LinkedHashMap<>();

    @PostConstruct
    private void load() {
        updateIssueBuffer();
    }

    public void updateIssueBuffer() {
        authenticator.authenticateAsServer();
        setIssues(issueConverter.convert(jiraIssueService.searchAll()));
    }

    public Issue updateIssueBuffer(final String key) {
        return updateIssueBuffer(IssueEvent.ISSUE_UPDATED, key);
    }

    public synchronized Issue updateIssueBuffer(IssueEvent event, final String key) {
        if (event == IssueEvent.ISSUE_DELETED)
            return issueBuffer.remove(key);
        
        final com.atlassian.jira.rest.client.api.domain.Issue searchIssue = jiraIssueService.searchIssue(key);
        if (searchIssue == null)
            return issueBuffer.remove(key);

        final Issue issue = issueConverter.convert(searchIssue);
        putIssue(issue);
        return issue;
    }

    public synchronized List<Issue> getIssues(String user) {
        return issueBuffer.values().stream()
                .filter(t -> projectService.isProjectVisibleForUser(t.getProjectKey(), user))
                .collect(Collectors.toList());
    }

    private synchronized void setIssues(List<Issue> issues) {
        issueBuffer.clear();
        for (Issue issue : issues)
            putIssue(issue);
    }

    private void putIssue(Issue issue) {
        issueBuffer.put(issue.getIssueKey(), issue);
    }

}
