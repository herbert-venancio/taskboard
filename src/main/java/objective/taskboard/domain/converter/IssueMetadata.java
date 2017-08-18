/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
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
package objective.taskboard.domain.converter;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType.Direction;

import objective.taskboard.data.CustomField;
import objective.taskboard.jira.JiraProperties;

public class IssueMetadata {

    private final JiraProperties jiraProperties;
    private final Logger log;

    private final String projectKey;
    private final String assignee;
    private final String reporter;
    private final IssueParent realParent;
    private final List<IssueCoAssignee> coAssignees;
    private final CustomField classOfService;
    private final String blocked;
    private final String lastBlockReason;
    private final Map<String, CustomField> tShirtSizes;
    private final List<String> comments;
    private final List<String> dependenciesIssuesKey;
    private final String linkedParentKey;
    private final Map<String, CustomField> additionalEstimatedHours;
    private final Map<String, CustomField> release;
    private final List<String> labels;
    private final List<String> components;

    public IssueMetadata(Issue issue, JiraProperties jiraProperties, List<String> parentIssueLinks, Logger log) {
        this.jiraProperties = jiraProperties;
        this.log = log;
        this.projectKey = issue.getProject().getKey();
        this.assignee = issue.getAssignee() == null ? null : issue.getAssignee().getName();
        this.reporter = issue.getReporter() == null ? null : issue.getReporter().getName();
        this.realParent = extractRealParent(issue);
        this.linkedParentKey = extractLinkedParentKey(issue, parentIssueLinks);
        this.coAssignees = extractCoAssignees(issue);
        this.classOfService = extractClassOfService(issue);
        this.blocked = extractBlocked(issue);
        this.lastBlockReason = extractLastBlockReason(issue);
        this.tShirtSizes = extractTShirtSizes(issue);
        this.comments = extractComments(issue);
        this.dependenciesIssuesKey = extractDependenciesIssues(issue);
        this.additionalEstimatedHours = extractAdditionalEstimatedHours(issue);
        this.release = extractRelease(issue);
        this.labels = extractLabels(issue);
        this.components = extractComponents(issue);
    }

    private IssueParent extractRealParent(Issue issue) {
        IssueField field = issue.getField("parent");

        if (field == null)
            return null;

        JSONObject json = (JSONObject) field.getValue();

        if (json == null)
            return null;

        try {
            String key = json.getString("key");
            JSONObject type = json.getJSONObject("fields").getJSONObject("issuetype");
            long typeId = type.getLong("id");
            String typeIconUrl = type.getString("iconUrl");
            return new IssueParent(key, typeId, typeIconUrl);
        } catch (JSONException e) {
            logErrorExtractField(issue, field, e);
            return null;
        }
    }

    private String extractLinkedParentKey(Issue issue, List<String> parentIssueLinks) {
        if (issue.getIssueLinks() == null || issue.getIssueType().getId() == jiraProperties.getIssuetype().getDemand().getId())
            return null;

        List<IssueLink> links = newArrayList(issue.getIssueLinks()).stream()
                .filter(l -> parentIssueLinks.contains(l.getIssueLinkType().getDescription()))
                .collect(toList());

        if (links.isEmpty())
            return null;

        return links.get(0).getTargetIssueKey();
    }

    private List<IssueCoAssignee> extractCoAssignees(Issue issue) {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getCoAssignees().getId());

        if (field == null)
            return newArrayList();

        JSONArray value = (JSONArray) field.getValue();

        if (value == null)
            return newArrayList();

        List<IssueCoAssignee> coAssignees = newArrayList();
        for (int i = 0; i < value.length(); i++) {
            try {
                String name = value.getJSONObject(i).getString("name");
                String avatarUrl = value.getJSONObject(i).getJSONObject("avatarUrls").getString("24x24");
                coAssignees.add(new IssueCoAssignee(name, avatarUrl));
            } catch (JSONException e) {
                logErrorExtractField(issue, field, e);
            }
        }

        return coAssignees;
    }

    private CustomField extractClassOfService(Issue issue) {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getClassOfService().getId());

        if (field == null)
            return null;

        JSONObject json = (JSONObject) field.getValue();

        if (json == null)
            return null;

        try {
            Long optionId = json.getLong("id");
            String value = json.getString("value");
            return new CustomField(field.getName(), value, optionId);
        } catch (JSONException e) {
            logErrorExtractField(issue, field, e);
            return null;
        }
    }

    private String extractBlocked(Issue issue) {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getBlocked().getId());

        if (field == null)
            return "";

        JSONArray jsonArray = (JSONArray) field.getValue();

        if (jsonArray == null || jsonArray.length() == 0)
            return "";

        try {
            return jsonArray.getJSONObject(0).getString("value");
        } catch (JSONException e) {
            logErrorExtractField(issue, field, e);
            return "";
        }
    }

    private String extractLastBlockReason(Issue issue) {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getLastBlockReason().getId());

        if (field == null || field.getValue() == null)
            return "";

        String lastBlockReason = field.getValue().toString();
        return lastBlockReason.length() > 200 ? lastBlockReason.substring(0, 200) + "..." : lastBlockReason;
    }

    private Map<String, CustomField> extractTShirtSizes(Issue issue) {
        Map<String, CustomField> tShirtSizes = newHashMap();

        for (String tSizeId : jiraProperties.getCustomfield().getTShirtSize().getIds()) {
            String tShirtSizeValue = extractTShirtSize(issue, tSizeId);

            if (isNullOrEmpty(tShirtSizeValue))
                continue;

            String fieldName = issue.getField(tSizeId).getName();
            CustomField tShirtSize = new CustomField(fieldName, tShirtSizeValue);
            tShirtSizes.put(tSizeId, tShirtSize);
        }

        return tShirtSizes;
    }

    private String extractTShirtSize(Issue issue, String tShirtSizeId) {
        IssueField field = issue.getField(tShirtSizeId);

        if (field == null)
            return "";

        JSONObject json = (JSONObject) field.getValue();

        try {
            return json != null ? json.getString("value") : "";
        } catch (JSONException e) {
            logErrorExtractField(issue, field, e);
            return "";
        }
    }

    private List<String> extractComments(Issue issue) {
        if (issue.getComments() == null)
            return newArrayList();

        return newArrayList(issue.getComments()).stream()
               .map(Comment::toString)
               .collect(toList());
    }

    private List<String> extractDependenciesIssues(Issue issue) {
        if (issue.getIssueLinks() == null)
            return newArrayList();

        return newArrayList(issue.getIssueLinks()).stream()
                .filter(this::isDependencyLink)
                .map(link -> link.getTargetIssueKey())
                .collect(toList());
    }

    private boolean isDependencyLink(final IssueLink link) {
        return jiraProperties.getIssuelink().getDependencies().contains(link.getIssueLinkType().getName())
            && link.getIssueLinkType().getDirection() == Direction.OUTBOUND;
    }

    private Map<String, CustomField> extractAdditionalEstimatedHours(Issue issue) {
        String additionalHoursId = jiraProperties.getCustomfield().getAdditionalEstimatedHours().getId();
        IssueField field = issue.getField(additionalHoursId);
        if (field == null || field.getValue() == null)
            return newHashMap();

        Double additionalHours = (Double) field.getValue();
        CustomField customFieldAdditionalHours = new CustomField(field.getName(), additionalHours);
        Map<String, CustomField> mapAdditionalHours = newHashMap();
        mapAdditionalHours.put(additionalHoursId, customFieldAdditionalHours);
        return mapAdditionalHours;
    }

    private Map<String, CustomField> extractRelease(Issue issue) {
        String releaseId = jiraProperties.getCustomfield().getRelease().getId();
        IssueField field = issue.getField(releaseId);

        if (field == null)
            return newHashMap();

        JSONObject json = (JSONObject) field.getValue();

        if (json == null)
            return newHashMap();

        try {
            String release = json.getString("name");
            CustomField customFieldRelease = new CustomField(field.getName(), release);
            Map<String, CustomField> mapRelease = newHashMap();
            mapRelease.put(releaseId, customFieldRelease);
            return mapRelease;
        } catch (JSONException e) {
            logErrorExtractField(issue, field, e);
            return newHashMap();
        }
    }

    private List<String> extractLabels(Issue issue) {
        if (issue.getLabels() == null)
            return newArrayList();

        return issue.getLabels().stream().collect(toList());
    }

    private List<String> extractComponents(Issue issue) {
        if (issue.getComponents() == null)
            return newArrayList();

        return newArrayList(issue.getComponents()).stream()
                .map(BasicComponent::getName)
                .collect(toList());
    }

    private void logErrorExtractField(Issue issue, IssueField field, JSONException e) {
        log.error("Error extracting " + field.getName() + " from issue " + issue.getKey() + ": " + e.getMessage(), e);
    }

    public String getParentKey() {
        if (getRealParent() != null)
            return getRealParent().getKey();
        if (getLinkedParentKey() != null)
            return getLinkedParentKey();
        return "";
    }

    static class IssueParent {
        private final String key;
        private final long typeId;
        private final String typeIconUrl;

        public IssueParent(String key, long issueTypeId, String issueTypeIconUrl) {
            this.key = key;
            this.typeId = issueTypeId;
            this.typeIconUrl = issueTypeIconUrl;
        }

        public String getKey() {
            return this.key;
        }

        public long getTypeId() {
            return this.typeId;
        }

        public String getTypeIconUrl() {
            return this.typeIconUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IssueParent that = (IssueParent) o;

            if (typeId != that.typeId) return false;
            if (key != null ? !key.equals(that.key) : that.key != null) return false;
            return typeIconUrl != null ? typeIconUrl.equals(that.typeIconUrl) : that.typeIconUrl == null;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (int) (typeId ^ (typeId >>> 32));
            result = 31 * result + (typeIconUrl != null ? typeIconUrl.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "IssueMetadata.IssueParent(key=" + this.getKey() + ", typeId=" + this.getTypeId() + ", typeIconUrl=" + this.getTypeIconUrl() + ")";
        }
    }


    static class IssueCoAssignee {
        private final String name;
        private final String avatarUrl;

        public IssueCoAssignee(String name, String avatarUrl) {
            this.name = name;
            this.avatarUrl = avatarUrl;
        }

        public String getName() {
            return this.name;
        }

        public String getAvatarUrl() {
            return this.avatarUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IssueCoAssignee that = (IssueCoAssignee) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            return avatarUrl != null ? avatarUrl.equals(that.avatarUrl) : that.avatarUrl == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (avatarUrl != null ? avatarUrl.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "IssueMetadata.IssueCoAssignee(name=" + this.getName() + ", avatarUrl=" + this.getAvatarUrl() + ")";
        }
    }

    public JiraProperties getJiraProperties() {
        return this.jiraProperties;
    }

    public Logger getLog() {
        return this.log;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getReporter() {
        return reporter;
    }

    public Map<String, CustomField> gettShirtSizes() {
        return tShirtSizes;
    }

    public IssueParent getRealParent() {
        return this.realParent;
    }

    public List<IssueCoAssignee> getCoAssignees() {
        return this.coAssignees;
    }

    public CustomField getClassOfService() {
        return this.classOfService;
    }

    public String getBlocked() {
        return this.blocked;
    }

    public String getLastBlockReason() {
        return this.lastBlockReason;
    }

    public Map<String, CustomField> getTShirtSizes() {
        return this.tShirtSizes;
    }

    public List<String> getComments() {
        return this.comments;
    }

    public List<String> getDependenciesIssuesKey() {
        return this.dependenciesIssuesKey;
    }

    public String getLinkedParentKey() {
        return this.linkedParentKey;
    }

    public Map<String, CustomField> getAdditionalEstimatedHours() {
        return this.additionalEstimatedHours;
    }

    public Map<String, CustomField> getRelease() {
        return this.release;
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public List<String> getComponents() {
        return this.components;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueMetadata that = (IssueMetadata) o;

        if (jiraProperties != null ? !jiraProperties.equals(that.jiraProperties) : that.jiraProperties != null)
            return false;
        if (log != null ? !log.equals(that.log) : that.log != null) return false;
        if (projectKey != null ? !projectKey.equals(that.projectKey) : that.projectKey != null) return false;
        if (assignee != null ? !assignee.equals(that.assignee) : that.assignee != null) return false;
        if (reporter != null ? !reporter.equals(that.reporter) : that.reporter != null) return false;
        if (realParent != null ? !realParent.equals(that.realParent) : that.realParent != null) return false;
        if (coAssignees != null ? !coAssignees.equals(that.coAssignees) : that.coAssignees != null) return false;
        if (classOfService != null ? !classOfService.equals(that.classOfService) : that.classOfService != null)
            return false;
        if (blocked != null ? !blocked.equals(that.blocked) : that.blocked != null) return false;
        if (lastBlockReason != null ? !lastBlockReason.equals(that.lastBlockReason) : that.lastBlockReason != null)
            return false;
        if (tShirtSizes != null ? !tShirtSizes.equals(that.tShirtSizes) : that.tShirtSizes != null) return false;
        if (comments != null ? !comments.equals(that.comments) : that.comments != null) return false;
        if (dependenciesIssuesKey != null ? !dependenciesIssuesKey.equals(that.dependenciesIssuesKey) : that.dependenciesIssuesKey != null)
            return false;
        if (linkedParentKey != null ? !linkedParentKey.equals(that.linkedParentKey) : that.linkedParentKey != null)
            return false;
        if (additionalEstimatedHours != null ? !additionalEstimatedHours.equals(that.additionalEstimatedHours) : that.additionalEstimatedHours != null)
            return false;
        if (release != null ? !release.equals(that.release) : that.release != null) return false;
        if (labels != null ? !labels.equals(that.labels) : that.labels != null) return false;
        return components != null ? components.equals(that.components) : that.components == null;
    }

    @Override
    public int hashCode() {
        int result = jiraProperties != null ? jiraProperties.hashCode() : 0;
        result = 31 * result + (log != null ? log.hashCode() : 0);
        result = 31 * result + (projectKey != null ? projectKey.hashCode() : 0);
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        result = 31 * result + (reporter != null ? reporter.hashCode() : 0);
        result = 31 * result + (realParent != null ? realParent.hashCode() : 0);
        result = 31 * result + (coAssignees != null ? coAssignees.hashCode() : 0);
        result = 31 * result + (classOfService != null ? classOfService.hashCode() : 0);
        result = 31 * result + (blocked != null ? blocked.hashCode() : 0);
        result = 31 * result + (lastBlockReason != null ? lastBlockReason.hashCode() : 0);
        result = 31 * result + (tShirtSizes != null ? tShirtSizes.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (dependenciesIssuesKey != null ? dependenciesIssuesKey.hashCode() : 0);
        result = 31 * result + (linkedParentKey != null ? linkedParentKey.hashCode() : 0);
        result = 31 * result + (additionalEstimatedHours != null ? additionalEstimatedHours.hashCode() : 0);
        result = 31 * result + (release != null ? release.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (components != null ? components.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IssueMetadata{" +
                "jiraProperties=" + jiraProperties +
                ", projectKey='" + projectKey + '\'' +
                ", assignee='" + assignee + '\'' +
                ", reporter='" + reporter + '\'' +
                ", realParent=" + realParent +
                ", coAssignees=" + coAssignees +
                ", classOfService=" + classOfService +
                ", blocked='" + blocked + '\'' +
                ", lastBlockReason='" + lastBlockReason + '\'' +
                ", tShirtSizes=" + tShirtSizes +
                ", comments=" + comments +
                ", dependenciesIssuesKey=" + dependenciesIssuesKey +
                ", linkedParentKey='" + linkedParentKey + '\'' +
                ", additionalEstimatedHours=" + additionalEstimatedHours +
                ", release=" + release +
                ", labels=" + labels +
                ", components=" + components +
                '}';
    }
}
