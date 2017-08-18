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
package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "issue_type_visibility")
public class IssueTypeConfiguration extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private long issueTypeId;

    private long parentIssueTypeId;

    public long getIssueTypeId() {
        return this.issueTypeId;
    }

    public long getParentIssueTypeId() {
        return this.parentIssueTypeId;
    }

    public void setIssueTypeId(final long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public void setParentIssueTypeId(final long parentIssueTypeId) {
        this.parentIssueTypeId = parentIssueTypeId;
    }

    @Override
    public String toString() {
        return "IssueTypeConfiguration{" +
                "issueTypeId=" + issueTypeId +
                ", parentIssueTypeId=" + parentIssueTypeId +
                '}';
    }

    public IssueTypeConfiguration() {
    }

    @java.beans.ConstructorProperties({"issueTypeId", "parentIssueTypeId"})
    public IssueTypeConfiguration(final long issueTypeId, final long parentIssueTypeId) {
        this.issueTypeId = issueTypeId;
        this.parentIssueTypeId = parentIssueTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IssueTypeConfiguration that = (IssueTypeConfiguration) o;

        if (issueTypeId != that.issueTypeId) return false;
        return parentIssueTypeId == that.parentIssueTypeId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (issueTypeId ^ (issueTypeId >>> 32));
        result = 31 * result + (int) (parentIssueTypeId ^ (parentIssueTypeId >>> 32));
        return result;
    }
}
