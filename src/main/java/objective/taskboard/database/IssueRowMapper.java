package objective.taskboard.database;

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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import objective.taskboard.data.Issue;

import org.springframework.jdbc.core.RowMapper;

public class IssueRowMapper implements RowMapper<Issue> {

    @Override
    public Issue mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> customFields = new HashMap<>();

        for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String name = rs.getMetaData().getColumnName(i);
            if(name.startsWith("customfield_"))
                customFields.put(name, rs.getString(name));
        }

        return Issue.from(rs.getString("ISSUEKEY"), rs.getString("PROJECTKEY"), rs.getString("PROJECT"), rs.getLong("ISSUETYPE"), "", rs.getString("SUMMARY"),
                rs.getLong("STATUS"), rs.getString("SUBRESPONSAVEL1"), rs.getString("SUBRESPONSAVEL2"),
                rs.getString("PARENT"), rs.getInt("PARENTTYPE"), "", null, rs.getString("SUBRESPONSAVEIS"),
                rs.getString("ASSIGNEE"), null, rs.getLong("PRIORITY"), rs.getString("ESTIMATIVA"),
                rs.getDate("DUEDATE"), rs.getString("DESCRIPTION"), null, rs.getString("COMMENT"), customFields);
    }

}
