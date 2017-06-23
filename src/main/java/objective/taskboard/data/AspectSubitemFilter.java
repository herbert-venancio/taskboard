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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AspectSubitemFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Object value;
    private boolean selected;
    private boolean visible;
    private List<String> teams;
    private List<String> versions;

    public static AspectSubitemFilter from(String name, Object value, boolean selected) {
        return new AspectSubitemFilter(name, value, selected, true, null, null);
    }

    public static AspectSubitemFilter from(String name, Object value, boolean selected, List<String> teams, List<String> versions) {
        return new AspectSubitemFilter(name, value, selected, true, teams, versions);
    }

}
