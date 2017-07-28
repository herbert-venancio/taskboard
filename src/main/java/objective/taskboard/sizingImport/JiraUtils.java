package objective.taskboard.sizingImport;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;

import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;

@Component
class JiraUtils {

    private final JiraEndpointAsLoggedInUser jiraEndpoint;
    private final JiraProperties jiraProperties;
    private final MetadataService metadataService;
    
    @Autowired
    public JiraUtils(JiraEndpointAsLoggedInUser jiraEndpoint, JiraProperties jiraProperties, MetadataService metadataService) {
        this.jiraEndpoint = jiraEndpoint;
        this.jiraProperties = jiraProperties;
        this.metadataService = metadataService;
    }

    public BasicIssue createIssue(IssueInputBuilder featureBuilder) {
        return jiraEndpoint.executeRequest(client -> client.getIssueClient().createIssue(featureBuilder.build()));
    }

    public void linkToDemand(String demandKey, String issueKey) {
        IssuelinksType demandLink = getDemandLink();
        jiraEndpoint.executeRequest(client -> client.getIssueClient().linkIssue(new LinkIssuesInput(demandKey, issueKey, demandLink.getName())));
    }

    public IssuelinksType getDemandLink() {
        return metadataService.getIssueLinksMetadata().get(jiraProperties.getIssuelink().getDemandId());
    }

    public CimIssueType getFeatureMetadata(String projectKey) {
        long featureTypeId = jiraProperties.getIssuetype().getDefaultFeature().getId();
        return getIssueTypeMetadata(projectKey, featureTypeId);
    }

    public Project getProject(String projectKey) {
        return jiraEndpoint.executeRequest(client -> client.getProjectClient().getProject(projectKey));
    }

    public CustomFieldOption getCustomFieldOption(CimIssueType issueMetadata, String fieldId, String optionValue) {
        CimFieldInfo fieldMetadata = issueMetadata.getFields().get(fieldId);
        if (fieldMetadata == null)
            throw new RuntimeException("Custom field not found :" + fieldId);

        if (StringUtils.isEmpty(optionValue))
            return null;
        
        for (Object allowedValue : fieldMetadata.getAllowedValues()) {
            if (allowedValue instanceof CustomFieldOption) {
                CustomFieldOption option = (CustomFieldOption) allowedValue;

                if (option.getValue() != null && option.getValue().equals(optionValue))
                    return option;
            }
        }

        throw new RuntimeException("Custom field option not found: " + optionValue);
    }

    public Version createVersion(String projectKey, String name) {
        VersionInput versionInput = new VersionInput(projectKey, name, null, null, false, false);
        return jiraEndpoint.executeRequest(client -> client.getVersionRestClient().createVersion(versionInput));
    }

    public boolean hasPermitionToCreateVersion(String project) {
        Permissions permissions = jiraEndpoint.executeRequest(client -> 
            client.getMyPermissionsRestClient().getMyPermissions(MyPermissionsInput.withProject(project)));

        return permissions.havePermission("PROJECT_ADMIN");
    }

    private CimIssueType getIssueTypeMetadata(String projectKey, Long issueTypeId) {
        GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder()
                .withExpandedIssueTypesFields()
                .withProjectKeys(projectKey)
                .withIssueTypeIds(issueTypeId)
                .build();
        
        Iterable<CimProject> projects = jiraEndpoint
                .executeRequest(client -> client.getIssueClient().getCreateIssueMetadata(options));
        
        for (CimProject projectMetaData : projects) {
            for (CimIssueType issueTypeMetaData : projectMetaData.getIssueTypes()) {
                if (issueTypeMetaData.getId().equals(issueTypeId))
                    return issueTypeMetaData;
            }
        }

        throw new RuntimeException("Issue type not found: " + issueTypeId);
    }

    public Issue getIssue(String jiraKey) {
        Issue issue = jiraEndpoint.executeRequest(client -> client.getIssueClient().getIssue(jiraKey));
        return issue;
    }

}
