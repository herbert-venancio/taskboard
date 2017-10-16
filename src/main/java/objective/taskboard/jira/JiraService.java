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
package objective.taskboard.jira;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.google.common.collect.ImmutableList;

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.jira.data.Transition;
import objective.taskboard.jira.data.Transitions;
import objective.taskboard.jira.data.Transitions.DoTransitionRequestBody;
import objective.taskboard.jira.endpoint.JiraEndpoint;
import objective.taskboard.jira.endpoint.JiraEndpoint.Request;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import retrofit.RetrofitError;

@Service
@EnableConfigurationProperties(JiraProperties.class)
public class JiraService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JiraService.class);
    private static String MSG_UNAUTHORIZED = "Incorrect user or password";
    private static String MSG_FORBIDDEN = "The jira account is requesting a captcha challenge. Try logging in jira";

    @Autowired
    private JiraProperties properties;

    @Autowired
    private JiraEndpoint jiraEndpoint;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    public void authenticate(String username, String password) {
        log.debug("⬣⬣⬣⬣⬣  authenticate");
        try {
            Request<ServerInfo> request = client -> client.getMetadataClient().getServerInfo();
            if (StringUtils.isEmpty(password))
                throw new AccessDeniedException("The password can't be empty");
            ServerInfo info = jiraEndpoint.executeRequest(username, password, request);
            if (info == null)
                throw new RuntimeException("The server did not respond");
        } catch (RuntimeException ex) {
            checkAuthenticationError(ex, username);
            log.error("Authentication error for user " + username);
            throw ex;
        }
    }

    private void checkAuthenticationError(RuntimeException ex, String username) {
        if (ex instanceof JiraServiceException) {
            JiraServiceException jse = (JiraServiceException) ex;
            if (!jse.getStatusCode().isPresent())
                throw new IllegalStateException("Jira return an unrecognized error during authentication.");
            
            HttpStatus httpStatus = jse.getStatusCode().get();
            log.error("Authentication error " + httpStatus.value() + " for user " + username);

            if (httpStatus == HttpStatus.UNAUTHORIZED)
                throw new AccessDeniedException(MSG_UNAUTHORIZED, ex);

            if (httpStatus == HttpStatus.FORBIDDEN)
                throw new AccessDeniedException(MSG_FORBIDDEN, ex);

        }
    }

    public void doTransition(String issueKey, Long transitionId, String resolutionName) {
        log.debug("⬣⬣⬣⬣⬣  doTransition");
        try {
            DoTransitionRequestBody requestBody = new DoTransitionRequestBody(transitionId, resolutionName);
            jiraEndpointAsUser.request(Transitions.Service.class).doTransition(issueKey, requestBody);
        } catch (RetrofitError e) {
            throw new FrontEndMessageException(e);
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void doTransitionAsMaster(String issueKey, Long transitionId) {
        log.debug("⬣⬣⬣⬣⬣  doTransition (master)");
        try {
            DoTransitionRequestBody requestBody = new DoTransitionRequestBody(transitionId, null);
            jiraEndpointAsMaster.request(Transitions.Service.class).doTransition(issueKey, requestBody);
        } catch (RetrofitError e) {
            throw new FrontEndMessageException(e);
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void assignSubResponsavel(String issueKey) {
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

    public List<Transition> getTransitions(String issueKey) {
        log.debug("⬣⬣⬣⬣⬣  getTransitions");
        Iterable<Transition> response = jiraEndpointAsUser.request(Transitions.Service.class).get(issueKey).transitions;
        return ImmutableList.copyOf(response);
    }

    public List<Transition> getTransitionsAsMaster(String issueKey) {
        log.debug("⬣⬣⬣⬣⬣  getTransitions (master)");
        Iterable<Transition> response = jiraEndpointAsMaster.request(Transitions.Service.class).get(issueKey).transitions;
        return ImmutableList.copyOf(response);
    }

    public Issue getIssueByKey(String key) {
        log.debug("⬣⬣⬣⬣⬣  getIssueByKey");
        return jiraEndpointAsUser.executeRequest(client -> client.getIssueClient().getIssue(key));
    }

    public Issue getIssueByKeyAsMaster(String key) {
        log.debug("⬣⬣⬣⬣⬣  getIssueByKeyAsMaster");
        return jiraEndpointAsMaster.executeRequest(client -> client.getIssueClient().getIssue(key));
    }

    public String createIssue(IssueInput issueInput) {
        log.debug("⬣⬣⬣⬣⬣  createIssue");
        BasicIssue issue = jiraEndpointAsUser.executeRequest(client -> client.getIssueClient().createIssue(issueInput));
        return issue.getKey();
    }

    public String createIssueAsMaster(IssueInput issueInput) {
        log.debug("⬣⬣⬣⬣⬣  createIssue (master)");
        BasicIssue issue = jiraEndpointAsMaster.executeRequest(client -> client.getIssueClient().createIssue(issueInput));
        return issue.getKey();
    }

    public void toggleAssignAndSubresponsavelToUser(String key) {
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

    private Set<String> getSubResponsaveis(String key) {
        try {
            Set<String> subResponsaveis = new HashSet<>();
            final IssueField fieldSubResponsaveis = getIssueByKey(key)
                    .getField(properties.getCustomfield().getCoAssignees().getId());

            if (fieldSubResponsaveis != null && fieldSubResponsaveis.getValue() != null) {
                final JSONArray subResponsaveisJson = (JSONArray) fieldSubResponsaveis.getValue();

                for (int i = 0; i < subResponsaveisJson.length(); i++) {
                    subResponsaveis.add(subResponsaveisJson.getJSONObject(i).getString("name"));
                }
            }

            return subResponsaveis;
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    public User getLoggedUser() {
        log.debug("⬣⬣⬣⬣⬣  getLoggedUser");
        return jiraEndpointAsUser.executeRequest(client -> client.getUserClient().getUser(CredentialsHolder.username()));
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
        public PermissaoNegadaException(Exception e) {
            super(e);
        }
    }

    @SuppressWarnings("serial")
    public static class ParametrosDePesquisaInvalidosException extends RuntimeException {
        public ParametrosDePesquisaInvalidosException(Exception e) {
            super(e);
        }
    }

}
