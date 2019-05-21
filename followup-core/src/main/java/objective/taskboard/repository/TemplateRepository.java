package objective.taskboard.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.followup.data.Template;

@JaversSpringDataAuditable
public interface TemplateRepository extends JpaRepository<Template, Long>{

    Template findByName(String templateName);
    Template findById(Long id);

}
