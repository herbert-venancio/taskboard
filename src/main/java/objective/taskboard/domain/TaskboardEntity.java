package objective.taskboard.domain;

import lombok.Data;

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

import lombok.Getter;

import javax.persistence.*;

//import static javax.persistence.GenerationType.SEQUENCE;

@Data
@MappedSuperclass
public abstract class TaskboardEntity {

    @Id
    @Getter
    @Column(name = "ID")
//    @SequenceGenerator(name = "taskboard_seq", sequenceName = "taskboard_seq", allocationSize = 1)
//    @GeneratedValue(strategy = SEQUENCE, generator = "taskboard_seq")
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

}
