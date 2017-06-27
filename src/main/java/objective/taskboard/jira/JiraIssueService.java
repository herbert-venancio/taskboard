package objective.taskboard.jira;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

import com.atlassian.jira.rest.client.api.domain.Issue;

import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.domain.Filter;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Service
public class JiraIssueService {

    @Autowired
    private JiraSearchService jiraSearchService;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private FilterCachedRepository filterRepository;

    @Autowired
    private JiraProperties properties;

    public String createJql(List<IssuesConfiguration> configs) {
        String projectsJql = projectsJql();
        String issueTypeAndStatusJql = issueTypeAndStatusAndLimitInDays(configs);
        if (issueTypeAndStatusJql.isEmpty()) return String.format("(%s)", projectsJql);
        else return String.format("(%s) AND (%s)", projectsJql, issueTypeAndStatusJql);
    }

    private String projectsJql() {
        List<ProjectFilterConfiguration> projects = projectRepository.getProjects();
        String projectKeys = "'" + projects.stream()
        								   .map(ProjectFilterConfiguration::getProjectKey)
        								   .collect(Collectors.joining("','")) + "'";
        return String.format("project in (%s) ", projectKeys);
    }

    private String issueTypeAndStatusAndLimitInDays(List<IssuesConfiguration> configs) {
        List<IssuesConfiguration> configsWithRangeDate = configs.stream().filter(c -> !(c.getLimitInDays() == null)).collect(Collectors.toList());
        List<IssuesConfiguration> configsWithoutRangeDate = configs.stream().filter(c -> c.getLimitInDays() == null).collect(Collectors.toList());

        List<String> configStatusAndType = configsWithoutRangeDate.stream().map(x -> String.format("(status=%d AND type=%d)", x.getStatus(), x.getIssueType())).collect(Collectors.toList());
        List<String> configStatusAndTypeAndLimitInDays = configsWithRangeDate.stream().map(c -> String.format(" OR (type=%d AND status CHANGED TO %d AFTER %s)",
                c.getIssueType(), c.getStatus(), c.getLimitInDays())).collect(Collectors.toList());

        return String.join(" OR ", configStatusAndType) + String.join("", configStatusAndTypeAndLimitInDays);
    }

    public List<Issue> searchIssues(List<IssuesConfiguration> configs, String additionalJqlCondition) {
        String jql = createJql(configs);
        if (additionalJqlCondition != null)
            jql = "(" + additionalJqlCondition + ") AND " + jql; 
        return jiraSearchService.searchIssues(jql);
    }

    public List<Issue> searchIssuesByKeys(final List<String> keys) {
        if (keys.isEmpty())
            return Arrays.asList();
        return searchIssues("key IN (" + String.join(",", keys) + ")");
    }

    public List<Issue> searchIssueSubTasksAndDemandedByKey(String key) {
    	String jql = "parent = " + key;
    	String linkeTypes = properties.getIssuelink().getDemand().getName();
    	if (!StringUtils.isEmpty(linkeTypes)) {
    		for (String linkType : Arrays.asList(linkeTypes.split("\\s*,\\s*"))) {
    			jql += " OR issuefunction in linkedIssuesOf('key = " + key + "', '" + linkType + "')";
    		}
    	}
        return searchIssues(jql);
    }

    public List<Issue> searchAll() {
        return searchIssues(null);
    }
    
    public List<Issue> searchAllProjectIssues() {
        String projectsJql = projectsJql();
        return jiraSearchService.searchIssues(projectsJql);
    }

    private List<Issue> searchIssues(String additionalJqlCondition) {
        List<Filter> filters = filterRepository.getCache();
        List<IssuesConfiguration> configs = filters.stream().map(x -> IssuesConfiguration.fromFilter(x)).collect(Collectors.toList());

        String jql = createJql(configs);
        if (additionalJqlCondition != null)
            jql = "(" + additionalJqlCondition + ") AND " + jql;

        return jiraSearchService.searchIssues(jql);
    }

}
