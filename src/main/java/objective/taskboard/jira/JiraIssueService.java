package objective.taskboard.jira;

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
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.IssueTypeConfigurationRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JiraIssueService {

    @Autowired
    JiraService jiraService;

    @Autowired
    ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    IssueTypeConfigurationRepository issueTypeConfigRepository;
    
    @Autowired
    JiraIssueToIssueConverter issueConverter;
    
    @Autowired
    private FilterCachedRepository filterRepository;

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

    public String issueTypeAndStatusAndLimitInDays(List<IssuesConfiguration> configs) {
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
        return jiraService.searchIssues(jql);
    }

    public Issue searchIssue(final String key) {
        final List<Issue> searchIssues = searchIssues("key = " + key);
        if (searchIssues.size() != 1)
            return null;
        return searchIssues.get(0);
    }

    public List<Issue> searchAll() {
        return searchIssues(null);
//=======
//		stopWatch.stop();
//		
//		stopWatch.start("jiraService.searchIssues");
//		List<com.atlassian.jira.rest.client.api.domain.Issue> searchIssues = jiraService.searchIssues(createJql(configs));
//		stopWatch.stop();
//		
//		stopWatch.start("jiraIssue -> taskboard issue");
//		List<Issue> issues = issueConverter.convert(searchIssues);
//		stopWatch.stop();
//		
//		LOGGER.info(stopWatch.prettyPrint());
//		
//		return issues;
//>>>>>>> Stashed changes
    }
    
    private List<Issue> searchIssues(String additionalJqlCondition) {
        List<Filter> filters = filterRepository.getCache();
        List<IssuesConfiguration> configs = filters.stream().map(x -> IssuesConfiguration.fromFilter(x)).collect(Collectors.toList());

        String jql = createJql(configs);
        if (additionalJqlCondition != null)
            jql = "(" + additionalJqlCondition + ") AND " + jql;

        return jiraService.searchIssues(jql);
    }

}
