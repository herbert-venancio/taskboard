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
import java.util.List;

public class AspectItemFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String description;
    private String field;
    private List<AspectSubitemFilter> aspectsSubitemFilter;//NOSONAR

    public static AspectItemFilter from(String description, String field, List<AspectSubitemFilter> aspectsSubitemFilter) {
        return new AspectItemFilter(description, field, aspectsSubitemFilter);
    }

    public String getDescription() {
        return this.description;
    }

    public String getField() {
        return this.field;
    }

    public List<AspectSubitemFilter> getAspectsSubitemFilter() {
        return this.aspectsSubitemFilter;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public void setAspectsSubitemFilter(final List<AspectSubitemFilter> aspectsSubitemFilter) {
        this.aspectsSubitemFilter = aspectsSubitemFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AspectItemFilter that = (AspectItemFilter) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        return aspectsSubitemFilter != null ? aspectsSubitemFilter.equals(that.aspectsSubitemFilter) : that.aspectsSubitemFilter == null;
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (field != null ? field.hashCode() : 0);
        result = 31 * result + (aspectsSubitemFilter != null ? aspectsSubitemFilter.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AspectItemFilter{" +
                "description='" + description + '\'' +
                ", field='" + field + '\'' +
                ", aspectsSubitemFilter=" + aspectsSubitemFilter +
                '}';
    }

    public AspectItemFilter() {
    }

    @java.beans.ConstructorProperties({"description", "field", "aspectsSubitemFilter"})
    private AspectItemFilter(final String description, final String field, final List<AspectSubitemFilter> aspectsSubitemFilter) {
        this.description = description;
        this.field = field;
        this.aspectsSubitemFilter = aspectsSubitemFilter;
    }
}
