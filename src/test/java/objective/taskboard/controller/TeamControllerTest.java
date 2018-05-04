package objective.taskboard.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.testUtils.ControllerTestUtils.getDefaultMockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.TeamRepository;
import objective.taskboard.repository.UserTeamRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TeamControllerTest.Configuration.class)
@DataJpaTest
@WebAppConfiguration
public class TeamControllerTest {

    interface VALID {
        String TEAM_NAME = "VALID_DEV";
        String TEAM_MANAGER = "Manager";
        String TEAM_COACH = "Coach";
        List<String> TEAM_MEMBERS = asList("thomas.developer", "graham.reviewer");
    }

    interface EMPTY {
        String TEAM_NAME = "EMPTY";
        String TEAM_MANAGER = "Manager";
        String TEAM_COACH = "Coach";
        List<String> TEAM_MEMBERS = emptyList();
    }

    interface INVALID {
        String TEAM_NAME = "INVALID";
    }

    private Long validId;
    private Long emptyId;
    private MockMvc mockMvc;

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
        mockMvc = getDefaultMockMvc(subject);
        Team taskb = new Team(VALID.TEAM_NAME, VALID.TEAM_MANAGER, VALID.TEAM_COACH, VALID.TEAM_MEMBERS);
        Team empty = new Team(EMPTY.TEAM_NAME, EMPTY.TEAM_MANAGER, EMPTY.TEAM_COACH, EMPTY.TEAM_MEMBERS);
        validId = teamRepository.save(taskb).getId();
        emptyId = teamRepository.save(empty).getId();
        teamCachedRepository.loadCache();
        System.out.println("End of setup");
    }

    @Test
    public void givenInvalidTeam_whenGet_thenIsNotFound() throws Exception {
        mockMvc.perform(get("/api/teams/{teamName}", INVALID.TEAM_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenValidTeam_whenGet_thenIsOk() throws Exception {
        mockMvc.perform(get("/api/teams/{teamName}", VALID.TEAM_NAME))
                .andExpect(status().isOk());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenEmptyTeam_whenUpdateTeamMembers_thenTeamIncludeMembers() throws Exception {
        String payload = createPayload(EMPTY.TEAM_NAME, "thomas.developer","graham.reviewer");
        mockMvc.perform(patch("/api/teams/{teamName}", EMPTY.TEAM_NAME).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        assertThat(teamRepository.findOne(emptyId).getMembers()).extracting(UserTeam::getUserName)
                .containsExactlyInAnyOrder("thomas.developer", "graham.reviewer");
        assertThat(userTeamRepository.findAll())
                .extracting(userTeam -> Pair.of(userTeam.getTeam(), userTeam.getUserName()))
                .containsExactlyInAnyOrder(
                        Pair.of(VALID.TEAM_NAME, "thomas.developer")
                        , Pair.of(VALID.TEAM_NAME, "graham.reviewer")
                        , Pair.of(EMPTY.TEAM_NAME, "thomas.developer")
                        , Pair.of(EMPTY.TEAM_NAME, "graham.reviewer")
                );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenValidTeam_whenUpdateTeamMembers_thenTeamMembersChangedAndOrphansAreRemoved() throws Exception {
        String payload = createPayload(VALID.TEAM_NAME, "graham.reviewer","john.doe");
        mockMvc.perform(patch("/api/teams/{teamName}", VALID.TEAM_NAME).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        assertThat(teamRepository.findOne(validId).getMembers()).extracting(UserTeam::getUserName)
                .containsExactlyInAnyOrder("graham.reviewer", "john.doe");
        assertThat(userTeamRepository.findAll())
                .extracting(userTeam -> Pair.of(userTeam.getTeam(), userTeam.getUserName()))
                .containsExactlyInAnyOrder(
                        Pair.of(VALID.TEAM_NAME, "graham.reviewer")
                        , Pair.of(VALID.TEAM_NAME, "john.doe")
                );
    }

    @Test
    public void givenEmptyTeam_whenUpdateTeamMemberIsNullOrEmpty_thenTeamMembersNotAdded() throws Exception {
        String payload = createPayload(VALID.TEAM_NAME, null, "");
        mockMvc.perform(patch("/api/teams/{teamName}", VALID.TEAM_NAME).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        assertThat(teamRepository.findOne(validId).getMembers())
                .isEmpty();
        assertThat(userTeamRepository.findAll())
                .isEmpty();
    }

    private static String createPayload(String teamName, String... teamMembers) {
        return "{"
                + "\"teamName\":" + addQuotes(teamName)
                + ",\"teamMembers\":["
                + Arrays.stream(teamMembers)
                    .map(TeamControllerTest::addQuotes)
                    .collect(Collectors.joining(","))
                + "]"
                + "}";
    }

    private static String addQuotes(String value) {
        if(value == null)
            return "null";
        else
            return "\"" + value + "\"";
    }
}
