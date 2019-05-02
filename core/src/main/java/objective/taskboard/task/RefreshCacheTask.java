package objective.taskboard.task;

import static java.lang.String.format;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.domain.IssueTypeConfiguration;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.domain.TeamFilterConfiguration;
import objective.taskboard.repository.IssueTypeConfigurationCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.TeamFilterConfigurationCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

@Component
public class RefreshCacheTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RefreshCacheTask.class);
    @Autowired
    private UserTeamCachedRepository userTeamRepository;

    @Autowired
    private TeamCachedRepository teamRepository;

    @Autowired
    private TeamFilterConfigurationCachedRepository teamFilterConfigurationRepository;

    @Autowired
    private IssueTypeConfigurationCachedRepository issueTypeConfigurationCachedRepository;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Scheduled(fixedRate = 600000)
    public void refreshUserTeam() {
        log.info("Refreshing UserTeam cache...");
        userTeamRepository.loadCache();
        List<UserTeam> users = userTeamRepository.getCache();
        log.info(format("UserTeam cache refreshed, %d records loaded.", users.size()));
    }

    @Scheduled(fixedRate = 600000)
    public void refreshTeam() {
        log.info("Refreshing Team cache...");
        teamRepository.loadCache();
        List<Team> team = teamRepository.getCache();
        log.info(format("Team cache refreshed, %d records loaded.", team.size()));
    }

    @Scheduled(fixedRate = 21600000)
    public void refreshTeamFilterConfiguration() {
        log.info("Refreshing Team Filter Configuration cache...");
        teamFilterConfigurationRepository.loadCache();
        List<TeamFilterConfiguration> teamFilterConfiguration = teamFilterConfigurationRepository.getCache();
        log.info(format("Team Filter Configuration cache refreshed, %d records loaded.", teamFilterConfiguration.size()));
    }

    @Scheduled(fixedRate = 600000)
    public void refreshProject() {
        log.info("Refreshing Project cache...");
        projectRepository.loadCache();
        List<ProjectFilterConfiguration> project = projectRepository.getProjects();
        log.info(format("Project cache refreshed, %d records loaded.", project.size()));
    }

    @Scheduled(fixedRate = 600000)
    public void refreshIssueTypeVisibility() {
        log.info("Refreshing issue type visibility cache...");
        issueTypeConfigurationCachedRepository.loadCache();
        List<IssueTypeConfiguration> configs = issueTypeConfigurationCachedRepository.getCache();
        log.info(format("Issue type visibility cache refreshed, %d records loaded.", configs.size()));
    }
}
