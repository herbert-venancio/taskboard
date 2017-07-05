package objective.taskboard.repository;

import objective.taskboard.followup.data.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long>{
}
