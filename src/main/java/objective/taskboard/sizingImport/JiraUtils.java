package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.utils.StreamUtils.instancesOf;
import static objective.taskboard.utils.StreamUtils.streamOf;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;

import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.utils.ObjectUtils;

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

    private BasicIssue createIssue(IssueInput input) {
        return jiraEndpoint.executeRequest(client -> client.getIssueClient().createIssue(input));
    }

    public BasicIssue createDemand(String projectKey, String summary, Version release) {
        long demandTypeId = jiraProperties.getIssuetype().getDemand().getId();
        String releaseFieldId = jiraProperties.getCustomfield().getRelease().getId();

        IssueInput demandInput = new IssueInputBuilder(projectKey, demandTypeId)
                .setSummary(summary)
                .setFieldValue(releaseFieldId, release)
                .build();
        
        return createIssue(demandInput);
    }

    public BasicIssue createFeature(String projectKey, String demandKey, String summary, Version release, Collection<IssueFieldValue> fieldValues) {
        long featureTypeId = jiraProperties.getIssuetype().getDefaultFeature().getId();
        String customFieldRelease = jiraProperties.getCustomfield().getRelease().getId();
        
        IssueInputBuilder builder = new IssueInputBuilder(projectKey, featureTypeId)
                .setSummary(summary)
                .setFieldValue(customFieldRelease, release);

        for (IssueFieldValue fv : fieldValues)
            fv.setFieldValue(builder);

        BasicIssue issue = createIssue(builder.build());
        
        IssuelinksType demandLink = getDemandLink();
        jiraEndpoint.executeRequest(client -> client.getIssueClient().linkIssue(new LinkIssuesInput(demandKey, issue.getKey(), demandLink.getName())));
        
        return issue;
    }
   
    public Optional<String> getDemandKeyGivenFeature(Issue feature) {
        String demandIssueLinkName = getDemandLink().getName();

        return StreamSupport.stream(feature.getIssueLinks().spliterator(), false)
                .filter(link -> link.getIssueLinkType().getName().equals(demandIssueLinkName))
                .map(IssueLink::getTargetIssueKey)
                .findFirst();
    }

    
    private IssuelinksType getDemandLink() {
        return metadataService.getIssueLinksMetadata().get(jiraProperties.getIssuelink().getDemandId().toString());
    }

    public CimIssueType requestFeatureCreateIssueMetadata(String projectKey) {
        long featureTypeId = jiraProperties.getIssuetype().getDefaultFeature().getId();
        return requestCreateIssueMetadata(projectKey, featureTypeId);
    }

    public List<CimFieldInfo> getSizingFields(CimIssueType createIssueMetadata) {
        List<String> configuredSizingFieldsId = jiraProperties.getCustomfield().getTShirtSize().getIds();
        Collection<CimFieldInfo> issueFields = createIssueMetadata.getFields().values();
        
        return issueFields.stream()
                .filter(f -> configuredSizingFieldsId.contains(f.getId()))
                .collect(toList());
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

    public boolean isAdminOfProject(String projectKey) {
        Permissions permissions = jiraEndpoint.executeRequest(client -> 
            client.getMyPermissionsRestClient().getMyPermissions(MyPermissionsInput.withProject(projectKey)));

        return permissions.havePermission("PROJECT_ADMIN");
    }

    private CimIssueType requestCreateIssueMetadata(String projectKey, Long issueTypeId) {
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

    public Issue getIssue(String issueKey) {
        return jiraEndpoint.executeRequest(client -> client.getIssueClient().getIssue(issueKey));
    }

    public String getJiraUrl() {
        return jiraProperties.getUrl();
    }
    
    
    abstract static class IssueFieldValue {
        private final String fieldId;

        public IssueFieldValue(String fieldId) {
            this.fieldId = fieldId;
        }
        
        protected String getFieldId() {
            return fieldId;
        }
        
        protected abstract void setFieldValue(IssueInputBuilder builder);

        @Override
        public int hashCode() {
            return new HashCodeBuilder(31, 17).append(fieldId).build();
        }

        @Override
        public boolean equals(Object obj) {
            return ObjectUtils.equals(this, obj, other -> fieldId.equals(other.fieldId));
        }
    }

    static class IssueFieldObjectValue extends IssueFieldValue {
        private final Object value;

        public IssueFieldObjectValue(String fieldId, Object value) {
            super(fieldId);
            this.value = value;
        }

        @Override
        protected void setFieldValue(IssueInputBuilder builder) {
            builder.setFieldValue(getFieldId(), value);
        }
        
        @Override
        public String toString() {
            return "IssueFieldObjectValue {fieldId=" + getFieldId() + ",value=" + value + "}";
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(33, 17)
                    .appendSuper(super.hashCode())
                    .append(value)
                    .build();
        }

        @Override
        public boolean equals(Object obj) {
            return ObjectUtils.equals(this, obj, other -> super.equals(other) && value.equals(other.value));
        }
    }
    
    static class IssueCustomFieldOptionValue extends IssueFieldValue {
        private final String optionValue;
        private final CimIssueType issueMetadata;

        public IssueCustomFieldOptionValue(String fieldId, String optionValue, CimIssueType issueMetadata) {
            super(fieldId);
            this.optionValue = optionValue;
            this.issueMetadata = issueMetadata;
        }

        @Override
        protected void setFieldValue(IssueInputBuilder builder) {
            CustomFieldOption option = getOption();
            builder.setFieldValue(getFieldId(), option);
        }

        private CustomFieldOption getOption() {
            CimFieldInfo fieldMetadata = issueMetadata.getFields().get(getFieldId());
            if (fieldMetadata == null)
                throw new RuntimeException("Custom field not found :" + getFieldId());

            if (StringUtils.isEmpty(optionValue))
                return null;

            Optional<CustomFieldOption> option = streamOf(fieldMetadata.getAllowedValues())
                    .flatMap(instancesOf(CustomFieldOption.class))
                    .filter(o -> Objects.equals(o.getValue(), optionValue))
                    .findFirst();

            return option.orElseThrow(() -> new RuntimeException("Custom field option not found: " + optionValue));
        }
        
        @Override
        public String toString() {
            return "IssueCustomFieldOptionValue {fieldId=" + getFieldId() + ",optionValue=" + optionValue + "}";
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(35, 17)
                    .appendSuper(super.hashCode())
                    .append(optionValue)
                    .build();
        }

        @Override
        public boolean equals(Object obj) {
            return ObjectUtils.equals(this, obj, other -> super.equals(other) && optionValue.equals(other.optionValue));
        }
    }
}
