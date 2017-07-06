package objective.taskboard.repository;

import objective.taskboard.followup.data.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long>{

    List<Template> findTemplatesForProjectKeys(List<String> projectKeys);

}
