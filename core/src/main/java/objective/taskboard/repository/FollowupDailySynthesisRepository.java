package objective.taskboard.repository;

import java.time.LocalDate;
import java.util.List;

import objective.taskboard.domain.FollowupDailySynthesis;

public interface FollowupDailySynthesisRepository {

    boolean exists(Long projectId, LocalDate date);
    List<FollowupDailySynthesis> listAllBefore(Long projectId, LocalDate maxDateExclusive);
    void add(FollowupDailySynthesis followupDailySynthesis);
    void remove(Long projectId, LocalDate date);

}