package objective.taskboard.database;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.domain.Lane;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.repository.LaneCachedRepository;

@Service
public class TaskboardDatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LaneCachedRepository laneRepository;
    
    @Autowired
    private JiraProperties jiraProperties;

    public List<LaneConfiguration> laneConfiguration() throws SQLException {
        return getConfigurations();
    }

    @Cacheable("configuration")
    private List<LaneConfiguration> getConfigurations() {
        List<Lane> lanes = laneRepository.getCache();
        return TaskboardConfigToLaneConfigurationTransformer.getInstance().transform(lanes);
    }

    public List<Issue> getSubtasks(String parent) {
        return jdbcTemplate.query("select CONCAT(CONCAT(projectIssue.pkey, '-'), issue.issuenum) issueKey, issue.summary"
                + " from " + jiraProperties.getSchema() +  ".issuelink il"
                + " join " + jiraProperties.getSchema() +  ".issuelinktype lt on lt.id = il.linktype and COALESCE(lt.pstyle, ' ') = 'jira_subtask'"
                + " join " + jiraProperties.getSchema() +  ".jiraissue source on il.source = source.id"
                + " join " + jiraProperties.getSchema() +  ".project project on project.id = source.project"
                + " join " + jiraProperties.getSchema() +  ".jiraissue issue on issue.id = il.destination"
                + " join " + jiraProperties.getSchema() +  ".project projectIssue on issue.project = projectIssue.id"
                + " and CONCAT(CONCAT(project.pkey, '-'), source.issuenum) = '" + parent + "'", new SubtaskRowMapper());
    }

    public List<Issue> getSubtasksDemanda(String parent) {
        return jdbcTemplate.query("select CONCAT(CONCAT(projectIssue.pkey, '-'), issue.issuenum) issueKey, issue.summary"
                + " from " + jiraProperties.getSchema() +  ".issuelink il"
                + " join " + jiraProperties.getSchema() +  ".issuelinktype lt on lt.id = il.linktype"
                + " join " + jiraProperties.getSchema() +  ".jiraissue source on il.source = source.id"
                + " join " + jiraProperties.getSchema() +  ".project project on project.id = source.project"
                + " join " + jiraProperties.getSchema() +  ".jiraissue issue on issue.id = il.destination"
                + " join " + jiraProperties.getSchema() +  ".project projectIssue on issue.project = projectIssue.id"
                + " where lt.linkname = '" + jiraProperties.getIssuelink().getDemand().getName() + "'"
                + " and CONCAT(CONCAT(project.pkey, '-'), source.issuenum) = '" + parent + "'", new SubtaskRowMapper());
    }

}
