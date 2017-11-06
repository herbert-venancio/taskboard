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

import objective.taskboard.jira.data.Version;

import java.io.Serializable;
import java.util.List;

public class AspectSubitemFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Object value;
    private boolean selected;
    private boolean visible;
    private List<String> teams;
    private List<Version> versions;

    public static AspectSubitemFilter from(String name, Object value, boolean selected) {
        return new AspectSubitemFilter(name, value, selected, true, null, null);
    }

    public static AspectSubitemFilter from(String name, Object value, boolean selected, List<String> teams, List<Version> versions) {
        return new AspectSubitemFilter(name, value, selected, true, teams, versions);
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public List<String> getTeams() {
        return this.teams;
    }

    public List<Version> getVersions() {
        return this.versions;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public void setTeams(final List<String> teams) {
        this.teams = teams;
    }

    public void setVersions(final List<Version> versions) {
        this.versions = versions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AspectSubitemFilter that = (AspectSubitemFilter) o;

        if (selected != that.selected) return false;
        if (visible != that.visible) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (teams != null ? !teams.equals(that.teams) : that.teams != null) return false;
        return versions != null ? versions.equals(that.versions) : that.versions == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (selected ? 1 : 0);
        result = 31 * result + (visible ? 1 : 0);
        result = 31 * result + (teams != null ? teams.hashCode() : 0);
        result = 31 * result + (versions != null ? versions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AspectSubitemFilter{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", selected=" + selected +
                ", visible=" + visible +
                ", teams=" + teams +
                ", versions=" + versions +
                '}';
    }

    public AspectSubitemFilter() {
    }

    @java.beans.ConstructorProperties({"name", "value", "selected", "visible", "teams", "versions"})
    private AspectSubitemFilter(final String name, final Object value, final boolean selected, final boolean visible, final List<String> teams, final List<Version> versions) {
        this.name = name;
        this.value = value;
        this.selected = selected;
        this.visible = visible;
        this.teams = teams;
        this.versions = versions;
    }
}
