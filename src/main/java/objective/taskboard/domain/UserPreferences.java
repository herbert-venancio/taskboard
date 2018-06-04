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

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang.Validate;

@Entity
@Table(name = "user_preferences")
@NamedQueries({
    @NamedQuery(
            name = "UserPreferences.findByJiraUser",
            query = "SELECT up FROM UserPreferences up WHERE up.jiraUser = :jiraUser")
})
public class UserPreferences extends TaskboardEntity {

    private String jiraUser;

    @Lob
    private String preferences;

    protected UserPreferences() {}

    public UserPreferences(String jiraUser, String preferences) {
        this.setJiraUser(jiraUser);
        this.setPreferences(preferences);
    }

    public String getJiraUser() {
        return this.jiraUser;
    }

    public String getPreferences() {
        return this.preferences;
    }

    public void setJiraUser(final String jiraUser) {
        Validate.notEmpty(jiraUser, "jiraUser required");
        this.jiraUser = jiraUser;
    }

    public void setPreferences(final String preferences) {
        Validate.notEmpty(jiraUser, "preferences required");
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "jiraUser='" + jiraUser + '\'' +
                ", preferences='" + preferences + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserPreferences that = (UserPreferences) o;

        if (jiraUser != null ? !jiraUser.equals(that.jiraUser) : that.jiraUser != null) return false;
        return preferences != null ? preferences.equals(that.preferences) : that.preferences == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (jiraUser != null ? jiraUser.hashCode() : 0);
        result = 31 * result + (preferences != null ? preferences.hashCode() : 0);
        return result;
    }
}