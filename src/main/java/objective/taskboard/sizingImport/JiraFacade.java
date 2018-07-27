package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.utils.StreamUtils.instancesOf;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraLinkDto;
import objective.taskboard.jira.client.JiraLinkTypeDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.utils.ObjectUtils;

@Component
class JiraFacade {

    private final JiraEndpointAsLoggedInUser jiraEndpoint;
    private final JiraProperties jiraProperties;
    private final MetadataService metadataService;
    
    @Autowired
    public JiraFacade(JiraEndpointAsLoggedInUser jiraEndpoint, JiraProperties jiraProperties, MetadataService metadataService) {
        this.jiraEndpoint = jiraEndpoint;
        this.jiraProperties = jiraProperties;
        this.metadataService = metadataService;
    }

    private JiraIssue createIssue(JiraIssue.Input input) {
        return jiraEndpoint.request(JiraIssue.Service.class).create(input);
    }

    public JiraIssue createDemand(String projectKey, String summary, Version release) {
        long demandTypeId = jiraProperties.getIssuetype().getDemand().getId();

        JiraIssue.Input demandInput = JiraIssue.Input.builder(jiraProperties, projectKey, demandTypeId)
                .summary(summary)
                .release(release)
                .build();
        
        return createIssue(demandInput);
    }

    public JiraIssue createFeature(String projectKey, String demandKey, Long featureTypeId, String summary, Version release, Collection<IssueFieldValue> fieldValues) {
        JiraIssue.InputBuilder<?> builder = JiraIssue.Input.builder(jiraProperties, projectKey, featureTypeId)
                .summary(summary)
                .release(release);

        for (IssueFieldValue fv : fieldValues)
            fv.setFieldValue(builder);

        JiraIssue issue = createIssue(builder.build());
        
        JiraLinkTypeDto demandLink = getDemandLink();
        jiraEndpoint.request(JiraIssue.Service.class).linkIssue(JiraIssue.LinkInput.builder()
                .type(demandLink.name)
                .from(demandKey)
                .to(issue.key)
                .build());
        
        return issue;
    }
   
    public Optional<String> getDemandKeyGivenFeature(JiraIssueDto feature) {
        String demandIssueLinkName = getDemandLink().name;

        return feature.getIssueLinks().stream()
                .filter(link -> link.getIssueLinkType().getName().equals(demandIssueLinkName))
                .map(JiraLinkDto::getTargetIssueKey)
                .findFirst();
    }

    
    private JiraLinkTypeDto getDemandLink() {
        return metadataService.getIssueLinksMetadata().get(jiraProperties.getIssuelink().getDemandId().toString());
    }

    public List<JiraCreateIssue.IssueTypeMetadata> requestFeatureTypes(String projectKey) {
        List<Long> featureTypeIds = jiraProperties.getIssuetype().getFeatures().stream()
                .map(IssueTypeDetails::getId)
                .collect(toList());

        return requestCreateIssueMetadata(projectKey, featureTypeIds);
    }

    public List<String> getSizingFieldIds() {
        return jiraProperties.getCustomfield().getTShirtSize().getIds();
    }
    
    public JiraProject getProject(String projectKey) {
        return jiraEndpoint.request(JiraProject.Service.class).get(projectKey);
    }

    public Version createVersion(String projectKey, String name) {
        Version.Request versionInput = new Version.Request(projectKey, name);
        return jiraEndpoint.request(Version.Service.class).create(versionInput);
    }

    private List<JiraCreateIssue.IssueTypeMetadata> requestCreateIssueMetadata(String projectKey, List<Long> issueTypeIds) {
        return jiraEndpoint.request(JiraCreateIssue.Service.class)
                .getByProjectKey(projectKey, issueTypeIds)
                .projects.stream()
                .flatMap(p -> p.issueTypes.stream())
                .collect(toList());
    }

    public JiraIssueDto getIssue(String issueKey) {
        return jiraEndpoint.request(JiraIssueDto.Service.class).get(issueKey);
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
        
        protected abstract void setFieldValue(JiraIssue.InputBuilder<?> builder);

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
        protected void setFieldValue(JiraIssue.InputBuilder<?> builder) {
            builder.field(getFieldId(), value);
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
        private final JiraCreateIssue.IssueTypeMetadata issueMetadata;

        public IssueCustomFieldOptionValue(String fieldId, String optionValue, JiraCreateIssue.IssueTypeMetadata issueMetadata) {
            super(fieldId);
            this.optionValue = optionValue;
            this.issueMetadata = issueMetadata;
        }

        @Override
        protected void setFieldValue(JiraIssue.InputBuilder<?> builder) {
            JiraCreateIssue.CustomFieldOption option = getOption();
            builder.field(getFieldId(), option);
        }

        private JiraCreateIssue.CustomFieldOption getOption() {
            JiraCreateIssue.FieldInfoMetadata fieldMetadata = issueMetadata.getField(getFieldId());
            if (fieldMetadata == null)
                throw new RuntimeException("Custom field not found :" + getFieldId());

            if (StringUtils.isEmpty(optionValue))
                return null;

            Optional<JiraCreateIssue.CustomFieldOption> option = fieldMetadata.getAllowedValues().stream()
                    .flatMap(instancesOf(JiraCreateIssue.CustomFieldOption.class))
                    .filter(o -> Objects.equals(o.value, optionValue))
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
