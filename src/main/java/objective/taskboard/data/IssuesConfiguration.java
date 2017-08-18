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
package objective.taskboard.data;

import objective.taskboard.domain.Filter;

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

    public long getIssueType() {
        return this.issueType;
    }

    public long getStatus() {
        return this.status;
    }

    public String getLimitInDays() {
        return this.limitInDays;
    }

    public void setIssueType(final long issueType) {
        this.issueType = issueType;
    }

    public void setStatus(final long status) {
        this.status = status;
    }

    public void setLimitInDays(final String limitInDays) {
        this.limitInDays = limitInDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssuesConfiguration that = (IssuesConfiguration) o;

        if (issueType != that.issueType) return false;
        if (status != that.status) return false;
        return limitInDays != null ? limitInDays.equals(that.limitInDays) : that.limitInDays == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (issueType ^ (issueType >>> 32));
        result = 31 * result + (int) (status ^ (status >>> 32));
        result = 31 * result + (limitInDays != null ? limitInDays.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IssuesConfiguration{" +
                "issueType=" + issueType +
                ", status=" + status +
                ", limitInDays='" + limitInDays + '\'' +
                '}';
    }

    public IssuesConfiguration() {
    }
}
