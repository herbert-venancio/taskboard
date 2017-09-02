package objective.taskboard.jira;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.domain.Filter;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Service
public class JiraIssueJqlBuilderService {
    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;
    
    @Autowired
    private FilterCachedRepository filterRepository;
    
    @Autowired
    private JiraProperties jiraProperties;


    public String projectsJql() {
        List<ProjectFilterConfiguration> projects = projectRepository.getProjects();
        String projectKeys = "'" + projects.stream()
                                           .map(ProjectFilterConfiguration::getProjectKey)
                                           .collect(Collectors.joining("','")) + "'";
        return String.format("project in (%s) ", projectKeys);
    }

    public String buildQueryForIssues() {
        List<Filter> filters = filterRepository.getCache();
        List<IssuesConfiguration> configs = filters.stream().map(x -> IssuesConfiguration.fromFilter(x)).collect(Collectors.toList());
        
        String projectsJql = projectsJql();
        String issueTypeAndStatusJql = issueTypeAndStatusAndLimitInDays(configs);
        if (issueTypeAndStatusJql.isEmpty()) 
            return String.format("(%s)", projectsJql);
        
        issueTypeAndStatusJql += " OR (status in ("+StringUtils.join(jiraProperties.getStatusesDeferredIds(),",")+"))";
        return String.format("(%s) AND (%s)", projectsJql, issueTypeAndStatusJql);
    }

    private String issueTypeAndStatusAndLimitInDays(List<IssuesConfiguration> configs) {
        List<IssuesConfiguration> configsWithRangeDate = configs.stream().filter(c -> !(c.getLimitInDays() == null)).collect(Collectors.toList());
        List<IssuesConfiguration> configsWithoutRangeDate = configs.stream().filter(c -> c.getLimitInDays() == null).collect(Collectors.toList());

        List<String> configStatusAndType = configsWithoutRangeDate.stream().map(x -> String.format("(status=%d AND type=%d)", x.getStatus(), x.getIssueType())).collect(Collectors.toList());
        List<String> configStatusAndTypeAndLimitInDays = configsWithRangeDate.stream().map(c -> String.format(" OR (type=%d AND status CHANGED TO %d AFTER %s)",
                c.getIssueType(), c.getStatus(), c.getLimitInDays())).collect(Collectors.toList());

        return String.join(" OR ", configStatusAndType) + String.join("", configStatusAndTypeAndLimitInDays);
    }    
}
