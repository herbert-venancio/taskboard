package objective.taskboard.domain;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2016 Objective Solutions
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.jira.JiraProperties;

@Service
public class IssueColorService {

    @Autowired
    private JiraProperties jiraProperties;
    
    public String getColor(long issueTypeId, long parentTypeId) {
        int os = jiraProperties.getIssuetype().getOs().getId();
        int task = jiraProperties.getIssuetype().getTask().getId();
        int bug = jiraProperties.getIssuetype().getBug().getId();
        
        if (issueTypeId == os || parentTypeId == os)
            return "#add9fe";
        
        if (issueTypeId == task || parentTypeId == task)
            return "#fee5bc";
        
        if (issueTypeId == bug || parentTypeId == bug)
            return "#FF8B94";

        return "#ddf9d9";
    }
}
