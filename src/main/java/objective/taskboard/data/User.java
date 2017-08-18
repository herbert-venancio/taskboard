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
import java.net.URI;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String user;
    private final String name;
    private final String mail;
    private final URI avatar;

    public static User from(String user, String name, String mail, URI avatar) throws Exception {
        return new User(user, name, mail, avatar);
    }

    public String getUser() {
        return this.user;
    }

    public String getName() {
        return this.name;
    }

    public String getMail() {
        return this.mail;
    }

    public URI getAvatar() {
        return this.avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user1 = (User) o;

        if (user != null ? !user.equals(user1.user) : user1.user != null) return false;
        if (name != null ? !name.equals(user1.name) : user1.name != null) return false;
        if (mail != null ? !mail.equals(user1.mail) : user1.mail != null) return false;
        return avatar != null ? avatar.equals(user1.avatar) : user1.avatar == null;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (mail != null ? mail.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "user='" + user + '\'' +
                ", name='" + name + '\'' +
                ", mail='" + mail + '\'' +
                ", avatar=" + avatar +
                '}';
    }

    @java.beans.ConstructorProperties({"user", "name", "mail", "avatar"})
    private User(final String user, final String name, final String mail, final URI avatar) {
        this.user = user;
        this.name = name;
        this.mail = mail;
        this.avatar = avatar;
    }
}
