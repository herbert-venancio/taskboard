package objective.taskboard.issueBuffer;

import static objective.taskboard.jira.JiraIssueService.ISSUES_BY_USER_CACHE_NAME;

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

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.data.Issue;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.issueBuffer.IssueChangedNotificationService.IssueChangedListener;
import objective.taskboard.jira.JiraIssueService;

@Slf4j
@Service
public class IssueBufferService implements IssueChangedListener {

    private final JiraIssueToIssueConverter issueConverter;
    private final JiraIssueService jiraIssueService;
    private final Map<String, Issue> issueBuffer = new LinkedHashMap<>();
    private final CacheManager cacheManager;
    
    public IssueBufferService(JiraIssueToIssueConverter issueConverter, JiraIssueService jiraIssueService, CacheManager cacheManager) {
        this.issueConverter = issueConverter;
        this.jiraIssueService = jiraIssueService;
        this.cacheManager = cacheManager;
    }
    
    @Override
    public void onIssueUpdate(String issueKey) {
        log.debug("Issue updated " + issueKey);
        clearCache();
    }
    
    @Override
    public void onIssueCreated(String issueKey) {
        log.debug("Issue created " + issueKey);
        clearCache();
    }
    
    @Override
    public void onIssueDeleted(String issueKey) {
        log.debug("Issue deleted " + issueKey);
        clearCache();
    }
    
    private void clearCache() {
        log.debug("Clearing issues cache");
        cacheManager.getCache(ISSUES_BY_USER_CACHE_NAME).clear();        
    }

    public List<Issue> getIssues() {
        String user = CredentialsHolder.username();
        log.debug("❱❱❱❱❱❱ getIssues[] ❱❱ {}", user);
        List<Issue> issues = issueConverter.convert(jiraIssueService.searchAll(user));
        setIssues(issues);
        return issues;
    }

    public Issue getIssue(String issueKey) {
        log.debug("❱❱❱❱❱❱ getIssue ❱❱ {}", CredentialsHolder.username());
        return issueBuffer.get(issueKey);
    }
    
    private void setIssues(List<Issue> issues) {
        issueBuffer.clear();
        for (Issue issue : issues)
            putIssue(issue);
    }

    private void putIssue(Issue issue) {
        issueBuffer.put(issue.getIssueKey(), issue);
    }

}
