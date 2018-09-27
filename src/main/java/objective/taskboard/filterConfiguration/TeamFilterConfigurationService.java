package objective.taskboard.filterConfiguration;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.data.Team;
import objective.taskboard.domain.TeamFilterConfiguration;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.TeamFilterConfigurationCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class TeamFilterConfigurationService {

    @Autowired
    private TeamFilterConfigurationCachedRepository teamFilterConfigurationRepository;

    @Autowired
    private TeamCachedRepository teamRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserTeamCachedRepository userTeamRepository;

    @Cacheable(CacheConfiguration.CONFIGURED_TEAMS)
    private List<Team> getConfiguredTeams() {
        Set<Long> configuredTeamsIds = teamFilterConfigurationRepository.getCache()
                .stream()
                .map(TeamFilterConfiguration::getTeamId)
                .collect(toSet());

        return getTeamsByIds(configuredTeamsIds);
    }

    public List<Team> getDefaultTeamsInProjectsVisibleToUser() {
        Set<Long> visibleTeamsIds = projectService.getNonArchivedJiraProjectsForUser()
                .stream()
                .map(p -> p.getTeamId())
                .collect(toSet());
        
        return getTeamsByIds(visibleTeamsIds);
    }
    
    public List<Team> getGloballyVisibleTeams() {
        return teamRepository.findGloballyVisibles();
    }

    private List<Team> getTeamsByIds(Set<Long> teamsIds) {
        return teamRepository.getCache()
                .stream()
                .filter(t -> teamsIds.contains(t.getId()))
                .collect(toList());
    }

    public List<Team> getConfiguredTeamsByUser(String user) {
        return userTeamRepository.findByUserName(user)
                .stream()
                .map(ut -> getConfiguredTeamByName(ut.getTeam()))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private Team getConfiguredTeamByName(String teamName) {
        return getConfiguredTeams().stream()
                .filter(t -> Objects.equals(t.getName(), teamName))
                .findFirst()
                .orElse(null);
    }
}
