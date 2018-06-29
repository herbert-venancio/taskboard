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

import java.net.URI;
import java.util.List;

import objective.taskboard.jira.data.Version;

public class FilterFieldValue {
    private String name;
    private String value;
    private URI iconUri;
    private boolean selected;
    private List<String> teams;
    private List<Version> releases;

    public FilterFieldValue(String name, String value, URI iconUri, boolean selected) {
        this(name, value, iconUri, selected, null, null);
    }

    public FilterFieldValue(final String name, final String value, final URI iconUri, final boolean selected, final List<String> teams, final List<Version> releases) {
        this.name = name;
        this.value = value;
        this.iconUri = iconUri;
        this.selected = selected;
        this.teams = teams;
        this.releases = releases;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public URI getIconUri() {
        return this.iconUri;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public List<String> getTeams() {
        return this.teams;
    }

    public List<Version> getReleases() {
        return this.releases;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setIconUri(final URI iconUri) {
        this.iconUri = iconUri;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public void setTeams(final List<String> teams) {
        this.teams = teams;
    }

    public void setReleases(final List<Version> releases) {
        this.releases = releases;
    }

}
