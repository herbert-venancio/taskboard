package objective.taskboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.data.TaskboardIssue;

public interface TaskboardIssueRepository extends JpaRepository<TaskboardIssue, Long> {
    public List<TaskboardIssue> findByIssueKeyIn(List<String> issueKeyList);
    public TaskboardIssue findByIssueKey(String issueKey);
}
