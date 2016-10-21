package objective.taskboard.data;

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

import lombok.Data;
import lombok.NoArgsConstructor;
import objective.taskboard.domain.Filter;

@Data
@NoArgsConstructor
public class IssuesConfiguration {

    private long issueType;
    private long status;
    private String limitInDays;

    public IssuesConfiguration(long issueType, long status) {
        this.issueType = issueType;
        this.status = status;
    }

    public IssuesConfiguration(long issueType, long status, String limitInDays) {
        this.issueType = issueType;
        this.status = status;
        this.limitInDays = limitInDays;
    }

    public static IssuesConfiguration from(long issueType, long status) {
        return new IssuesConfiguration(issueType, status);
    }

    public static IssuesConfiguration from(long issueType, long status, String limitInDays) {
        return new IssuesConfiguration(issueType, status, limitInDays);
    }

    public static IssuesConfiguration fromFilter(Filter filter) {
        if (filter.getLimitInDays() == null) {
            return new IssuesConfiguration(filter.getIssueTypeId(), filter.getStatusId());
        } else {
            return new IssuesConfiguration(filter.getIssueTypeId(), filter.getStatusId(), filter.getLimitInDays());
        }
    }

    public boolean matches(Issue issue) {
        return issue.getType() == getIssueType() && issue.getStatus() == getStatus();
    }

}
