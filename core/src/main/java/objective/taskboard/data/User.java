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

import java.io.Serializable;
import java.util.Objects;

import objective.taskboard.domain.converter.IssueCoAssignee;
import objective.taskboard.jira.client.JiraUserDto;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    public String name;
    public String user;
    public String mail;
    private boolean assigned = true;
    
    public User(){
        this(null,null,null);
    }

    public User(final String name) {
        this(null, name, null);
    }

    public User(final String user, final String name, final String mail) {
        this.user = user;
        this.name = name;
        this.mail = mail;
        if (name == null) {
            assigned = false;
            this.name = null;
        }
    }

    public boolean isAssigned() {
        return assigned;
    }

    public static User from(JiraUserDto assignee) {
        if (assignee == null)
            return new User();
        return new User(assignee.getDisplayName(), assignee.getName(), assignee.getEmailAddress());
    }

    public static User from(IssueCoAssignee x) {
        return new User(x.getName(), x.getName(), null);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        if (name == null)
            return 0;
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User))
            return false;
        return Objects.equals(((User)obj).name, name);
    }
}
