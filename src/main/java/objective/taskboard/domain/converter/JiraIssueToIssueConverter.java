package objective.taskboard.domain.converter;

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

import static com.google.common.base.Strings.isNullOrEmpty;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType.Direction;
import com.atlassian.jira.rest.client.api.domain.User;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.data.CustomField;
import objective.taskboard.data.Issue;
import objective.taskboard.data.UserTeam;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.domain.ParentIssueLink;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraService;
import objective.taskboard.repository.ParentIssueLinkRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

@Slf4j
@Service
public class JiraIssueToIssueConverter {

    private static final String NO_TEAM = "NO TEAM";

    @Autowired
    private UserTeamCachedRepository userTeamRepository;

    @Autowired
    private TeamFilterConfigurationService teamFilterConfigurationService;

    @Autowired
    private ParentIssueLinkRepository parentIssueLinkRepository;

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private IssueColorService issueColorService;

    @Autowired
    private StartDateStepService startDateStepService;

    private List<String> parentIssueLinks;
    private List<com.atlassian.jira.rest.client.api.domain.Issue> issuesList;
    private List<String> usersInvalidTeam;

    @PostConstruct
    private void loadParentIssueLinks() {
        parentIssueLinks = parentIssueLinkRepository.findAll().stream()
                               .map(ParentIssueLink::getDescriptionIssueLink)
                               .collect(toList());
    }

    public List<Issue> convert(List<com.atlassian.jira.rest.client.api.domain.Issue> issueList) {
        issuesList = newArrayList(issueList);
        List<Issue> collect = issueList.stream()
                                  .map(this::convert)
                                  .collect(toList());
        System.out.println(collect.size());
        return collect;
    }

    public Issue convert(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        List<String> avatarSubResponsaveis = getSubResponsaveisAvatar(jiraIssue);
        List<String> nameSubResponsaveis = getSubResponsaveisName(jiraIssue);

        String subResponsavel1 = jiraIssue.getAssignee() != null ? jiraIssue.getAssignee().getAvatarUri("24x24").toString() : "";
        String subResponsavel2 = avatarSubResponsaveis.stream().filter(x -> !x.equals(subResponsavel1)).findFirst().orElse("");

        Map<String, Object> customFields = newHashMap();
        customFields.put(jiraProperties.getCustomfield().getClassOfService().getId(), getClassOfService(jiraIssue));
        customFields.put(jiraProperties.getCustomfield().getBlocked().getId(), getBlocked(jiraIssue));
        customFields.put(jiraProperties.getCustomfield().getLastBlockReason().getId(), getLastBlockReason(jiraIssue));
        customFields.putAll(getAdditionalEstimatedHours(jiraIssue));
        customFields.putAll(getTShirtSizes(jiraIssue));
        customFields.putAll(getRelease(jiraIssue));

        List<String> teamGroups = getTeamGroups(jiraIssue);

        usersInvalidTeam = usersInvalidTeam.stream()
                               .distinct()
                               .collect(toList());

        String color = issueColorService.getColor(getClassOfServiceId(jiraIssue));

        return Issue.from(
                jiraIssue.getKey(),
                jiraIssue.getProject().getKey(),
                jiraIssue.getProject().getName(),
                jiraIssue.getIssueType().getId(),
                jiraIssue.getIssueType().getIconUri().toASCIIString(),
                jiraIssue.getSummary() != null ? jiraIssue.getSummary() : "",
                jiraIssue.getStatus().getId(),
                startDateStepService.get(jiraIssue),
                subResponsavel1,
                subResponsavel2,
                getParentKey(jiraIssue),
                getParentType(jiraIssue),
                getParentTypeIconUri(jiraIssue),
                getRequired(jiraIssue),
                String.join(",", nameSubResponsaveis),
                jiraIssue.getAssignee() != null ? jiraIssue.getAssignee().getName() : "",
                String.join(",", usersInvalidTeam),
                jiraIssue.getPriority() != null ? jiraIssue.getPriority().getId() : 0l,
                jiraIssue.getDueDate() != null ? jiraIssue.getDueDate().toDate() : null,
                jiraIssue.getCreationDate().getMillis(),
                jiraIssue.getDescription() != null ? jiraIssue.getDescription() : "",
                teamGroups,
                getComments(jiraIssue),
                customFields,
                color
        );
    }

    private long getParentType(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        IssueField field = jiraIssue.getField("parent");

        if (field == null)
            return 0;

        JSONObject json = (JSONObject) field.getValue();

        try {
            return (json != null) ? json.getJSONObject("fields").getJSONObject("issuetype").getLong("id") : 0;
        } catch (JSONException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    private String getParentTypeIconUri(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        IssueField field = jiraIssue.getField("parent");
        if (field == null)
            return "";

        JSONObject json = (JSONObject) field.getValue();
        if (json != null) {
            try {
                return json.getJSONObject("fields").getJSONObject("issuetype").getString("iconUrl");
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }

        return "";
    }

    private String getParentKey(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        IssueField field = jiraIssue.getField("parent");

        if (field == null) {
            if (jiraIssue.getIssueLinks() != null) {
                List<IssueLink> links = newArrayList(jiraIssue.getIssueLinks());
                List<IssueLink> collect = links.stream()
                        .filter(l -> parentIssueLinks.contains(l.getIssueLinkType().getDescription()))
                        .collect(toList());
                if (!collect.isEmpty()) {
                    IssueLink link = collect.stream().findFirst().orElse(null);
                    return (link != null) ? link.getTargetIssueKey() : "";
                }
            }
            return "";
        }

        JSONObject json = (JSONObject) field.getValue();

        try {
            return (json != null) ? json.getString("key") : "";
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    private List<String> getRequired(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        return newArrayList(jiraIssue.getIssueLinks()).stream()
                .filter(this::isRequiresLink)
                .map(link -> link.getTargetIssueKey())
                .collect(toList());
    }

    private boolean isRequiresLink(final IssueLink link) {
         return jiraProperties.getIssuelink().getDependencies().contains(link.getIssueLinkType().getName())
             && link.getIssueLinkType().getDirection() == Direction.OUTBOUND;
    }

    private String getComments(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        Iterable<Comment> comment = jiraIssue.getComments();
        if (newArrayList(comment).size() == 0)
            return "";
        else
            return comment.iterator().next().toString();
    }

    private Map<String, Object> getTShirtSizes(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        Map<String, Object> tShirtSizes = newHashMap();

        for (String tSizeId : jiraProperties.getCustomfield().getTShirtSize().getIds()) {
            String tShirtSizeValue = getJsonValue(jiraIssue, tSizeId);

            if (isNullOrEmpty(tShirtSizeValue))
                continue;

            String fieldName = jiraIssue.getField(tSizeId).getName();
            CustomField tShirtSize = CustomField.from(fieldName, tShirtSizeValue);
            tShirtSizes.put(tSizeId, tShirtSize);
        }

        return tShirtSizes;
    }

    private String getClassOfService(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        String defaultClassOfService = jiraProperties.getCustomfield().getClassOfService().getDefaultValue();
        JSONObject json = getJSONClassOfService(jiraIssue);
        try {
            return json == null ? defaultClassOfService : json.getString("value");
        } catch (JSONException e) {
            return "";
        }
    }

    private Long getClassOfServiceId(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        JSONObject json = getJSONClassOfService(jiraIssue);
        try {
            return json == null ? 0L : json.getLong("id");
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            return 0L;
        }
    }

    private JSONObject getJSONClassOfService(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        String defaultClassOfService = jiraProperties.getCustomfield().getClassOfService().getDefaultValue();
        String classOfServiceId = jiraProperties.getCustomfield().getClassOfService().getId();

        String classOfService = getJsonValue(jiraIssue, classOfServiceId);
        boolean isNotDefaultClassOfService = !isNullOrEmpty(classOfService) && !classOfService.equals(defaultClassOfService);
        boolean isDemand = jiraIssue.getIssueType().getId().equals(jiraProperties.getIssuetype().getDemand().getId());
        if (isNotDefaultClassOfService || isDemand) {
            IssueField field = jiraIssue.getField(classOfServiceId);
            return field == null ? null : (JSONObject) field.getValue();
        }

        String parentKey = getParentKey(jiraIssue);
        com.atlassian.jira.rest.client.api.domain.Issue parent = getIssueByKey(parentKey);
        return parent == null ? null : getJSONClassOfService(parent);
    }

    private String getJsonValue(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue, String fieldId) {
        IssueField field = jiraIssue.getField(fieldId);

        if (field == null)
            return "";

        JSONObject json = (JSONObject) field.getValue();

        try {
            return (json != null) ? json.getString("value") : "";
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    private String getBlocked(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        IssueField field = jiraIssue.getField(jiraProperties.getCustomfield().getBlocked().getId());

        if (field == null) return "";

        JSONArray jsonArray = (JSONArray) field.getValue();

        try {
            return (jsonArray != null) ? jsonArray.getJSONObject(0).getString("value") : "";
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    private String getLastBlockReason(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        IssueField field = jiraIssue.getField(jiraProperties.getCustomfield().getLastBlockReason().getId());

        if (field == null || field.getValue() == null)
            return "";

        String lastBlockReason = field.getValue().toString();
        return lastBlockReason.length() > 200 ? lastBlockReason.substring(0, 200) + "..." : lastBlockReason;
    }

    private Map<String, Object> getAdditionalEstimatedHours(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        String additionalEstimatedHoursId = jiraProperties.getCustomfield().getAdditionalEstimatedHours().getId();
        IssueField field = jiraIssue.getField(additionalEstimatedHoursId);
        if (field == null || field.getValue() == null)
            return newHashMap();

        Double additionalHours = (Double) field.getValue();
        CustomField customFieldAdditionalHours = CustomField.from(field.getName(), additionalHours);
        Map<String, Object> mapAdditionalHours = newHashMap();
        mapAdditionalHours.put(additionalEstimatedHoursId, customFieldAdditionalHours);
        return mapAdditionalHours;
    }

    private Map<String, Object> getRelease(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        String releaseId = jiraProperties.getCustomfield().getRelease().getId();
        IssueField field = jiraIssue.getField(releaseId);
        if (field == null)
            return newHashMap();

        JSONObject json = (JSONObject) field.getValue();

        try {
            if (json == null) {
                String parentKey = getParentKey(jiraIssue);
                com.atlassian.jira.rest.client.api.domain.Issue parent = getIssueByKey(parentKey);
                if (parent == null)
                    return newHashMap();

                return getRelease(parent);
            }

            String release = json.getString("name");
            CustomField customFieldRelease = CustomField.from(field.getName(), release);
            Map<String, Object> mapRelease = newHashMap();
            mapRelease.put(releaseId, customFieldRelease);
            return mapRelease;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            return newHashMap();
        }
    }

    private List<String> getSubResponsaveisAvatar(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        IssueField field = jiraIssue.getField(jiraProperties.getCustomfield().getCoAssignees().getId());
        List<String> names = newArrayList();

        if (field == null)
            return names;

        JSONArray value = (JSONArray) field.getValue();

        if (value != null) {
            for (int i = 0; i < value.length(); i++) {
                try {
                    names.add(value.getJSONObject(i).getJSONObject("avatarUrls").getString("24x24"));
                } catch (JSONException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return names;
    }

    private List<String> getSubResponsaveisName(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        IssueField field = jiraIssue.getField(jiraProperties.getCustomfield().getCoAssignees().getId());
        List<String> names = newArrayList();

        if (field == null)
            return names;

        JSONArray value = (JSONArray) field.getValue();

        if (value != null) {
            for (int i = 0; i < value.length(); i++) {
                try {
                    names.add(value.getJSONObject(i).getString("name"));
                } catch (JSONException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return names;
    }

    private List<String> getTeamGroups(com.atlassian.jira.rest.client.api.domain.Issue issue) {
        List<UserTeam> users = getUsers(issue);
        if (users.isEmpty())
            return newArrayList(NO_TEAM);

        return users.stream()
                .filter(Objects::nonNull)
                .map(u -> u.getTeam())
                .distinct()
                .collect(toList());
    }

    private List<UserTeam> getUsers(com.atlassian.jira.rest.client.api.domain.Issue issue) {
        usersInvalidTeam = newArrayList();
        List<UserTeam> users = getUsersResponsaveis(issue);
        if (foundSomeUser(users))
            return users;

        users = getParentUsersResponsaveis(issue);
        if (foundSomeUser(users))
            return users;

        User reporter = issue.getReporter();
        if (reporter != null)
            users.addAll(getUserTeam(reporter.getName()));
        return users;
    }

    private List<UserTeam> getUsersResponsaveis(com.atlassian.jira.rest.client.api.domain.Issue issue) {
        List<UserTeam> users = newArrayList();
        User assignee = issue.getAssignee();
        if (assignee != null)
            users.addAll(getUserTeam(assignee.getName()));

        for (String subResponsavel : getSubResponsaveisName(issue))
            users.addAll(getUserTeam(subResponsavel));

        return users;
    }

    private boolean foundSomeUser(List<UserTeam> users) {
        return !users.isEmpty() || !usersInvalidTeam.isEmpty();
    }

    private List<UserTeam> getUserTeam(String userName) {
        List<UserTeam> usersTeam = userTeamRepository.findByUserName(userName)
                .stream()
                .filter(ut -> isTeamVisible(ut.getTeam()))
                .collect(toList());

        if (!usersTeam.isEmpty())
            return usersTeam;

        usersInvalidTeam.add(userName);
        return newArrayList();
    }

    private List<UserTeam> getParentUsersResponsaveis(com.atlassian.jira.rest.client.api.domain.Issue issue) {
        String parentKey = getParentKey(issue);
        com.atlassian.jira.rest.client.api.domain.Issue parent = getIssueByKey(parentKey);
        if (parent == null)
            return newArrayList();
        return getUsersResponsaveis(parent);
    }

    private com.atlassian.jira.rest.client.api.domain.Issue getIssueByKey(String parentKey) {
        if (isNullOrEmpty(parentKey))
            return null;

        com.atlassian.jira.rest.client.api.domain.Issue parent = issuesList.stream()
                .filter(i -> i.getKey().equals(parentKey))
                .findFirst()
                .orElse(null);

        if (parent != null)
            return parent;

        parent = jiraService.getIssueByKeyAsMaster(parentKey);
        issuesList.add(parent);
        return parent;
    }

    private boolean isTeamVisible(String team) {
        return teamFilterConfigurationService.getConfiguredTeams()
                .stream()
                .anyMatch(t -> Objects.equals(t.getName(), team));
    }
}
