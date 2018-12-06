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
package objective.taskboard.domain;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import objective.taskboard.domain.converter.BooleanConverter;

@Entity
@Table(name = "step")
public class Step extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "step", fetch = FetchType.EAGER)
    private Collection<Filter> filters;

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

    public Collection<Filter> getFilters() {
        return this.filters;
    }

    public String getName() {
        return this.name;
    }

    public Integer getOrdem() {
        return this.ordem;
    }

    public Double getWeight() {
        return this.weight;
    }

    public String getColor() {
        return this.color;
    }

    public Boolean getShowHeader() {
        return this.showHeader;
    }

    public Stage getStage() {
        return this.stage;
    }

    public void setFilters(final Collection<Filter> filters) {
        this.filters = filters;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setOrdem(final Integer ordem) {
        this.ordem = ordem;
    }

    public void setWeight(final Double weight) {
        this.weight = weight;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public void setShowHeader(final Boolean showHeader) {
        this.showHeader = showHeader;
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
    }

}
