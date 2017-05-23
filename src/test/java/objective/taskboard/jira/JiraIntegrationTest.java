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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;

import objective.taskboard.auth.CredentialsHolder;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
//@WebAppConfiguration
public class JiraIntegrationTest {

    private static final long ISSUETYPE_ATIVIDADE = 11L;
    private static final String ISSUEKEY_OBJ_1951 = "OBJ-1951";
    private static final String PROJECTKEY_OBJ = "OBJ";
    private static final String PASSWORD_OBJECTIVE = "objective";
    private static final String USERNAME_LOUSA = "lousa";

    private static final String CUSTOMFIELD_CENTRO_DE_CUSTO = "customfield_10390";

    //@Autowired
    private JiraService jiraService;

    //@Before
    public void setUp() {
        Mockito.when(CredentialsHolder.username()).thenReturn(USERNAME_LOUSA);
        Mockito.when(CredentialsHolder.password()).thenReturn(PASSWORD_OBJECTIVE);
    }

    //	@Test
    public void issueByKey() {
        Issue issue = jiraService.getIssueByKey(ISSUEKEY_OBJ_1951);
        assertNotNull(issue);
        assertEquals(issue.getKey(), ISSUEKEY_OBJ_1951);
    }

    //	@Test
    public void transitionsByIssue() {
        Issue issue = jiraService.getIssueByKey(ISSUEKEY_OBJ_1951);
        List<Transition> transitions = jiraService.getTransitions(issue);
        assertFalse(transitions.isEmpty());
    }

    //	@Test
    public void transitionByName() {
        Issue issue = jiraService.getIssueByKey(ISSUEKEY_OBJ_1951);
        Transition transition = jiraService.getTransitionByName(issue, "Close Issue");
        assertNotNull(transition);
    }

    //	@Test
    public void createIssueAndResolveIssue() {
        Issue issue = createTestIssueArquitetura();
        Transition transition = jiraService.getTransitionByName(issue, "Resolve Issue");

        String resolutions = jiraService.getResolutions(transition.getName());
        jiraService.doTransition(issue, transition, null, resolutions);

//		Issue issueAfterTransition = jiraService.getIssueByKey(issue.getKey());
//		assertEquals("Resolvido", issueAfterTransition.getStatus().getName());
    }

//	@Test
//	public void createIssueAndResolveIssueByTransitionName() {
//		Issue issue = createTestIssueArquitetura();
//		
//		jiraFacade.doTransitionByName(issue, "Resolve Issue");
//		
//		Issue issueAfterTransition = jiraFacade.getIssue(issue.getKey());
//		assertEquals("Resolvido", issueAfterTransition.getStatus().getName());
//	}

    private Issue createTestIssueArquitetura() {
        final IssueInput issueInput = createIssueInputArquitetura(PROJECTKEY_OBJ, ISSUETYPE_ATIVIDADE, "Teste unitário de transição: To Do -> Doing", "Reunião", "Arquitetura");
        final String newIssueKey = jiraService.createIssue(issueInput);
        return jiraService.getIssueByKey(newIssueKey);
    }

    private IssueInput createIssueInputArquitetura(String projectKey, Long issueType, String summary, String component, String centroDeCusto) {
        final ComplexIssueInputFieldValue value = new ComplexIssueInputFieldValue(Collections.singletonMap("value", (Object) centroDeCusto));
        return new IssueInputBuilder(projectKey, issueType)
                .setSummary(summary)
                .setFieldValue(CUSTOMFIELD_CENTRO_DE_CUSTO, singletonList(value))
                .setComponentsNames(newArrayList(component))
                .build();
    }
}
