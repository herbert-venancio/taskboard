package objective.taskboard.followup;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ReleaseHistoryProvider.ProjectRelease;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.followup.cluster.FollowupClusterProvider;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.project.ProjectProfileItem;
import objective.taskboard.repository.FollowupDailySynthesisRepository;
import objective.taskboard.utils.Clock;

@Service
public class FollowUpSnapshotService {

    private final static Logger log = LoggerFactory.getLogger(FollowUpSnapshotService.class);

    private final Clock clock;
    private final FollowUpDataRepository historyRepository;
    private final ProjectService projectService;
    private final FollowupDailySynthesisRepository dailySynthesisRepository;
    private final FollowUpDataGenerator dataGenerator;
    private final FollowupClusterProvider clusterProvider;
    private final ReleaseHistoryProvider releaseHistoryProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final SynthesisSynchronizer synthesisSyncronizer;
    
    @Autowired
    public FollowUpSnapshotService(
            Clock clock,
            FollowUpDataRepository historyRepository,
            ProjectService projectService,
            FollowupDailySynthesisRepository dailySynthesisRepository, 
            FollowUpDataGenerator dataGenerator,
            FollowupClusterProvider clusterProvider,
            ReleaseHistoryProvider releaseHistoryProvider,
            ApplicationEventPublisher eventPublisher,
            SynthesisSynchronizer synthesisSyncronizer) {
        this.clock = clock;
        this.historyRepository = historyRepository;
        this.projectService = projectService;
        this.dailySynthesisRepository = dailySynthesisRepository;
        this.dataGenerator = dataGenerator;
        this.clusterProvider = clusterProvider;
        this.releaseHistoryProvider = releaseHistoryProvider;
        this.eventPublisher = eventPublisher;
        this.synthesisSyncronizer = synthesisSyncronizer;
    }

    public FollowUpSnapshot getFromCurrentState(ZoneId timezone, String projectKey) {
        return createSnapshot(timezone, LocalDate.now(), projectKey,
                (cluster) -> dataGenerator.generate(timezone, cluster, projectKey));
    }

    public FollowUpSnapshot getFromHistory(LocalDate date, ZoneId timezone, String projectKey) {
        return createSnapshot(timezone, date, projectKey, 
                (cluster) -> historyRepository.get(date, timezone, projectKey));
    }

    public List<LocalDate> getAvailableHistory(String projectKey) {
        return historyRepository.getHistoryByProject(projectKey);
    }
    
    public FollowUpSnapshot get(Optional<LocalDate> date, ZoneId timezone, String projectKey) {
        return date.isPresent()
                ? getFromHistory(date.get(), timezone, projectKey)
                : getFromCurrentState(timezone, projectKey);
    }

    private FollowUpSnapshot createSnapshot(ZoneId timezone, LocalDate date, String projectKey, FollowupDataSupplier dataSupplier) {
        ProjectFilterConfiguration project = projectService.getTaskboardProjectOrCry(projectKey);
        FollowUpTimeline timeline = FollowUpTimeline.build(date, project, historyRepository);
        FollowupCluster cluster = clusterProvider.getFor(project);
        FollowUpData data = dataSupplier.get(cluster);

        FollowUpSnapshotValuesProvider valuesProvider = new FollowUpSnapshotValuesProvider() {
            @Override
            public List<EffortHistoryRow> getEffortHistory() {
                return dailySynthesisRepository.listAllBefore(project.getId(), timeline.getReference()).stream()
                        .map(EffortHistoryRow::from)
                        .collect(toList());
            }

            @Override
            public List<ProjectRelease> getReleases() {
                return releaseHistoryProvider.get(project.getProjectKey());
            }

            @Override
            public Optional<FollowUpData> getScopeBaseline() {
                return timeline.getBaselineDate().map(d -> historyRepository.get(d, timezone, project.getProjectKey()));
            }

            @Override
            public List<ProjectProfileItem> getProjectProfile() {
                return projectService.getProjectProfile(projectKey);
            }
        };

        return new FollowUpSnapshot(timeline, data, cluster, valuesProvider);
    }

    public void storeSnapshots(ZoneId timezone) {
        log.info("Snapshots storage started...");
        for (ProjectFilterConfiguration pf : projectService.getTaskboardProjects()) {
            String projectKey = pf.getProjectKey();

            log.info("Snapshot storage of project " + projectKey + " started...");
            storeSnapshot(timezone, projectKey);
            log.info("Snapshot storage of project " + projectKey + " completed.");
        }
        log.info("Snapshots storage completed.");
        log.info("Publishing event for clearing the followup caches");
        eventPublisher.publishEvent(new SnapshotGeneratedEvent(this));
    }

    private void storeSnapshot(ZoneId timezone, String projectKey) {
        LocalDate date = LocalDateTime.ofInstant(clock.now(), timezone).toLocalDate();
        FollowUpData data = dataGenerator.generate(timezone, projectKey);

        historyRepository.save(projectKey, date, data);
        syncSynthesis(projectKey, date, timezone, true);
    }

    public void syncSynthesis(ZoneId timezone) {
        log.info("Synthesis sync started...");

        for (ProjectFilterConfiguration pf : projectService.getTaskboardProjects()) {
            String projectKey = pf.getProjectKey();

            log.info("Synthesis sync of project " + projectKey + " started...");
            getAvailableHistory(projectKey).forEach(date -> syncSynthesis(projectKey, date, timezone, false));
            log.info("Synthesis sync of project " + projectKey + " completed.");
        }

        log.info("Synthesis sync completed.");
    }

    private synchronized void syncSynthesis(String projectKey, LocalDate date, ZoneId timezone, boolean override) {
        synthesisSyncronizer.syncSynthesis(
                () -> getFromHistory(date, timezone, projectKey), 
                projectKey,
                date,
                override);
    }

    private interface FollowupDataSupplier {
        FollowUpData get(FollowupCluster cluster);
    }
}
