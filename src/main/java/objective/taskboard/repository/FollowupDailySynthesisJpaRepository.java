package objective.taskboard.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.FollowupDailySynthesis;

public interface FollowupDailySynthesisJpaRepository extends JpaRepository<FollowupDailySynthesis, Long>{
    Optional<FollowupDailySynthesis> findByFollowupDateAndProjectId(LocalDate date, Integer projectId);
    List<FollowupDailySynthesis> findByProjectId(Integer projectId);
}
