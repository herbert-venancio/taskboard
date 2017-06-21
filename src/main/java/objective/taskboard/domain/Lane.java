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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import objective.taskboard.domain.converter.BooleanConverter;

@Data
@Entity
@Table(name = "lane")
@EqualsAndHashCode(callSuper = true)
public class Lane extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 3755116141467717335L;

    @Convert(converter = BooleanConverter.class)
    public boolean showLaneTeam;

    private String name;

    private Integer ordem;

    private double weight;

    @Convert(converter = BooleanConverter.class)
    private boolean showHeader;
    
    @Column(name = "SHOW_PARENT_ICON_SINT")
    @Convert(converter = BooleanConverter.class)
    private boolean showParentIconInSynthetic;

    @OneToMany(mappedBy = "lane", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Stage> stages;

    @OneToMany(mappedBy = "lane", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private Collection<Rule> rules;


}
