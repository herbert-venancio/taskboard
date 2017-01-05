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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.database.TaskboardDatabaseService;
import objective.taskboard.jira.endpoint.JiraEndpoint;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;

@Slf4j
@Service
@EnableConfigurationProperties(JiraProperties.class)
public class JiraService {

    @Autowired
    private TaskboardDatabaseService taskboardDatabaseService;

    @Autowired
    private JiraProperties properties;

    @Autowired
    private JiraEndpoint jiraEndpoint;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    public boolean authenticate(String username, String password) {
        log.debug("⬣⬣⬣⬣⬣  authenticate");
        try {
            ServerInfo info = jiraEndpoint.executeRequest(username, password, client -> client.getMetadataClient().getServerInfo());
            return info != null;
        } catch (JiraServiceException e) {
            if (!e.getStatusCode().isPresent() || !e.getStatusCode().get().is4xxClientError())
                log.error("Authentication error", e);
            
            return false;
        }
    }

    public void doTransitionByName(objective.taskboard.data.Issue issue, String transitionName, String resolution) throws JSONException {
        String issueKey = issue.getIssueKey();
        Issue issueByJira = getIssueByKey(issueKey);
        Transition transitionByName = getTransitionByName(issueByJira, transitionName);

        doTransition(issueByJira, transitionByName, issue, resolution);

        assignSubResponsavel(issueByJira.getKey());
    }

    private void assignSubResponsavel(String issueKey) throws JSONException {
        log.debug("⬣⬣⬣⬣⬣  assignSubResponsavel");
        final Set<String> subResponsaveis = getSubResponsaveis(issueKey);
        final String jiraUser = CredentialsHolder.username();
        if (!subResponsaveis.contains(jiraUser)) {
            subResponsaveis.add(jiraUser);
            List<ComplexIssueInputFieldValue> issueFieldValues =
                    subResponsaveis.stream().map(subResponsavel -> ComplexIssueInputFieldValue.with("name", subResponsavel)).collect(Collectors.toList());
            updateIssue(issueKey, new IssueInputBuilder().setFieldValue(properties.getCustomfield().getCoAssignees().getId(), issueFieldValues));
        }
    }

    private void assignIssue(String key, String assignee) {
        log.debug("⬣⬣⬣⬣⬣  assignIssue");
        updateIssue(key, new IssueInputBuilder().setAssigneeName(assignee));
    }

    public void doTransition(Issue issueByJira, Transition transition, objective.taskboard.data.Issue issue, String resolution) {
        log.debug("⬣⬣⬣⬣⬣  doTransition");
        TransitionInput transitionInput;
        String issueComment =  issue.getComments();
        if (resolution == null) {
            transitionInput = issueComment.isEmpty()? new TransitionInput(transition.getId()) :
                new TransitionInput(transition.getId(), Comment.valueOf(issueComment));
        } else {
            final List<FieldInput> fields = newArrayList(new FieldInput("resolution", ComplexIssueInputFieldValue.with("name", resolution)));
            transitionInput = issueComment.isEmpty()? new TransitionInput(transition.getId(), fields):
                new TransitionInput(transition.getId(), fields, Comment.valueOf(issueComment));
        }
        jiraEndpointAsUser.executeRequest(client -> client.getIssueClient().transition(issueByJira, transitionInput));
    }

    public String getResolutions(String transitionName) {
        log.debug("⬣⬣⬣⬣⬣  getResolutions");
        Resolution resolutionTransition = null;

        Iterable<Resolution> response = jiraEndpointAsUser.executeRequest(client -> client.getMetadataClient().getResolutions());
        List<Resolution> resolutions = newArrayList(response);

        if (properties.getTransitionsDoneNames().contains(transitionName)) {
            String done = properties.getResolutions().getDone().getName();
            resolutionTransition = resolutions.stream().filter(resolution -> resolution.getName().equals(done)).findFirst().orElse(null);
            return resolutionTransition.getName();
        } else if(properties.getTransitionsCancelNames().contains(transitionName))
            return properties.getResolutions().getCanceled().getName();
        return null;
    }

    public List<Transition> getTransitions(Issue issue) {
        log.debug("⬣⬣⬣⬣⬣  getTransitions");
        Iterable<Transition> response = jiraEndpointAsUser.executeRequest(client -> client.getIssueClient().getTransitions(issue));
        return ImmutableList.copyOf(response);
    }

    public Issue getIssueByKey(String key) {
        log.debug("⬣⬣⬣⬣⬣  getIssueByKey");
        return jiraEndpointAsUser.executeRequest(client -> client.getIssueClient().getIssue(key));
    }

    public Issue getIssueByKeyAsMaster(String key) {
        log.debug("⬣⬣⬣⬣⬣  getIssueByKey");
        return jiraEndpointAsMaster.executeRequest(client -> client.getIssueClient().getIssue(key));
    }
    
    public String createIssue(IssueInput issueInput) {
        log.debug("⬣⬣⬣⬣⬣  createIssue");
        BasicIssue issue = jiraEndpointAsUser.executeRequest(client -> client.getIssueClient().createIssue(issueInput));
        return issue.getKey();
    }

    public Transition getTransitionByName(Issue issue, String transitionName) {
        return getTransitions(issue)
                .stream()
                .filter(transition -> transition.getName().equals(transitionName))
                .findFirst()
                .orElse(null);
    }

    public void toggleAssignAndSubresponsavelToUser(String key) throws JSONException {
        final Set<String> subResponsaveis = getSubResponsaveis(key);
        String jiraUser = CredentialsHolder.username();
        String assignee = "";

        if (subResponsaveis.contains(jiraUser)) {
            subResponsaveis.remove(jiraUser);
        } else {
            subResponsaveis.add(jiraUser);
            assignee = jiraUser;
        }

        if (!subResponsaveis.isEmpty() && assignee.isEmpty()) {
            assignee = subResponsaveis.iterator().next();
        }

        if (assignee.isEmpty()) {
            return;
        }

        assignSubResponsavel(key);
        assignIssue(key, assignee);
    }


    private Set<String> getSubResponsaveis(String key) throws JSONException {
        Set<String> subResponsaveis = new HashSet<>();
        final IssueField fieldSubResponsaveis = getIssueByKey(key).getField(properties.getCustomfield().getCoAssignees().getId());

        if (fieldSubResponsaveis != null && fieldSubResponsaveis.getValue() != null) {
            final JSONArray subResponsaveisJson = (JSONArray) fieldSubResponsaveis.getValue();

            for (int i = 0; i < subResponsaveisJson.length(); i++) {
                subResponsaveis.add(subResponsaveisJson.getJSONObject(i).getString("name"));
            }
        }

        return subResponsaveis;
    }

    public List<Transition> getTransitionsByIssueKey(String issueKey) {
        try {
            Issue issue = getIssueByKey(issueKey);
            return this.getTransitions(issue);
        } catch (RestClientException e) {
            if (HttpStatus.FORBIDDEN.value() == e.getStatusCode().or(0))
                throw new PermissaoNegadaException(e);

            throw e;
        }
    }

    public User getLoggedUser() {
        log.debug("⬣⬣⬣⬣⬣  getLoggedUser");
        return jiraEndpointAsUser.executeRequest(client -> client.getUserClient().getUser(CredentialsHolder.username()));
    }

    public List<objective.taskboard.data.Issue> getIssueSubTasks(String issueKey) {
        Issue issue = getIssueByKey(issueKey);
        List<objective.taskboard.data.Issue> subs = new ArrayList<>();
        if (issue.getIssueType().getId() == properties.getIssuetype().getDemand().getId())
            subs.addAll(taskboardDatabaseService.getSubtasksDemanda(issue.getKey()));
        else
            subs.addAll(taskboardDatabaseService.getSubtasks(issue.getKey()));
        return subs;
    }

    public void block(String issueKey, String lastBlockReason) {
        setBlocked(issueKey, true, lastBlockReason);
    }

    public void unblock(String issueKey) {
        setBlocked(issueKey, false, "");
    }

    private void setBlocked(String issueKey, boolean blocked, String lastBlockReason) {
        log.debug("⬣⬣⬣⬣⬣  setBlocked");
        String yesOptionId = properties.getCustomfield().getBlocked().getYesOptionId().toString();
        ComplexIssueInputFieldValue value = new ComplexIssueInputFieldValue(Collections.singletonMap("id", blocked ? yesOptionId : null));
        updateIssue(issueKey, new IssueInputBuilder()
                                    .setFieldValue(properties.getCustomfield().getBlocked().getId(), Collections.singletonList(value))
                                    .setFieldValue(properties.getCustomfield().getLastBlockReason().getId(), lastBlockReason));
    }

    private void updateIssue(String issueKey, IssueInputBuilder changes) {
        try {
            jiraEndpointAsUser.executeRequest(client -> client.getIssueClient().updateIssue(issueKey, changes.build()));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not update issue.", ex);
        }
    }

    public objective.taskboard.data.User getUser() {
        try {
            com.atlassian.jira.rest.client.api.domain.User jiraUser = getLoggedUser();
            return objective.taskboard.data.User.from(jiraUser.getDisplayName(), jiraUser.getName(), jiraUser.getEmailAddress(), jiraUser.getAvatarUri());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    @SuppressWarnings("serial")
    public static class PermissaoNegadaException extends RuntimeException {
        public PermissaoNegadaException(RestClientException e) {
            super(e);
        }
    }

    @SuppressWarnings("serial")
    public static class ParametrosDePesquisaInvalidosException extends RuntimeException {
        public ParametrosDePesquisaInvalidosException(RestClientException e) {
            super(e);
        }
    }

}
