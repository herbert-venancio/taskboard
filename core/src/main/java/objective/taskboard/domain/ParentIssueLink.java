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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PARENT_LINK_CONFIG")
public class ParentIssueLink extends TaskboardEntity {

    @Column(name = "DESCRIPTION_ISSUE_LINK")
    public String descriptionIssueLink;

    public String getDescriptionIssueLink() {
        return this.descriptionIssueLink;
    }

    public void setDescriptionIssueLink(final String descriptionIssueLink) {
        this.descriptionIssueLink = descriptionIssueLink;
    }

    @Override
    public String toString() {
        return "ParentIssueLink{" +
                "descriptionIssueLink='" + descriptionIssueLink + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ParentIssueLink that = (ParentIssueLink) o;

        return descriptionIssueLink != null ? descriptionIssueLink.equals(that.descriptionIssueLink) : that.descriptionIssueLink == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (descriptionIssueLink != null ? descriptionIssueLink.hashCode() : 0);
        return result;
    }
}
