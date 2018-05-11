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

    private String fieldId;
    private Serializable value;
    private Long optionId;
    
    public CustomField(){}

    public CustomField(String fieldId, Serializable value) {
        this(fieldId, value, null);
    }

    public CustomField(String fieldId, Serializable value, Long optionId) {
        this.fieldId = fieldId;
        this.value = value;
        this.optionId = optionId;
    }
    
    public String getFieldId() {
        return fieldId;
    }

    public Object getValue() {
        return this.value;
    }

    public Long getOptionId() {
        return this.optionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldId == null) ? 0 : fieldId.hashCode());
        result = prime * result + ((optionId == null) ? 0 : optionId.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomField other = (CustomField) obj;
        if (fieldId == null) {
            if (other.fieldId != null)
                return false;
        } else if (!fieldId.equals(other.fieldId))
            return false;
        if (optionId == null) {
            if (other.optionId != null)
                return false;
        } else if (!optionId.equals(other.optionId))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    
}
