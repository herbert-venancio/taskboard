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
package objective.taskboard.controller;

import static org.springframework.http.HttpStatus.OK;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.Issue;
import objective.taskboard.data.IssueTypeDto;
import objective.taskboard.data.SubtaskDto;
import objective.taskboard.database.TaskboardDatabaseService;
import objective.taskboard.filterPreferences.CardFieldFilterService;
import objective.taskboard.filterPreferences.UserPreferencesService;
import objective.taskboard.issue.CardStatusOrderCalculator;
import objective.taskboard.issue.SubtasksService;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.issueBuffer.IssueBufferState;
import objective.taskboard.issueTypeVisibility.IssueTypeVisibilityService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.linkgraph.LinkGraphProperties;
import objective.taskboard.team.UserTeamService;

@RestController
@RequestMapping("/ws/issues")
public class IssueController {

    @Autowired
    private TaskboardDatabaseService taskService;

    @Autowired
    private JiraService jiraBean;

    @Autowired
    private IssueTypeVisibilityService issueTypeVisibilityService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private CardFieldFilterService cardFieldFilterService;

    @Autowired
    private UserPreferencesService userPreferencesService;

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private LinkGraphProperties linkGraphProperties;

    @Autowired
    private UserTeamService userTeamService;

    @Autowired
    private SubtasksService subtasksService;

    @Autowired
    private CardStatusOrderCalculator statusOrderCalculator;

    @GetMapping
    public List<CardDto> issues() {
        List<Issue> visibleIssues = issueBufferService.getVisibleIssues();
        visibleIssues = cardFieldFilterService.getIssuesSelectedByLoggedUser(visibleIssues);
        return toCardDto(visibleIssues);
    }

    @RequestMapping(path = "/byids", method = RequestMethod.POST)
    public List<CardDto> byids(@RequestBody List<Long> issuesIds) {
        List<Issue> visibleIssues = issueBufferService.getVisibleIssuesByIds(issuesIds);
        visibleIssues = cardFieldFilterService.getIssuesSelectedByLoggedUser(visibleIssues);
        return toCardDto(visibleIssues);
    }

    @RequestMapping(path = "byKey/{issueKey}", method = RequestMethod.GET)
    public ResponseEntity<Object> byKey(@PathVariable String issueKey, @RequestParam(required = false, defaultValue ="true") boolean onlyVisible) {
        Optional<Issue> issueOptional = issueBufferService.getIssueByKey(issueKey, onlyVisible);
        if(issueOptional.isPresent()){
            Issue issue = issueOptional.get();
            return new ResponseEntity<>(toCardDto(issue), OK);
        }else{
            return new ResponseEntity<>("Card: " +issueKey+" not found.", HttpStatus.NOT_FOUND);
        }
    }
    
    @RequestMapping(path = "addMeAsAssignee", method = RequestMethod.POST)
    public CardDto addMeAsAssignee(@RequestBody String issueKey) {
        return toCardDto(issueBufferService.addMeAsAssignee(issueKey));
    }

    @RequestMapping(path = "addAssigneeToIssue/{issue}", method = RequestMethod.POST)
    public CardDto addAssigneeToIssue(@PathVariable("issue") String issueKey, @RequestBody UserRequestDTO user) {
        return toCardDto(issueBufferService.addAssigneeToIssue(issueKey, user.username));
    }

    @RequestMapping(path = "removeAssigneeFromIssue/{issue}", method = RequestMethod.POST)
    public CardDto removeAssigneeFromIssue(@PathVariable("issue") String issueKey, @RequestBody UserRequestDTO user) {
        return toCardDto(issueBufferService.removeAssigneeFromIssue(issueKey, user.username));
    }

    @RequestMapping(path = "addTeamToIssue/{issue}", method = RequestMethod.POST)
    public CardDto addTeamToIssue(@PathVariable("issue") String issueKey, @RequestBody TeamRequestDTO team) {
        return toCardDto(issueBufferService.addTeamToIssue(issueKey, team.id));
    }

    @RequestMapping(path = "replaceTeamInIssue/{issue}", method = RequestMethod.POST)
    public CardDto replaceTeamInIssue(@PathVariable("issue") String issueKey, @RequestBody ReplaceTeamRequestDTO replaceTeamRequest) {
        return toCardDto(issueBufferService.replaceTeamInIssue(
                issueKey,
                replaceTeamRequest.teamToReplace,
                replaceTeamRequest.replacementTeam));
    }

    @RequestMapping(path = "removeTeamFromIssue/{issue}", method = RequestMethod.POST)
    public CardDto removeTeamFromIssue(@PathVariable("issue") String issueKey, @RequestBody TeamRequestDTO team) {
        return toCardDto(issueBufferService.removeTeamFromIssue(
                issueKey,
                team.id));
    }

    @RequestMapping(path = "restoreDefaultTeams/{issue}", method = RequestMethod.POST)
    public CardDto restoreDefaultTeams(@PathVariable("issue") String issueKey) {
        return toCardDto(issueBufferService.restoreDefaultTeams(issueKey));
    }
    
    @RequestMapping(path ="saveDescription/{issue}", method = RequestMethod.POST)
    public CardDto saveDescription(@PathVariable("issue") String issueKey, @RequestBody(required=false) String description) {
        return toCardDto(issueBufferService.saveDescription(issueKey, description));
    }

    @RequestMapping(path ="saveClassOfService/{issue}", method = RequestMethod.POST)
    public CardDto saveClassOfService(@PathVariable("issue") String issueKey, @RequestBody String classOfService) {
        return toCardDto(issueBufferService.saveClassOfService(issueKey, classOfService));
    }

    @RequestMapping(path = "saveSummary/{issue}", method = RequestMethod.POST)
    public CardDto saveSummary(@PathVariable("issue") String issueKey, @RequestBody(required=false) String summary) {
        return toCardDto(issueBufferService.saveSummary(issueKey, summary));
    }

    @RequestMapping(path ="saveTshirt/{issue}", method = RequestMethod.POST)
    public CardDto saveTshirt(@PathVariable("issue") String issueKey, @RequestBody String size) {
        return toCardDto(issueBufferService.saveTshirt(issueKey, size));
    }
    
    @RequestMapping(path ="saveBallpark/{issue}", method = RequestMethod.POST)
    public CardDto saveBallpark(@PathVariable("issue") String issueKey, @RequestBody Map<String, String> parameters) {
        return toCardDto(issueBufferService.saveBallPark(issueKey, parameters.get("fieldId"), parameters.get("size")));
    }

    @RequestMapping(path="addSubtasks/{issue}", method = RequestMethod.POST, consumes="application/json")
    public CardDto addSubtasks(@PathVariable("issue") String issueKey, @RequestBody List<SubtaskDto> subtasks) {
        return toCardDto(issueBufferService.createSubtasks(issueKey, subtasks));
    }

    @RequestMapping(path = "transition", method = RequestMethod.POST)
    public CardDto transition(@RequestBody TransitionRequestDTO tr) throws JSONException {
        Map<String, Object> fields = tr.fields == null ? Collections.emptyMap() : tr.fields;
        return toCardDto(issueBufferService.doTransition(tr.issueKey, tr.transitionId, fields));
    }

    @RequestMapping(path = "resolutions/{transition}", method = RequestMethod.GET)
    public String resolutions(@PathVariable String transition) {
        return jiraBean.getResolutions(transition);
    }

    @RequestMapping(path = "cacheState")
    public IssueBufferState issueCacheState() {
        return issueBufferService.getState();
    }

    @RequestMapping("configuration")
    public Map<String, Object> configuration() {
        Map<String, Object> map = new HashMap<>();
        map.put("laneConfiguration", taskService.laneConfiguration());
        map.put("tShirtSizes", jiraProperties.getCustomfield().getTShirtSize().getSizes());
        map.put("issueTypes", getIssueTypes());
        map.put("issueTypesConfig", issueTypeVisibilityService.getIssueTypeVisibility());
        map.put("priorities", metadataService.getPrioritiesMetadata());
        map.put("statuses", metadataService.getStatusesMetadataAsLoggedInUser());
        map.put("urlJira", jiraProperties.getUrl());
        map.put("ballparks", jiraProperties.getFollowup().getBallparkMappings());
        map.put("urlLinkGraph", linkGraphProperties.getUrl());
        map.put("userPreferences", userPreferencesService.getLoggedUserPreferences().getPreferences());
        map.put("isNewUser", userPreferencesService.getLoggedUserPreferences().getIsNewUser());
        return map;
    }

    private List<IssueTypeDto> getIssueTypes() {
        return metadataService.getIssueTypeMetadataAsLoggedInUser().values().stream()
                .sorted((it1, it2) -> it1.getId().compareTo(it2.getId()))
                .map(it -> new IssueTypeDto(it, subtasksService.isSizeRequired(it), subtasksService.issueTypeIsVisibibleAtSubtaskCreation(it)))
                .collect(Collectors.toList());
    }

    @RequestMapping("card-field-filters")
    public List<CardFieldFilter> cardFieldFilters() throws InterruptedException, ExecutionException {
        return cardFieldFilterService.getFilterForLoggerUser();
    }

    @RequestMapping(path = "block-task/{issue}", method = RequestMethod.POST)
    public CardDto blockTask(@PathVariable("issue") String issue, @RequestBody String lastBlockReason) {
        jiraBean.block(issue, lastBlockReason);
        return toCardDto(issueBufferService.updateIssueBuffer(issue));
    }

    @RequestMapping(path = "unblock-task/{issue}", method = RequestMethod.POST)
    public CardDto unblockTask(@PathVariable("issue") String issue) {
        jiraBean.unblock(issue);
        return toCardDto(issueBufferService.updateIssueBuffer(issue));
    }

    @RequestMapping("reorder")
    public List<CardDto> reorder(@RequestBody String [] issues) {
        return toCardDto(issueBufferService.reorder(issues));
    }

    @RequestMapping("issue-buffer-state")
    public String getState() {
        return issueBufferService.getState().name();
    }

    private CardDto toCardDto(Issue issue) {
        List<Long> teamsVisibleToUser = userTeamService.getIdsOfTeamsVisibleToUser();
        return CardDto.fromIssue(issue, teamsVisibleToUser, statusOrderCalculator);
    }

    private List<CardDto> toCardDto(List<Issue> issues) {
        List<Long> teamsVisibleToUser = userTeamService.getIdsOfTeamsVisibleToUser();
        return issues.stream()
                .map(i->CardDto.fromIssue(i, teamsVisibleToUser, statusOrderCalculator))
                .collect(Collectors.toList());
    }

    private static class TransitionRequestDTO {
        public String issueKey;
        public Long transitionId;
        public Map<String, Object> fields;
    }

    private static class TeamRequestDTO {
        public Long id;
    }

    private static class ReplaceTeamRequestDTO {
        public Long teamToReplace;
        public Long replacementTeam;
    }

    private static class UserRequestDTO {
        public String username;
    }
}
