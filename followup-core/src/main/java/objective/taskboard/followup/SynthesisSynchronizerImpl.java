package objective.taskboard.followup;

import java.time.LocalDate;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.FollowupDailySynthesis;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.repository.FollowupDailySynthesisRepository;

@Component
public class SynthesisSynchronizerImpl implements SynthesisSynchronizer {
    private ProjectService projectService;
    
    private FollowupDailySynthesisRepository dailySynthesisRepository;
    
    @Autowired
    public SynthesisSynchronizerImpl(ProjectService projectService, FollowupDailySynthesisRepository dailySynthesisRepository) {
        this.projectService = projectService;
        this.dailySynthesisRepository = dailySynthesisRepository;
    }
    
    @Override
    public synchronized void syncSynthesis(Supplier<FollowUpSnapshot> lazySnapshotProvider, String projectKey, LocalDate date, boolean override) {
        ProjectFilterConfiguration project = projectService.getTaskboardProjectOrCry(projectKey);

        if (dailySynthesisRepository.exists(project.getId(), date)) {
            if (override) {
                dailySynthesisRepository.remove(project.getId(), date);
            } else {
                return;
            }
        }

        EffortHistoryRow effortHistoryRow = lazySnapshotProvider.get().getEffortHistoryRow();

        dailySynthesisRepository.add(new FollowupDailySynthesis(
                project.getId(), 
                date, 
                effortHistoryRow.sumEffortDone, 
                effortHistoryRow.sumEffortBacklog));
    }
}
