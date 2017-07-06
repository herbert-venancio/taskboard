package objective.taskboard.followup.data;

import lombok.Data;
import objective.taskboard.domain.ProjectFilterConfiguration;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name="TEMPLATE")
@NamedQuery(name = "Template.findTemplatesForProjectKeys"
        , query = "SELECT t FROM Template t JOIN t.projects p WHERE p.projectKey IN (?1)")
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
