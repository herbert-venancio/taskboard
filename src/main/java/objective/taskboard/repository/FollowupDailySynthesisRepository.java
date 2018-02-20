package objective.taskboard.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.FollowupDailySynthesis;

@Component
public class FollowupDailySynthesisRepository {

    private FollowupDailySynthesisJpaRepository repo;

    @Autowired
    public FollowupDailySynthesisRepository(FollowupDailySynthesisJpaRepository repo) {
        this.repo = repo;
    }

    public Optional<FollowupDailySynthesis> findByFollowupDateAndProjectId(LocalDate date, Integer projectId) {
        return repo.findByFollowupDateAndProjectId(date, projectId);
    }
    
    public List<FollowupDailySynthesis> findByFollowupProjectId(Integer projectId) {
        return repo.findByProjectId(projectId);
    }

    public void save(FollowupDailySynthesis followupDailySynthesis) {
        repo.save(followupDailySynthesis);
    }
}
