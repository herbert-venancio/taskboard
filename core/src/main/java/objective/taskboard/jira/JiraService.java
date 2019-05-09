package objective.taskboard.jira;

import static objective.taskboard.jira.JiraSearchService.postProcessWorklogs;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byKey;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byName;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byNames;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byValue;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byValueOrId;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byId;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.data.BlockCardValue;
import objective.taskboard.data.SubtaskDto;
import objective.taskboard.data.User;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraResolutionDto;
import objective.taskboard.jira.client.JiraServerInfoDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.data.JiraUser.UserDetails;
import objective.taskboard.jira.data.Transition;
import objective.taskboard.jira.data.FieldsRequiredInTransition;
import objective.taskboard.jira.data.Transitions;
import objective.taskboard.jira.data.Transitions.DoTransitionRequestBody;
import objective.taskboard.jira.data.plugin.RoleData;
import objective.taskboard.jira.data.plugin.UserDetail;
import objective.taskboard.jira.endpoint.JiraEndpoint;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import objective.taskboard.jira.properties.JiraProperties;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Service
@EnableConfigurationProperties(JiraProperties.class)
public class JiraService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JiraService.class);
    private static final String MSG_UNAUTHORIZED = "Incorrect user or password";
    private static final String MSG_FORBIDDEN = "The jira account is requesting a captcha challenge. Try logging in jira";

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
            if (StringUtils.isEmpty(password))
                throw new AccessDeniedException("The password can't be empty");
            
            jiraEndpoint
                .request(JiraServerInfoDto.Service.class, username, password)
                .get();
            
        } catch (RetrofitError ex) {
            checkAuthenticationError(ex, username);
            log.error("Authentication error for user " + username);
            throw ex;
        }
    }

    private void checkAuthenticationError(RetrofitError ex, String username) {
            HttpStatus httpStatus = HttpStatus.valueOf(ex.getResponse().getStatus());
            log.error("Authentication error " + httpStatus.value() + " for user " + username);

            if (httpStatus == HttpStatus.UNAUTHORIZED)
                throw new AccessDeniedException(MSG_UNAUTHORIZED, ex);

            if (httpStatus == HttpStatus.FORBIDDEN)
                throw new AccessDeniedException(MSG_FORBIDDEN, ex);
    }

    public void doTransition(String issueKey, Long transitionId, Map<String, Object> fields) {
        log.debug("⬣⬣⬣⬣⬣  doTransition");
        try {
            DoTransitionRequestBody requestBody = new DoTransitionRequestBody(transitionId, fields);
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
            DoTransitionRequestBody requestBody = new DoTransitionRequestBody(transitionId);
            jiraEndpointAsMaster.request(Transitions.Service.class).doTransition(issueKey, requestBody);
        } catch (RetrofitError e) {
            log.error("Error executing transition '" + transitionId + "' on issue '" + issueKey + "'", e);
            throw new FrontEndMessageException(e);
        }
    }

    /**
     * Will find a user in jira, by name/midlename/surname/userid
     * @param namePart
     * @return
     */
    public List<UserDetails> findUsers(String namePart) {
        log.debug("⬣⬣⬣⬣⬣  getUsersThatNameStartsWith");
        Iterable<UserDetails> response = jiraEndpointAsUser.request(JiraUser.Service.class).findUsers(namePart);
        return ImmutableList.copyOf(response);
    }

    public boolean assignToUsers(String issueKey, List<User> assigneeList) {
        return new AssignIssueToUserAction(issueKey, assigneeList).call();
    }
    
    public String getResolutions(String transitionName) {
        log.debug("⬣⬣⬣⬣⬣  getResolutions");


        List<JiraResolutionDto> resolutions = jiraEndpointAsUser.request(JiraResolutionDto.Service.class).all();

        if (properties.getTransitionsDoneNames().contains(transitionName)) {
            String done = properties.getResolutions().getDone().getName();
            return resolutions.stream()
                    .filter(r -> done.equals(r.name))
                    .map(r -> r.name)
                    .findFirst()
                    .orElse(null);
        } else if(properties.getTransitionsCancelNames().contains(transitionName))
            return properties.getResolutions().getCanceled().getName();
        return null;
    }

    public List<FieldsRequiredInTransition> getFieldsRequiredInTransitions(String issueKey, List<Long> transitionIds) {
        log.debug("⬣⬣⬣⬣⬣  getFieldsRequiredInTransitions");
        return jiraEndpointAsUser.request(FieldsRequiredInTransition.Service.class).post(issueKey, transitionIds);
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

    public Optional<JiraIssueDto> getIssueByKey(String key) {
        log.debug("⬣⬣⬣⬣⬣  getIssueByKey");
        try {
            return Optional.of(postProcessWorklogs(jiraEndpointAsUser, jiraEndpointAsUser.request(JiraIssueDto.Service.class).get(key)));
        }catch(retrofit.RetrofitError e) {
            if (e.getResponse().getStatus() == 404)
                return Optional.empty();
            throw e;
        }
    }

    public JiraIssueDto getIssueByKeyAsMaster(String key) {
        log.debug("⬣⬣⬣⬣⬣  getIssueByKeyAsMaster");
        try {
            return postProcessWorklogs(jiraEndpointAsMaster, jiraEndpointAsMaster.request(JiraIssueDto.Service.class).get(key));
        } catch(retrofit.RetrofitError e) {
            if (e.getResponse().getStatus() == 404)
                return null;
            throw e;
        }
    }

    public String createIssueAsMaster(JiraIssue.Input issueInput) {
        log.debug("⬣⬣⬣⬣⬣  createIssue (master)");
            JiraIssue issue = jiraEndpointAsMaster.request(JiraIssue.Service.class).create(issueInput);
            return issue.key;
    }

    public String createIssueAsUser(JiraIssue.Input issueInput) {
        log.debug("⬣⬣⬣⬣⬣  createIssue (user)");
        JiraIssue issue = jiraEndpointAsUser.request(JiraIssue.Service.class).create(issueInput);
        return issue.key;
    }

    @Cacheable(CacheConfiguration.JIRA_USER)
    public JiraUser getJiraUser(String username) {
        log.debug("⬣⬣⬣⬣⬣  getJiraUser");
        return jiraEndpointAsUser.request(JiraUser.Service.class).get(username);
    }

    public void block(String issueKey, BlockCardValue blockCardValue) {
        log.debug("⬣⬣⬣⬣⬣  setBlocked");
        Response result = updateIssue(issueKey, JiraIssue.Input.builder(properties)
                .blocked(true)
                .shouldBlockAllSubtasks(blockCardValue.isShouldBlockAllSubtasks())
                .lastBlockReason(blockCardValue.getLastBlockReason())
                .build());

        if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
            throw new FrontEndMessageException("Unexpected return code during setBlocked: " + result.getStatus());
    }

    public void unblock(String issueKey) {
        log.debug("⬣⬣⬣⬣⬣  setUnblocked");
        Response result = updateIssue(issueKey, JiraIssue.Input.builder(properties)
                .blocked(false)
                .shouldBlockAllSubtasks(false)
                .lastBlockReason("")
                .build());

        if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
            throw new FrontEndMessageException("Unexpected return code during setBlocked: " + result.getStatus());
    }

    public void saveDescription(String issueKey, String value) {
        Response result = updateIssue(issueKey, JiraIssue.Input.builder()
                .description(value)
                .build());

        if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
            throw new FrontEndMessageException("Unexpected return code during saveDescription: " + result.getStatus());
    }

    public void saveClassOfService(String issueKey, String classOfService) {

        Response result = updateIssue(issueKey, JiraIssue.Input.builder(properties)
                .classOfService(byValue(classOfService))
                .build());

        if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
            throw new FrontEndMessageException("Unexpected return code during saveClassOfService: " + result.getStatus());
    }
    
    public void saveSummary(String issueKey, String summary) {
        Response result = updateIssue(issueKey, JiraIssue.Input.builder()
                .summary(summary)
                .build());

        if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
            throw new FrontEndMessageException("Unexpected return code during saveSummary: " + result.getStatus());
    }

    public void saveTshirt(String issueKey, String size) {
        Response result = updateIssue(issueKey, JiraIssue.Input.builder(properties)
                .tShirtSize(byValue(size))
                .build());

        if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
            throw new FrontEndMessageException("Unexpected return code during saveTshirt: " + result.getStatus());
    }

    public void saveBallpark(String issueKey, String fieldId, String size) {
         Response result = updateIssue(issueKey, JiraIssue.Input.builder(properties)
                    .field(fieldId, byValueOrId(size))
                    .build());

         if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
            throw new FrontEndMessageException("Unexpected return code during saveBallpark: " + result.getStatus());
    }

    public void createSubtask(String issueKey, SubtaskDto subtask) {
        JiraIssueDto jiraIssue = this.getIssueByKeyAsMaster(issueKey);

        JiraIssue.CustomInputBuilder issueBuilder = JiraIssue.Input.builder(properties, jiraIssue.getProject().getKey(), jiraIssue.getIssueType().getId())
                .project(byKey(jiraIssue.getProject().getKey()))
                .issueType(byId(subtask.issuetype))
                .parent(byKey(jiraIssue.getKey()))
                .summary(subtask.summary);

        if (!StringUtils.isBlank(subtask.tShirtSize)) {
            issueBuilder.tShirtSize(byValue(subtask.tShirtSize));
        }

        JiraIssue.Input issueInput = issueBuilder.build();
        createIssueAsUser(issueInput);
    }

    public void setTeams(String issueKey, List<Long> teamsIds) {
        log.debug("⬣⬣⬣⬣⬣  setTeams");
        String assignedTeamCfId = properties.getCustomfield().getAssignedTeams().getId();
        
        new SetFieldAction(issueKey, assignedTeamCfId, StringUtils.join(teamsIds,",")).call();
    }

    private Response updateIssue(String issueKey, JiraIssue.Input request) {
        try {
            return jiraEndpointAsUser.request(JiraIssue.Service.class).update(issueKey, request);
        } catch (RetrofitError ex) {
            if (HttpStatus.valueOf(ex.getResponse().getStatus()) == HttpStatus.NOT_FOUND)
                throw new FrontEndMessageException("Issue "+issueKey+" can't be update because it wasn't found in Jira");
            throw new FrontEndMessageException(ex);
        }
    }

    public User getLoggedUser() {
        JiraUser loggedJiraUser = getJiraUser(CredentialsHolder.username());
        return getUser(loggedJiraUser);
    }

    protected User getUser(JiraUser jiraUser) {
        try {
            return new User(jiraUser.displayName, jiraUser.name, jiraUser.emailAddress);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<UserDetail.Role> getUserRoles(String userName) {
        UserDetail.Service service = jiraEndpointAsMaster.request(UserDetail.Service.class);
        try {
            return service.get(userName).roles;
        } catch (RetrofitError ex) {
            if(ex.getResponse().getStatus() == 404) {
                throw new RuntimeException("Unknown user", ex);
            } else {
                throw ex;
            }
        }
    }

    public List<RoleData> getVisibleRoles() {
        List<RoleData> roles = jiraEndpointAsUser.request(RoleData.Service.class)
                .allVisible();
        roles.sort(Comparator.comparing(role -> role.name));
        return roles;
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

    /**
     * Tries to assign issue to user. Moves last assignee to co-assignees if already has assignee.
     * Throws {@link FrontEndMessageException} if action failed
     */
    private class AssignIssueToUserAction implements Callable<Boolean> {

        private final String issueKey;
        private Queue<User> assignees;

        public AssignIssueToUserAction(String issueKey, List<User> assignees) {
            this.issueKey = issueKey;
            this.assignees = new LinkedList<User>(assignees);
        }

        @Override
        public Boolean call() {
            JiraIssue.Input request = buildRequest();
            send(request);

            return true;
        }

        private JiraIssue.Input buildRequest() {
            Optional<User> firstAssignee = Optional.ofNullable(assignees.poll());
            String assignee = firstAssignee.map(u->u.name).orElse(null);
            List<String> coAssigneesNames = assignees.stream().map(a -> a.name).collect(Collectors.toList());

            return JiraIssue.Input.builder(properties)
                    .assignee(byName(assignee))
                    .coAssignees(byNames(coAssigneesNames))
                    .build();
        }

        private void send(JiraIssue.Input request) {
            Response result = updateIssue(issueKey, request);
            if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
                throw new FrontEndMessageException("Unexpected return code during AssignIssueToUserAction: " + result.getStatus());
        }
    }
    
    
    /**
     * Tries to set a given field value.
     * Throws {@link FrontEndMessageException} if action failed
     */
    private class SetFieldAction implements Callable<Boolean> {

        private final String issueKey;
        private String field;
        private String value;

        public SetFieldAction(String issueKey, String field, String value) {
            this.issueKey = issueKey;
            this.field = field;
            this.value = value;
        }

        @Override
        public Boolean call() {
            JiraIssue.Input request = buildRequest();
            send(request);
            return true;
        }

        private JiraIssue.Input buildRequest() {
            return JiraIssue.Input.builder()
                    .field(field, value)
                    .build();
        }

        private void send(JiraIssue.Input request) {
            Response result = updateIssue(issueKey, request);
            if (HttpStatus.valueOf(result.getStatus()) != HttpStatus.NO_CONTENT)
                throw new FrontEndMessageException("Unexpected return code during SetFieldAction: " + result.getStatus());
        }
    }
}