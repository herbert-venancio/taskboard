package objective.taskboard.domain.converter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static objective.taskboard.domain.converter.FieldValueExtractor.UNSUPPORTED_EXTRACTION_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import objective.taskboard.data.Issue;
import objective.taskboard.database.IssuePriorityService;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.jira.FieldMetadataService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraCommentDto;
import objective.taskboard.jira.client.JiraCommentResultSetDto;
import objective.taskboard.jira.client.JiraFieldDataDto;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.client.JiraPriorityDto;
import objective.taskboard.jira.client.JiraProjectDto;
import objective.taskboard.jira.client.JiraStatusDto;
import objective.taskboard.jira.client.JiraUserDto;
import objective.taskboard.jira.client.JiraWorklogResultSetDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.filter.LaneService;
import objective.taskboard.repository.ParentIssueLinkRepository;
import objective.taskboard.utils.IOUtilities;


@RunWith(MockitoJUnitRunner.class)
public class JiraIssueToIssueConverterTest {

    public static final String COST_CENTER_FIELD_ID = "customfield_10390";

    private static final String PARENT_ISSUE_KEY = "ISSUE-1";
    private static final String ISSUE_KEY = "ISSUE-2";
    private static final String TYPE_ICON_URI = "iconURI";
    private static final String CLASS_OF_SERVICE_EXPEDITE = "Expedite";
    private static final String CLASS_OF_SERVICE_STANDARD = "Standard";

    private static final String JSON_PARENT = "{key:'%s'}";
    private static final String JSON_CLASS_OF_SERVICE = "{id:1, value:'%s'}";
    private static final String JSON_COST_CENTER = "[{\"id\": \"13080\",\"value\": \"Taskboard\"}]";
    private static final String JSON_SHIRT_SIZE_SMALL = "{\"id\": \"12641\",\"value\": \"S\"}";

    private static final String PARENT_ID = "parent";
    private static final String CLASS_OF_SERVICE_ID = "classOfServiceId";
    private static final String CO_ASSIGNEES_ID = "coAssigneesId";
    private static final String BLOCKED_ID = "blockedId";
    private static final String SHOULD_BLOCK_ALL_SUBTASKS_ID = "shouldBlockAllSubTasksId";
    private static final String LAST_BLOCK_REASON_ID = "lastBlockReasonId";
    private static final String ADDITIONAL_ESTIMATED_HOURS_ID = "additionalEstimatedHoursId";
    private static final String RELEASE_ID = "releaseId";
    private static final String ADDITIONAL_ESTIMATED_HOURS_FIELD_ID = "customfield_11450";

    @InjectMocks
    private JiraIssueToIssueConverter subject;

    @Mock
    private ParentIssueLinkRepository parentIssueLinkRepository;
    @Mock
    private JiraIssueDto issue;
    @Mock
    private JiraIssueDto parent;
    @Mock
    private JiraProjectDto project;
    @Mock
    private JiraPriorityDto priority;
    @Mock
    private JiraIssueTypeDto issueType;
    @Mock
    private JiraStatusDto status;
    @Mock
    private JiraUserDto assignee;
    @Mock
    private JiraProperties jiraProperties;
    @Mock
    private JiraProperties.CustomField customField;
    @Mock
    private JiraProperties.CustomField.CustomFieldDetails coAssigneesDetails;
    @Mock
    private JiraProperties.CustomField.Blocked blocked;
    @Mock
    private JiraProperties.CustomField.ShouldBlockAllSubtasks shouldBlockAllSubtasks;
    @Mock
    private JiraProperties.CustomField.TShirtSize tShirtSize;
    @Mock
    private JiraProperties.CustomField.CustomFieldDetails lastBlockReason;
    @Mock
    private JiraProperties.CustomField.CustomFieldDetails additionalEstimatedHours;
    @Mock
    private JiraProperties.CustomField.CustomFieldDetails release;
    @Mock
    private JiraProperties.CustomField.ClassOfServiceDetails classOfServiceDetails;
    @Mock
    private objective.taskboard.jira.properties.JiraProperties.IssueLink issueLinkProperty;
    @Mock
    private IssueTeamService issueTeamService;
    @Mock
    private StartDateStepService startDateStepService;
    @Mock
    private IssueColorService issueColorService;
    @Mock
    private JiraService jiraService;
    @Mock
    private IssuePriorityService priorityService;
    @Mock
    private MetadataService metadataService;
    @Mock
    private JiraCommentDto comment;
    @Mock
    private LaneService laneService;
    @Mock
    private CardVisibilityEvalService cardVisibilityEvalService;
    @Mock
    private FieldMetadataService fieldMetadataService;
    @Mock
    private JiraCommentResultSetDto commentsDto;
    private List<JiraCommentDto> comments = new ArrayList<>();

    @Before
    public void before() throws Exception {
        when(parentIssueLinkRepository.findAll()).thenReturn(asList());

        when(classOfServiceDetails.getId()).thenReturn(CLASS_OF_SERVICE_ID);
        when(customField.getClassOfService()).thenReturn(classOfServiceDetails);

        when(coAssigneesDetails.getId()).thenReturn(CO_ASSIGNEES_ID);
        when(customField.getCoAssignees()).thenReturn(coAssigneesDetails);

        when(blocked.getId()).thenReturn(BLOCKED_ID);
        when(customField.getBlocked()).thenReturn(blocked);

        when(shouldBlockAllSubtasks.getId()).thenReturn(SHOULD_BLOCK_ALL_SUBTASKS_ID);
        when(customField.getShouldBlockAllSubtasks()).thenReturn(shouldBlockAllSubtasks);

        when(tShirtSize.getIds()).thenReturn(asList());
        when(customField.getTShirtSize()).thenReturn(tShirtSize);

        when(lastBlockReason.getId()).thenReturn(LAST_BLOCK_REASON_ID);
        when(customField.getLastBlockReason()).thenReturn(lastBlockReason);

        when(additionalEstimatedHours.getId()).thenReturn(ADDITIONAL_ESTIMATED_HOURS_ID);
        when(customField.getAdditionalEstimatedHours()).thenReturn(additionalEstimatedHours);

        when(release.getId()).thenReturn(RELEASE_ID);
        when(customField.getRelease()).thenReturn(release);

        when(customField.getAssignedTeams()).thenReturn(mock(JiraProperties.CustomField.CustomFieldDetails.class));
        when(customField.getCoAssignees()).thenReturn(mock(JiraProperties.CustomField.CustomFieldDetails.class));
        
        when(jiraProperties.getCustomfield()).thenReturn(customField);
        when(jiraProperties.getExtraFields()).thenReturn(new JiraProperties.ExtraFields());

        mockIssue(issue, ISSUE_KEY);

        when(priorityService.determinePriority(any())).thenReturn(0L);

        when(cardVisibilityEvalService.calculateVisibleUntil(any(), any(), any())).thenReturn(Optional.empty());

        JiraIssueTypeDto metadataIssueType = new JiraIssueTypeDto(1L, null, false, URI.create(TYPE_ICON_URI));
        when(metadataService.getIssueTypeById(any())).thenReturn(metadataIssueType);
    }

    @Test
    public void simpleIssueConvert() {
        when(project.getKey()).thenReturn("ISSUE");
        when(project.getName()).thenReturn("PROJECT");
        when(issueType.getId()).thenReturn(1L);
        when(issue.getSummary()).thenReturn("Summary");
        when(status.getId()).thenReturn(1L);
        when(startDateStepService.get(issue)).thenReturn(1L);
        when(assignee.getAvatarUri(anyString())).thenReturn(URI.create("assigneeAvatarURI"));
        when(assignee.getName()).thenReturn("assignee");
        when(issue.getAssignee()).thenReturn(assignee);
        when(priority.getId()).thenReturn(1L);
        when(issue.getDueDate()).thenReturn(new DateTime(0));
        when(issue.getDescription()).thenReturn("Description");

        Issue.CardTeam cardTeam = new Issue.CardTeam();
        cardTeam.name = "team";
        when(issueTeamService.resolveTeams(any())).thenReturn(Sets.newSet(cardTeam));

        objective.taskboard.data.Issue converted = subject.convertSingleIssue(issue, buildProvider());

        assertEquals("Issue key", ISSUE_KEY, converted.getIssueKey());
        assertEquals("Project key", "ISSUE", converted.getProjectKey());
        assertEquals("Project name", "PROJECT", converted.getProject());
        assertEquals("Issue type id", 1L, converted.getType());
        assertEquals("Issue type icon URI", TYPE_ICON_URI, converted.getTypeIconUri());
        assertEquals("Summary", "Summary", converted.getSummary());
        assertEquals("Status id", 1L, converted.getStatus());
        assertEquals("Start date step millis", 1L, converted.getStartDateStepMillis());
        assertEquals("Parent key", "", converted.getParent());
        assertEquals("Parent type id", 0L, converted.getParentType());
        assertEquals("Parent type icon URI", "", converted.getParentTypeIconUri());
        assertEquals("Dependencies quantity", 0, converted.getDependencies().size());
        assertEquals("Co-assignees", 0, converted.getCoAssignees().size());
        assertEquals("Assignee name", "assignee", converted.getAssignee().name);
        assertEquals("Priority", 1L, converted.getPriority());
        assertEquals("Due date", new Date(0), converted.getDueDate());
        assertEquals("Creation date millis", new DateTime(0).getMillis(), converted.getCreated());
        assertEquals("Description", "Description", converted.getDescription());
        assertEquals("Teams", 1, converted.getTeams().size());
        assertEquals("Team name", "team", converted.getTeams().iterator().next().name);
        assertEquals("Comments", 1, converted.getComments().size());
        assertFalse("Blocked", converted.isBlocked());
        assertEquals("Last block reason", "", converted.getLastBlockReason());
        assertEquals("Class of Service", null, converted.getLocalClassOfServiceCustomField());
        assertEquals("Color", null, converted.getColor());
        assertEquals("Priority order", 0L, converted.getPriorityOrder());
    }

    private ParentProvider buildProvider() {
        return parentKey -> {
           throw new RuntimeException("Should not ask for parent during test: " + parentKey);
        };
    }

    @Test
    public void issueWithParentConvert() throws Exception {
        when(classOfServiceDetails.getDefaultValue()).thenReturn(CLASS_OF_SERVICE_STANDARD);
        mockIssue(parent, PARENT_ISSUE_KEY, 1L);

        mockIssueField(parent, CLASS_OF_SERVICE_ID, format(JSON_CLASS_OF_SERVICE, CLASS_OF_SERVICE_EXPEDITE));
        mockIssueField(parent, RELEASE_ID, "{id: 100}");
        mockIssueField(issue, PARENT_ID, format(JSON_PARENT, PARENT_ISSUE_KEY));

        objective.taskboard.data.Issue parentIssue = subject.convertSingleIssue(parent, buildProvider());
        objective.taskboard.data.Issue childIssue = subject.convertSingleIssue(issue, parentKey -> Optional.of(parentIssue));
        childIssue.setParentCard(parentIssue);
        List<objective.taskboard.data.Issue> issuesConverted = Arrays.asList(parentIssue, childIssue);

        assertEquals("Issues converted quantity", 2, issuesConverted.size());
        objective.taskboard.data.Issue converted = issuesConverted.stream()
                .filter(i -> i.getIssueKey().equals(ISSUE_KEY))
                .findFirst().orElse(null);
        assertIssueWithParent(converted);
        assertEquals("Class of service", CLASS_OF_SERVICE_EXPEDITE, converted.getClassOfServiceValue());
        assertEquals("Release", "100", converted.getReleaseId());
    }


    @Test
    public void issueWithComment() throws Exception {
        assertEquals( 1, comments.size());
        assertEquals("Comment", "Comment", comments.get(0).body);
    }


    @Test
    public void updateIssueConverted() throws JSONException {
        mockIssueField(issue, CLASS_OF_SERVICE_ID, format(JSON_CLASS_OF_SERVICE, CLASS_OF_SERVICE_EXPEDITE));

        objective.taskboard.data.Issue converted = subject.convertSingleIssue(issue, buildProvider());

        assertEquals("Class of service", converted.getClassOfServiceValue(), CLASS_OF_SERVICE_EXPEDITE);

        mockIssueField(issue, CLASS_OF_SERVICE_ID, format(JSON_CLASS_OF_SERVICE, CLASS_OF_SERVICE_STANDARD));

        objective.taskboard.data.Issue issueUpdated = subject.convertSingleIssue(issue, buildProvider());
        assertNotNull("Issue updated converted", issueUpdated);
        assertEquals("Class of service", issueUpdated.getClassOfServiceValue(), CLASS_OF_SERVICE_STANDARD);
    }


    @Test
    public void whenIssuesWithMultipleNestedLevelsAreReturned_makeSureValuesDependingOnParentsAreCorrectlyConverted() throws Exception {
        JiraIssueDto A = mock(JiraIssueDto.class);
        JiraIssueDto B = mock(JiraIssueDto.class);
        JiraIssueDto C = mock(JiraIssueDto.class);
        mockIssue(A,"KEY-1");
        mockIssueField(A, PARENT_ID, format(JSON_PARENT, "PARENT-1"));
        mockIssueField(A, CLASS_OF_SERVICE_ID, format(JSON_CLASS_OF_SERVICE, CLASS_OF_SERVICE_EXPEDITE));
        mockIssueField(B, PARENT_ID, format(JSON_PARENT, "PARENT-2"));
        mockIssue(B,"PARENT-1");
        mockIssue(C,"PARENT-2");
        
        
        Map<String, Issue> issueByKey = new LinkedHashMap<>();
        ParentProvider provider = parentKey -> {
            objective.taskboard.data.Issue issue = issueByKey.get(parentKey);
            if (issue == null)
                return Optional.empty();
            
            return Optional.of(issue);
        };
        try {
            subject.convertSingleIssue(A, provider);
            fail("Should throw an IncompleteIssueException");
        }catch(IncompleteIssueException e) {
            assertEquals("PARENT-1", e.getMissingParentKey());
        }
        try {
            subject.convertSingleIssue(B, provider);
            fail("Should throw an IncompleteIssueException");
        }catch(IncompleteIssueException e) {
            assertEquals("PARENT-2", e.getMissingParentKey());
        }
        issueByKey.put("PARENT-2", subject.convertSingleIssue(C, provider));
        issueByKey.put("PARENT-1", subject.convertSingleIssue(B, provider));
        subject.convertSingleIssue(A, provider);
    }


    @Test
    public void givenExtraFieldsIsConfigured_whenConvert_thenExtractExtraFieldsHasValues() throws JSONException {
        // given
        setupExtraFields(COST_CENTER_FIELD_ID);
        mockIssueField(issue, COST_CENTER_FIELD_ID, JSON_COST_CENTER);

        // when
        Issue converted = subject.convertSingleIssue(issue, buildProvider());

        // then
        assertThat(converted.getExtraFields()).containsEntry(COST_CENTER_FIELD_ID, "Taskboard");
    }


    @Test
    public void givenExtraFieldsIsConfiguredAndValueIsEmpty_whenConvert_thenExtractedAsNull() throws JSONException {
        // given
        setupExtraFields(COST_CENTER_FIELD_ID);
        mockIssueField(issue, COST_CENTER_FIELD_ID, null);

        // when
        Issue converted = subject.convertSingleIssue(issue, buildProvider());

        // then
        assertThat(converted.getExtraFields()).isEmpty();
    }


    @Test
    public void givenExtraFieldsIsConfiguredAndIssueDoNotHaveField_whenConvert_thenExtractedAsNull() {
        // given
        setupExtraFields(COST_CENTER_FIELD_ID);

        // when
        Issue converted = subject.convertSingleIssue(issue, buildProvider());

        // then
        assertThat(converted.getExtraFields()).isEmpty();
    }


    @Test
    public void givenExtraFieldsNotSupported_whenConvert_thenExtractedAsUnsupportedValueString() throws JSONException {
        // given
        setupExtraFields(ADDITIONAL_ESTIMATED_HOURS_FIELD_ID);
        mockIssueField(issue, ADDITIONAL_ESTIMATED_HOURS_FIELD_ID, JSON_SHIRT_SIZE_SMALL);

        // when
        Issue converted = subject.convertSingleIssue(issue, buildProvider());

        // then
        assertThat(converted.getExtraFields()).containsEntry(ADDITIONAL_ESTIMATED_HOURS_FIELD_ID, UNSUPPORTED_EXTRACTION_VALUE);
    }

    private void setupExtraFields(String... extraFieldIds) {
        JiraProperties.ExtraFields extraFields = new JiraProperties.ExtraFields();
        extraFields.setFieldIds(asList(extraFieldIds));
        when(jiraProperties.getExtraFields()).thenReturn(extraFields);

        try {
            List<JiraFieldDataDto> allFields = new ObjectMapper()
                    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(IOUtilities.resourceToString("objective-jira-teste/field.response.json"), new TypeReference<List<JiraFieldDataDto>>() {});
            when(fieldMetadataService.getFieldsMetadata()).thenReturn(allFields);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void mockIssue(JiraIssueDto issue, String issueKey) throws Exception {
        when(issue.getKey()).thenReturn(issueKey);
        when(issue.getProject()).thenReturn(project);
        when(issue.getIssueType()).thenReturn(issueType);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getCreationDate()).thenReturn(new DateTime(0));
        when(issue.getPriority()).thenReturn(priority);
        when(issue.getWorklogs()).thenReturn(new JiraWorklogResultSetDto());
        when(issue.getComments()).thenReturn(commentsDto);

        URI uri;

        try{
            uri = new URI("http://example");
        } catch (URISyntaxException ex){
            uri = null; //NOSONAR
        }

        commentsDto.comments = comments;
        commentsDto.comments.add(comment);
        JiraUserDto user = mock(JiraUserDto.class);
        when(user.getDisplayName()).thenReturn("user");
        when(user.getAvatarUri(anyString())).thenReturn(uri);
        comment.author = user;
        comment.body = "Comment";
        comment.created = new Date();
    }

    private void mockIssue(JiraIssueDto issue, String issueKey, long issueTypeId) throws Exception {
        mockIssue(issue, issueKey);
        JiraIssueTypeDto issueType = mock(JiraIssueTypeDto.class);
        when(issueType.getId()).thenReturn(issueTypeId);
        when(issue.getIssueType()).thenReturn(issueType);
    }

    private void mockIssueField(JiraIssueDto issue, String fieldId, String json) throws JSONException {
        Object fieldValue;
        if(json == null)
            fieldValue = null;
        else if (json.startsWith("{"))
            fieldValue = new JSONObject(json);
        else if (json.startsWith("["))
            fieldValue = new JSONArray(json);
        else
            fieldValue = json;
        when(issue.getField(eq(fieldId))).thenReturn(fieldValue);
    }

    private void assertIssueWithParent(objective.taskboard.data.Issue converted) {
        assertNotNull("Issue converted", converted);
        assertEquals("Issue key", ISSUE_KEY, converted.getIssueKey());
        assertEquals("Parent key", PARENT_ISSUE_KEY, converted.getParent());
        assertEquals("Parent type id", 1L, converted.getParentType());
        assertEquals("Parent type icon URI", TYPE_ICON_URI, converted.getParentTypeIconUri());
    }

}
