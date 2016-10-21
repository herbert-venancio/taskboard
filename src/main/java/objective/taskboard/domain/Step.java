package objective.taskboard.domain;

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

import lombok.Data;
import lombok.EqualsAndHashCode;
import objective.taskboard.domain.converter.BooleanConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Data
@Entity
@Table(name = "step")
@EqualsAndHashCode(callSuper = true)
public class Step extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @OneToMany(mappedBy = "step", fetch = FetchType.EAGER)
    Collection<Filter> filters;
    
    private String name;
    
    private Integer ordem;
    
    private Double weight;
    
    @Transient
    private String color;
    
    @Convert(converter = BooleanConverter.class)
    private Boolean showHeader;
    
    @ManyToOne
    @JoinColumn(name = "stage")
    private Stage stage;

}
