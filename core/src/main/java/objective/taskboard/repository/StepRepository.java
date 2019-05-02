package objective.taskboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.Step;

public interface StepRepository extends JpaRepository<Step, Long> {
}
