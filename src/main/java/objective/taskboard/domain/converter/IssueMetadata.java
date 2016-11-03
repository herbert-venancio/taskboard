package objective.taskboard.domain.converter;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2016 Objective Solutions
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

import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType.Direction;
import com.google.common.collect.Lists;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import objective.taskboard.jira.JiraProperties;

@Slf4j
@Data
public class IssueMetadata {
    
    private final Issue issue;
    private final JiraProperties jiraProperties;

    private final IssueParent realParent;
    private final List<IssueCoAssignee> coAssignees;
    private final String classOfService;
    private final String blocked;
    private final String lastBlockReason;
    private final String tShirtSize;
    private final List<String> comments;
    private final List<String> requiredIssuesKey;
    private final String linkedParentKey;
    
    public IssueMetadata(Issue issue, JiraProperties jiraProperties, List<String> parentIssueLinks) {
        this.issue = issue;
        this.jiraProperties = jiraProperties;
        this.realParent = extractRealParent();
        this.coAssignees = extractCoAssignees();
        this.classOfService = extractClassOfService();
        this.blocked = extractBlocked();
        this.lastBlockReason = extractLastBlockReason();
        this.tShirtSize = extractTShirtSize();
        this.comments = extractComments();
        this.requiredIssuesKey = extractRequiredIssues();
        this.linkedParentKey = extractLinkedParentKey(parentIssueLinks);
    }
    
    private List<IssueCoAssignee> extractCoAssignees() {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getCoAssignees().getId());
        List<IssueCoAssignee> coAssignees = Lists.newArrayList();

        if (field == null)
            return coAssignees;

        JSONArray value = (JSONArray) field.getValue();

        if (value == null)
            return coAssignees;
        
        for (int i = 0; i < value.length(); i++)
            try {
                String name = value.getJSONObject(i).getString("name");
                String avatarUrl = value.getJSONObject(i).getJSONObject("avatarUrls").getString("24x24");
                coAssignees.add(new IssueCoAssignee(name, avatarUrl));
            } catch (JSONException e) {
                log.error("Error extracting co-assignee from issue " + issue.getKey() + ": " + e.getMessage(), e);
            }
        return coAssignees;
    }
    
    private String extractClassOfService() {
        IssueField jiraField = issue.getField(jiraProperties.getCustomfield().getClassOfService().getId());

        if (jiraField == null)
            return "";

        JSONObject jsonField = (JSONObject) jiraField.getValue();

        try {
            return jsonField != null ? jsonField.getString("value") : "";
        } catch (JSONException e) {
            log.error("Error extracting class-of-service from issue " + issue.getKey() + ": " + e.getMessage(), e);
            return "";
        }
    }

    private String extractBlocked() {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getBlocked().getId());

        if (field == null)
            return "";

        JSONArray jsonArray = (JSONArray) field.getValue();
        
        if (jsonArray != null && jsonArray.length() > 0)
            try {
                return jsonArray.getJSONObject(0).getString("value");
            } catch (JSONException e) {
                log.error("Error extracting blocked from issue " + issue.getKey() + ": " + e.getMessage(), e);
            }
        return "";
    }

    private String extractTShirtSize() {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getTShirtSize().getId());

        if (field == null)
            return "";

        JSONObject json = (JSONObject) field.getValue();
        
        if (json != null)
            try {
                return json.getString("value");
            } catch (JSONException e) {
                log.error("Error extracting t-shirt-size from issue " + issue.getKey() + ": " + e.getMessage(), e);
            }
        return "";
    }
    
    private String extractLastBlockReason() {
        IssueField field = issue.getField(jiraProperties.getCustomfield().getLastBlockReason().getId());
        
        if (field == null || field.getValue() == null)
            return ""; 
        
        String lastBlockReason = field.getValue().toString();                 
        return lastBlockReason.length() > 200 ? lastBlockReason.substring(0, 200) + "..." : lastBlockReason; 
    }
    
    private List<String> extractComments() {
       return Lists.newArrayList(issue.getComments()).stream()
               .map(Comment::toString)
               .collect(Collectors.toList());
    }
    
    private List<String> extractRequiredIssues() {
        return Lists.newArrayList(issue.getIssueLinks()).stream()
                .filter(this::isRequiresLink)
                .map(link -> link.getTargetIssueKey())
                .collect(Collectors.toList());
    }
    
    private boolean isRequiresLink(final IssueLink link) {
        return link.getIssueLinkType().getName().equals(jiraProperties.getIssuelink().getRequirement().getName())
            && link.getIssueLinkType().getDirection() == Direction.OUTBOUND;
    }
    
    private IssueParent extractRealParent() {
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
            log.error("Error extracting parent from issue " + issue.getKey() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    private String extractLinkedParentKey(List<String> parentIssueLinks) {
        if (issue.getIssueLinks() == null)
            return null;
        
        List<IssueLink> links = Lists.newArrayList(issue.getIssueLinks()).stream()
                .filter(l -> parentIssueLinks.contains(l.getIssueLinkType().getDescription()))
                .collect(Collectors.toList());
        
        if (links.isEmpty())
            return null;
        
        return links.get(0).getTargetIssueKey();
    }
    
    public String getParentKey() {
        if (getRealParent() != null)
            return getRealParent().getKey();
        if (getLinkedParentKey() != null)
            return getLinkedParentKey();
        return "";
    }

    @Data
    static class IssueParent {
        private final String key;
        private final long typeId;
        private final String typeIconUrl;
        
        public IssueParent(String key, long issueTypeId, String issueTypeIconUrl) {
            this.key = key;
            this.typeId = issueTypeId;
            this.typeIconUrl = issueTypeIconUrl;
        }
    }
    
    @Data
    static class IssueCoAssignee {
        private final String name;
        private final String avatarUrl;

        public IssueCoAssignee(String name, String avatarUrl) {
            this.name = name;
            this.avatarUrl = avatarUrl;
        }
    }

}
