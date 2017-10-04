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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import objective.taskboard.data.Issue;

@Entity
@Table(name = "filtro")
public class Filter extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "step")
    private Step step;

    private long issueTypeId;

    private long statusId;

    private String limitInDays;

    public Step getStep() {
        return this.step;
    }

    public long getIssueTypeId() {
        return this.issueTypeId;
    }

    public long getStatusId() {
        return this.statusId;
    }

    public String getLimitInDays() {
        return this.limitInDays;
    }

    public void setStep(final Step step) {
        this.step = step;
    }

    public void setIssueTypeId(final long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public void setStatusId(final long statusId) {
        this.statusId = statusId;
    }

    public void setLimitInDays(final String limitInDays) {
        this.limitInDays = limitInDays;
    }
    
    public boolean isApplicable(Issue issue) {
        return getStatusId() == issue.getStatus() && getIssueTypeId() == issue.getType();
    }

    @Override
    public String toString() {
        return "Filter{" +
                "step=" + step +
                ", issueTypeId=" + issueTypeId +
                ", statusId=" + statusId +
                ", limitInDays='" + limitInDays + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Filter filter = (Filter) o;

        if (issueTypeId != filter.issueTypeId) return false;
        if (statusId != filter.statusId) return false;
        if (step != null ? !step.equals(filter.step) : filter.step != null) return false;
        return limitInDays != null ? limitInDays.equals(filter.limitInDays) : filter.limitInDays == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (step != null ? step.hashCode() : 0);
        result = 31 * result + (int) (issueTypeId ^ (issueTypeId >>> 32));
        result = 31 * result + (int) (statusId ^ (statusId >>> 32));
        result = 31 * result + (limitInDays != null ? limitInDays.hashCode() : 0);
        return result;
    }
}
