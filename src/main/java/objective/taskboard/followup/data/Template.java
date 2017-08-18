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
package objective.taskboard.followup.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

import objective.taskboard.domain.ProjectFilterConfiguration;

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
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String path;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TEMPLATE_PROJECT",
                joinColumns = { @JoinColumn(name = "template_id", referencedColumnName = "id",
                        nullable = false, updatable = false) },
                inverseJoinColumns = { @JoinColumn(name = "project_key", referencedColumnName = "projectKey",
                        nullable = false, updatable = false) })
    private List<ProjectFilterConfiguration> projects;

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public List<ProjectFilterConfiguration> getProjects() {
        return this.projects;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setProjects(final List<ProjectFilterConfiguration> projects) {
        this.projects = projects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        if (id != null ? !id.equals(template.id) : template.id != null) return false;
        if (name != null ? !name.equals(template.name) : template.name != null) return false;
        if (path != null ? !path.equals(template.path) : template.path != null) return false;
        return projects != null ? projects.equals(template.projects) : template.projects == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (projects != null ? projects.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", projects=" + projects +
                '}';
    }
}
