package objective.taskboard.jira;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.google.common.collect.Lists;
import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.repository.UserTeamCachedRepository;
import org.mockito.Mockito;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
//@WebAppConfiguration
public class JiraIssueServiceTest {

    private static final String PASSWORD_OBJECTIVE = "objective";
    private static final String USERNAME_LOUSA = "lousa";

    //@Autowired
    private MetadataService metadataService;

    //@Autowired
    private JiraIssueService jiraIssueService;

//	@Autowired
//	private FilterCachedRepository filterRepository;

    //@Autowired
    private UserTeamCachedRepository userTeamRepository;

    //@Before
    public void setUp() {
        Mockito.when(CredentialsHolder.username()).thenReturn(USERNAME_LOUSA);
        Mockito.when(CredentialsHolder.password()).thenReturn(PASSWORD_OBJECTIVE);
    }

    //@Test
    public void createJqlNotEmpty() {
        assertFalse(jiraIssueService.createJql(newArrayList()).isEmpty());
    }

    //@Test
    public void createJqlWithProjects() {
        String jql = jiraIssueService.createJql(newArrayList());
        assertFalse(jql.isEmpty());
        assertTrue(jql.startsWith("(project in ("));
    }

    //@Test
    public void createJqlWithProjectsAndIssueConfiguration() {
        List<IssuesConfiguration> configs = Lists.newArrayList();
        configs.add(IssuesConfiguration.from(1L, 1L));
        configs.add(IssuesConfiguration.from(2L, 2L));

        String jql = jiraIssueService.createJql(configs);
        assertFalse(jql.isEmpty());
        assertTrue(jql.startsWith("(project in ("));
        assertTrue(jql.contains("(status=1 and type=1)"));
    }

    //@Test(expected = ParametrosDePesquisaInvalidosException.class)
    public void searchIssuesWithInvalidArguments() {
        List<IssuesConfiguration> configs = Lists.newArrayList();
        configs.add(IssuesConfiguration.from(1L, 1L));
        configs.add(IssuesConfiguration.from(2L, 2L));

        jiraIssueService.searchIssues(configs, null);
    }

    //@Test
    public void searchIssues() throws Exception {
        List<IssueType> types = newArrayList(metadataService.getIssueTypeMetadata().values());
        List<Status> statuses = newArrayList(metadataService.getStatusesMetadata().values());

        List<IssuesConfiguration> configs = Lists.newArrayList();
        configs.add(IssuesConfiguration.from(types.stream().findFirst().get().getId(), statuses.stream().findFirst().get().getId()));

        List<Issue> searchIssues = jiraIssueService.searchIssues(configs, null);
        assertNotNull(searchIssues);
        assertFalse(searchIssues.isEmpty());
    }

    //@Test
    public void searchIssuesWithTwoClauses() throws Exception {
        List<IssueType> types = newArrayList(metadataService.getIssueTypeMetadata().values());
        List<Status> statuses = newArrayList(metadataService.getStatusesMetadata().values());

        List<IssuesConfiguration> configs = Lists.newArrayList();
        configs.add(IssuesConfiguration.from(types.get(1).getId(), statuses.get(1).getId()));
        configs.add(IssuesConfiguration.from(types.get(2).getId(), statuses.get(2).getId()));

        assertNotNull(jiraIssueService.searchIssues(configs, null));
    }

    //	@Test
    public void searchAllFilters() {
        List<Issue> searchIssues = jiraIssueService.searchAll("");
        assertNotNull(searchIssues);
        assertFalse(searchIssues.isEmpty());
    }

    //	@Test
    public void userTeam() {
        new Thread(() -> {
            userTeamRepository.loadCache();
        }).start();
    }

}
