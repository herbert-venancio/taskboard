package objective.taskboard.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.TeamRepository;
import objective.taskboard.repository.UserTeamRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TeamControllerTest.Configuration.class)
@DataJpaTest
public class TeamControllerTest {

    private static final String VALID_TEAM_NAME = "VALID_DEV";
    private static final String VALID_TEAM_MANAGER = "Manager";
    private static final String VALID_TEAM_COACH = "Coach";
    private static final List<String> VALID_TEAM_MEMBERS = asList("thomas.developer", "graham.reviewer");

    private static final String EMPTY_TEAM_NAME = "EMPTY";
    private static final String EMPTY_TEAM_MANAGER = "Manager";
    private static final String EMPTY_TEAM_COACH = "Coach";
    private static final List<String> EMPTY_TEAM_MEMBERS = emptyList();

    private static final String INVALID_TEAM_NAME = "INVALID";

    private Long validId;
    private Long emptyId;

    @EntityScan(basePackageClasses = Team.class)
    public static class Configuration {
        @Bean
        public JpaRepositoryFactoryBean<TeamRepository, Team, Long> teamRepository() {
            return new JpaRepositoryFactoryBean<>(TeamRepository.class);
        }

        @Bean
        public JpaRepositoryFactoryBean<UserTeamRepository, UserTeam, Long> userTeamRepository() {
            return new JpaRepositoryFactoryBean<>(UserTeamRepository.class);
        }

        @Bean
        public TeamCachedRepository teamCachedRepository() {
            return new TeamCachedRepository();
        }

        @Bean
        public TeamController teamController() {
            return new TeamController();
        }
    }

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserTeamRepository userTeamRepository;

    @Autowired
    private TeamCachedRepository teamCachedRepository;

    @Autowired
    private TeamController subject;

    @Before
    public void setup() {
        Team taskb = new Team(VALID_TEAM_NAME, VALID_TEAM_MANAGER, VALID_TEAM_COACH, VALID_TEAM_MEMBERS);
        Team empty = new Team(EMPTY_TEAM_NAME, EMPTY_TEAM_MANAGER, EMPTY_TEAM_COACH, EMPTY_TEAM_MEMBERS);
        validId = teamRepository.save(taskb).getId();
        emptyId = teamRepository.save(empty).getId();
        teamCachedRepository.loadCache();
    }

    @Test
    public void givenInvalidTeam_whenGet_thenIsNotFound() {
        assertThat(subject.getTeamMembers(INVALID_TEAM_NAME).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void givenValidTeam_whenGet_thenIsOk() {
        assertThat(subject.getTeamMembers(VALID_TEAM_NAME).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenEmptyTeam_whenUpdateTeamMembers_thenTeamIncludeMembers() {
        TeamControllerData payload = createPayload(EMPTY_TEAM_NAME, "thomas.developer","graham.reviewer");
        assertThat(subject.updateTeamMembers(EMPTY_TEAM_NAME, payload).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(teamRepository.findOne(emptyId).getMembers()).extracting(UserTeam::getUserName)
                .containsExactlyInAnyOrder("thomas.developer", "graham.reviewer");
        assertThat(userTeamRepository.findAll().stream().filter(userTeam -> EMPTY_TEAM_NAME.equals(userTeam.getTeam())))
                .extracting(userTeam -> Pair.of(userTeam.getTeam(), userTeam.getUserName()))
                .containsExactlyInAnyOrder(
                        Pair.of(EMPTY_TEAM_NAME, "thomas.developer")
                        , Pair.of(EMPTY_TEAM_NAME, "graham.reviewer")
                );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenValidTeam_whenUpdateTeamMembers_thenTeamMembersChangedAndOrphansAreRemoved() {
        TeamControllerData payload = createPayload(VALID_TEAM_NAME, "graham.reviewer","john.doe");
        assertThat(subject.updateTeamMembers(VALID_TEAM_NAME, payload).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(teamRepository.findOne(validId).getMembers()).extracting(UserTeam::getUserName)
                .containsExactlyInAnyOrder("graham.reviewer", "john.doe");
        assertThat(userTeamRepository.findAll().stream().filter(userTeam -> VALID_TEAM_NAME.equals(userTeam.getTeam())))
                .extracting(userTeam -> Pair.of(userTeam.getTeam(), userTeam.getUserName()))
                .containsExactlyInAnyOrder(
                        Pair.of(VALID_TEAM_NAME, "graham.reviewer")
                        , Pair.of(VALID_TEAM_NAME, "john.doe")
                );
    }

    @Test
    public void givenEmptyTeam_whenUpdateTeamMemberIsNullOrEmpty_thenTeamMembersNotAdded() {
        TeamControllerData payload = createPayload(VALID_TEAM_NAME, null, "");
        assertThat(subject.updateTeamMembers(VALID_TEAM_NAME, payload).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(teamRepository.findOne(validId).getMembers())
                .isEmpty();
        assertThat(userTeamRepository.findAll().stream().filter(userTeam -> VALID_TEAM_NAME.equals(userTeam.getTeam())))
                .isEmpty();
    }

    private static TeamControllerData createPayload(String teamName, String... teamMembers) {
        TeamControllerData payload = new TeamControllerData();
        payload.teamName = teamName;
        payload.manager = null;
        payload.teamMembers = Lists.newArrayList(teamMembers);
        return payload;
    }
}
