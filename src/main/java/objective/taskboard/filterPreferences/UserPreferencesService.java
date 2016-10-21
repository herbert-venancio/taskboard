package objective.taskboard.filterPreferences;

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

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.domain.UserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPreferencesService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private String selectUserPreferences(String jiraUser) {
        final String sql = "SELECT preferences FROM user_preferences WHERE JIRA_USER = ?";
        final List<String> userPreferences = jdbcTemplate.queryForList(sql, new Object[]{jiraUser}, String.class);
        if (!userPreferences.isEmpty()) {
            return userPreferences.get(0);
        } else {
            return null;
        }
    }

    public synchronized String getUserPreferences() {
        final String jiraUser = CredentialsHolder.username();
        final String userPreferences = selectUserPreferences(jiraUser);

        if (userPreferences != null)
            return userPreferences;
        else
            return "{}";
    }

    public synchronized void insertOrUpdate(UserPreferences updatedUserPreferences) {
        final String jiraUser = updatedUserPreferences.getJiraUser();
        final String oldUserPreferences = selectUserPreferences(jiraUser);
        String sql = "";
        if (oldUserPreferences != null) {
            sql = "UPDATE user_preferences SET preferences = ? WHERE JIRA_USER = ?";
        } else {
            sql = "INSERT INTO user_preferences (PREFERENCES, JIRA_USER) VALUES(?, ?)";
        }
        jdbcTemplate.update(sql, updatedUserPreferences.getPreferences(), jiraUser);
    }
}