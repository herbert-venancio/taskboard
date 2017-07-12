package objective.taskboard.followup.data;

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
import objective.taskboard.domain.ProjectFilterConfiguration;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name="TEMPLATE", uniqueConstraints = @UniqueConstraint(name="unique_template_name", columnNames = {"name"}))
@NamedQueries({
        @NamedQuery(name = "Template.findTemplatesForProjectKeys", 
                    query = "SELECT distinct t FROM Template t JOIN t.projects p WHERE p.projectKey IN (?1)"),
        @NamedQuery(name = "Template.findTemplatesWithProjectKey", 
                    query = "SELECT distinct t FROM Template t JOIN t.projects p WHERE p.projectKey = ?1")
})
public class Template {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private String name;
    private String path;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TEMPLATE_PROJETO",
                joinColumns = { @JoinColumn(name = "TemplateId",
                        nullable = false, updatable = false) },
                inverseJoinColumns = { @JoinColumn(name = "ProjectId",
                        nullable = false, updatable = false) })
    private List<ProjectFilterConfiguration> projects;

}
