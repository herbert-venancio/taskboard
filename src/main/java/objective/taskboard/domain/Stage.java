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
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "stage")
@EqualsAndHashCode(callSuper = true)
public class Stage extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Integer ordem;

    private Double weight;

    @Convert(converter = BooleanConverter.class)
    private boolean showHeader;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid hexdacimal color!")
    private String color;

    @ManyToOne
    @JoinColumn(name = "lane")
    private Lane lane;

    @OneToMany(mappedBy = "stage", fetch = FetchType.EAGER)
    private List<Step> steps;

}
