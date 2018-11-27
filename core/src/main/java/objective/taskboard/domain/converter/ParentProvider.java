package objective.taskboard.domain.converter;

import java.util.Optional;

import objective.taskboard.data.Issue;

public interface ParentProvider {

    Optional<Issue> get(String parentKey);

}
