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

public class CustomField implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Serializable value;

    private Long optionId;

    public CustomField(String name, Serializable value, Long optionId) {
        this.name = name;
        this.value = value;
        this.optionId = optionId;
    }

    public CustomField(String name, Serializable value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    public Long getOptionId() {
        return this.optionId;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final Serializable value) {
        this.value = value;
    }

    public void setOptionId(final Long optionId) {
        this.optionId = optionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomField that = (CustomField) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return optionId != null ? optionId.equals(that.optionId) : that.optionId == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (optionId != null ? optionId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CustomField{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", optionId=" + optionId +
                '}';
    }
}
