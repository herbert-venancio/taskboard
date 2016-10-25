package objective.taskboard.controller;

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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.data.AspectItemFilter;
import objective.taskboard.data.AspectSubitemFilter;
import objective.taskboard.data.Issue;
import objective.taskboard.data.Team;
import objective.taskboard.database.TaskboardDatabaseService;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.filterPreferences.UserPreferencesService;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.issueTypeVisibility.IssueTypeVisibilityService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.JiraService.PermissaoNegadaException;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectVisibilityService;
import objective.taskboard.linkgraph.LinkGraphProperties;

import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.google.common.collect.Lists;

@Slf4j
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
    private TeamFilterConfigurationService teamFilterConfigurationService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private ProjectVisibilityService projectService;

    @Autowired
    private UserPreferencesService userPreferencesService;

    @Value("${spring.datasource.username}")
    private String datasourceUserName;

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private LinkGraphProperties linkGraphProperties;

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public List<Issue> issues() throws SQLException, JSONException {
        return issueBufferService.getIssues(CredentialsHolder.username());
    }

    @RequestMapping(path = "assign", method = RequestMethod.POST)
    public Issue assign(@RequestBody Issue issue) throws JSONException {
        jiraBean.toggleAssignAndSubresponsavelToUser(issue.getIssueKey());
        return issueBufferService.updateIssueBuffer(issue.getIssueKey());
    }

    @RequestMapping(path = "create-issue", method = RequestMethod.POST)
    public Issue createIssue(@RequestBody Issue issue) throws JSONException {
        com.atlassian.jira.rest.client.api.domain.Issue parent = jiraBean.getIssueByKey(issue.getParent());
        IssueInputBuilder issueBuilder = new IssueInputBuilder(parent.getProject().getKey(), issue.getType());
        issueBuilder.setPriorityId(issue.getPriority());
        issueBuilder.setDueDate(new DateTime(issue.getDueDate().getTime()));
        issueBuilder.setDescription(issue.getDescription());
        issueBuilder.setSummary(issue.getSummary());
        issueBuilder.setFieldValue("parent", ComplexIssueInputFieldValue.with("key", parent.getKey()));
        issueBuilder.setFieldValue(jiraProperties.getCustomfield().getTShirtSize().getId(), ComplexIssueInputFieldValue.with("id", issue.getCustomFields().get(jiraProperties.getCustomfield().getTShirtSize()).toString())); 
        issueBuilder.setFieldValue(jiraProperties.getCustomfield().getClassOfService().getId(), ComplexIssueInputFieldValue.with("id", issue.getCustomFields().get(jiraProperties.getCustomfield().getClassOfService()).toString())); 
        log.info("Creating issue: " + issue);
        String issueKey = jiraBean.createIssue(issueBuilder.build());
        log.info("Created issue " + issueKey);
        com.atlassian.jira.rest.client.api.domain.Issue created = jiraBean.getIssueByKey(issueKey);
        return Issue.from(created.getKey(), created.getSummary());
    }

    @RequestMapping(path = "transition", method = RequestMethod.POST)
    public Map<String, Object> transition(@RequestBody TransitionDTO params) throws JSONException {
        jiraBean.doTransitionByName(params.issue, params.transition, params.resolution);
        issueBufferService.updateIssueBuffer(params.issue.getIssueKey());
        return new HashMap<>();
    }

    @RequestMapping(path = "transitions", method = RequestMethod.POST)
    public List<Transition> transitions(@RequestBody Issue issue) {
        try {
            List<Transition> transitionsByIssueKey = jiraBean.getTransitionsByIssueKey(issue.getIssueKey());
            return transitionsByIssueKey;
        } catch (PermissaoNegadaException e) {
            return Lists.newLinkedList();
        }
    }


    @RequestMapping(path = "resolutions/{transition}", method = RequestMethod.GET)
    public String resolutions(@PathVariable String transition) {
        return jiraBean.getResolutions(transition);
    }

    @RequestMapping(path = "subtasks", method = RequestMethod.POST)
    public List<Issue> subtasks(@RequestBody Issue issue) throws SQLException {
        return jiraBean.getIssueSubTasks(issue.getIssueKey());
    }

    @RequestMapping(path = "timetracking", method = RequestMethod.POST)
    public TimeTracking timetracking(@RequestBody Issue issue) throws JSONException {
        Integer timeEstimateMinutes = 0;
        Integer timeSpentMinutes = 0;

        com.atlassian.jira.rest.client.api.domain.Issue issueJira = jiraBean.getIssueByKey(issue.getIssueKey());

        timeEstimateMinutes += issueJira.getTimeTracking().getOriginalEstimateMinutes() != null ? issueJira.getTimeTracking().getOriginalEstimateMinutes() : 0;
        timeSpentMinutes += issueJira.getTimeTracking().getTimeSpentMinutes() != null ? issueJira.getTimeTracking().getTimeSpentMinutes() : 0;

        for (Subtask subTask : issueJira.getSubtasks()) {
            com.atlassian.jira.rest.client.api.domain.Issue subTaskJira = jiraBean.getIssueByKey(subTask.getIssueKey());

            timeEstimateMinutes += subTaskJira.getTimeTracking().getOriginalEstimateMinutes() != null ? subTaskJira.getTimeTracking().getOriginalEstimateMinutes() : 0;
            timeSpentMinutes += subTaskJira.getTimeTracking().getTimeSpentMinutes() != null ? subTaskJira.getTimeTracking().getTimeSpentMinutes() : 0;
        }

        return new TimeTracking(timeEstimateMinutes, null, timeSpentMinutes);
    }

    @RequestMapping("configuration")
    public Map<String, Object> configuration() throws SQLException, InterruptedException, ExecutionException {
        Map<String, Object> map = new HashMap<>();
        map.put("laneConfiguration", taskService.laneConfiguration());
        map.put("issueTypes", metadataService.getIssueTypeMetadata());
        map.put("issueTypesConfig", issueTypeVisibilityService.getIssueTypeVisibility());
        map.put("priorities", metadataService.getPrioritiesMetadata());
        map.put("statuses", metadataService.getStatusesMetadata());
        map.put("userPreferences", userPreferencesService.getUserPreferences());
        map.put("urlJira", jiraProperties.getUrl());
        map.put("urlLinkGraph", linkGraphProperties.getUrl());
        return map;
    }

    @RequestMapping("aspects-filter")
    public List<AspectItemFilter> aspectsFilter() throws InterruptedException, ExecutionException {
        List<AspectItemFilter> aspectsItem = new ArrayList<>();
        aspectsItem.addAll(getDefaultFieldFilterList());
        return aspectsItem;
    }

    @RequestMapping("impede-task/{issue}")
    public void impedeTask(@PathVariable("issue") String issue) {
        jiraBean.impede(issue);
        issueBufferService.updateIssueBuffer(issue);
    }

    @RequestMapping("unimpede-task/{issue}")
    public void unimpedeTask(@PathVariable("issue") String issue) {
        jiraBean.unimpede(issue);
        issueBufferService.updateIssueBuffer(issue);
    }

    private List<AspectItemFilter> getDefaultFieldFilterList() throws InterruptedException, ExecutionException {
        List<AspectItemFilter> defaultFieldFilters = new ArrayList<>();
        defaultFieldFilters.add(AspectItemFilter.from("Issue Type", "type", getIssueTypeFilterItems()));
        defaultFieldFilters.add(AspectItemFilter.from("Project", "projectKey", getProjectFilterItems()));
        defaultFieldFilters.add(AspectItemFilter.from("Team", "teams", getTeamFilterItems()));
        return defaultFieldFilters;
    }

    private List<AspectSubitemFilter> getIssueTypeFilterItems() throws InterruptedException, ExecutionException {
        return issueTypeVisibilityService.getVisibleIssueTypes()
                .stream()
                .map(t -> AspectSubitemFilter.from(t.getName(), t, true))
                .collect(Collectors.toList());
    }

    private List<AspectSubitemFilter> getProjectFilterItems() {
        return projectService.getProjectsVisibleToUser(CredentialsHolder.username())
                .stream().map(t -> AspectSubitemFilter.from(t.getName(), t.getKey(), true)).collect(Collectors.toList());
    }

    private List<AspectSubitemFilter> getTeamFilterItems() {
        final List<Team> visibleTeams = teamFilterConfigurationService.getVisibleTeams();
        sortTeam(visibleTeams);

        final List<AspectSubitemFilter> result = visibleTeams.stream()
                .map(t -> AspectSubitemFilter.from(t.getName(), t.getName(), true))
                .collect(Collectors.toList());

        return result;
    }

    private void sortTeam(final List<Team> visibleTeams) {
        final Comparator<Team> teamComparator = (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return o1.getName().compareTo(o2.getName());
        };
        Collections.sort(visibleTeams, teamComparator);
    }

    public static class TransitionDTO {
        public String transition;
        public String resolution;
        public Issue issue;
    }
}
