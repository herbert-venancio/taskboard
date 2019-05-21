package objective.taskboard.repository;

import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.data.TaskboardIssue;

@JaversSpringDataAuditable
public interface TaskboardIssueRepository extends JpaRepository<TaskboardIssue, Long> {
    List<TaskboardIssue> findByIssueKeyIn(List<String> issueKeyList);
}
