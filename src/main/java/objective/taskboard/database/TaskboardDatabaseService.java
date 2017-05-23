package objective.taskboard.database;

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
