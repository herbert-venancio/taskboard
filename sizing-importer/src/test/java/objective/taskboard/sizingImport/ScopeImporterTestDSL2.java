package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toMap;
import static objective.taskboard.sizingImport.SizingImportJiraMock.NOT_MOCKED_EXCEPTION_ANSWER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.OptionalAssert;

import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraIssueDtoSearch;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.properties.JiraProperties;

public class ScopeImporterTestDSL2 {

    private final SizingImportJiraMock jiraMock = new SizingImportJiraMock();
    private final SizingImportConfig importConfig = new SizingImportConfig();
    public final JiraProperties jiraProperties = new JiraProperties();
    private final MetadataService metadataService = mock(MetadataService.class);
    private final JiraEndpointAsLoggedInUser jiraEndpoint = mock(JiraEndpointAsLoggedInUser.class);
    private final JiraFacade jiraFacade = new JiraFacade(jiraEndpoint, jiraProperties, metadataService);
    private final SizingSheetImporterNotifier importerNotifier = new SizingSheetImporterNotifier();

    public ScopeImporterTestDSL2() {
        JiraProperties.IssueType issueType = new JiraProperties.IssueType();
        issueType.setFeatures(new ArrayList<>());
        issueType.setSubtasks(new ArrayList<>());
        jiraProperties.setIssuetype(issueType);

        JiraProperties.IssueLink issueLink = new JiraProperties.IssueLink();
        issueLink.setDemandId(Integer.parseInt(jiraMock.findLinkTypeByNameOrThrow("Demand").id));
        jiraProperties.setIssuelink(issueLink);

        JiraProperties.CustomField.TShirtSize tshirtSize = new JiraProperties.CustomField.TShirtSize();
        JiraProperties.CustomField.CustomFieldDetails release = new JiraProperties.CustomField.CustomFieldDetails();
        JiraProperties.CustomField customfield = new JiraProperties.CustomField();
        tshirtSize.setIds(new ArrayList<>());
        customfield.setTShirtSize(tshirtSize);
        customfield.setRelease(release);
        jiraProperties.setCustomfield(customfield);

        doAnswer(NOT_MOCKED_EXCEPTION_ANSWER).when(jiraEndpoint).request(any());
        doReturn(jiraMock.projectRest).when(jiraEndpoint).request(eq(JiraProject.Service.class));
        doReturn(jiraMock.createMetadataRest).when(jiraEndpoint).request(eq(JiraCreateIssue.Service.class));
        doReturn(jiraMock.issueRest).when(jiraEndpoint).request(eq(JiraIssue.Service.class));
        doReturn(jiraMock.searchRest).when(jiraEndpoint).request(eq(JiraIssueDtoSearch.Service.class));
        doAnswer(invocation -> jiraMock.linkTypeRest.all()
                .issueLinkTypes.stream()
                .collect(toMap(l -> l.id, l -> l))
        ).when(metadataService).getIssueLinksMetadata();
    }

    public void jira(JiraProjectBuilder... builders) {
        Arrays.stream(builders)
                .forEach(b -> b.build(this, jiraMock));
    }

    public SizingInvocationBuilder sizing(SizingLineBuilder... builders) {
        SizingInvocationBuilder invocationBuilder = new SizingInvocationBuilder(this);
        Arrays.stream(builders)
                .forEach(b -> b.build(invocationBuilder));
        return invocationBuilder;
    }

    public static JiraProjectBuilder withProject() {
        return new JiraProjectBuilder();
    }

    public static JiraIssueBuilder demand() {
        return new JiraIssueBuilder().isDemand();
    }

    public static JiraIssueBuilder feature() {
        return new JiraIssueBuilder().isFeature();
    }

    public static JiraIssueBuilder subtask() {
        return new JiraIssueBuilder().isSubtask();
    }
    public static JiraIssueBuilder issue() {
        return new JiraIssueBuilder();
    }

    public static SizingLineBuilder phase(String name) {
        return new SizingLineBuilder().phase(name);
    }

    public static JiraIssueTypeBuilder demandType() {
        return new JiraIssueTypeBuilder().isDemand();
    }

    public static JiraIssueTypeBuilder featureType() {
        return new JiraIssueTypeBuilder().isFeature();
    }

    public static JiraIssueTypeBuilder subtaskType() {
        return new JiraIssueTypeBuilder().isSubtask();
    }

    public static JiraIssueTypeBuilder timeboxType() {
        return new JiraIssueTypeBuilder();
    }

    public static JiraIssueTypeBuilder continuousType() {
        return new JiraIssueTypeBuilder();
    }

    public static JiraIssueTypeCustomFieldBuilder customField() {
        return new JiraIssueTypeCustomFieldBuilder();
    }

    public static JiraIssueTypeCustomFieldBuilder tshirtSizeField() {
        return customField().isTshirtSize();
    }

    public static JiraIssueTypeCustomFieldBuilder tshirtSizeField(String name) {
        return tshirtSizeField().name(name);
    }

    public OptionalAssert<JiraIssueDto> assertThatIssue(String key) {
        return jiraMock.assertThatIssue(key);
    }

    static class JiraProjectBuilder {

        private String key;
        private String name;
        private List<JiraProjectVersionBuilder> versions = new ArrayList<>();
        private List<JiraIssueTypeBuilder> issueTypes = new ArrayList<>();
        private List<JiraIssueBuilder> issues = new ArrayList<>();

        public JiraProjectBuilder key(String key) {
            this.key = key;
            return this;
        }

        public JiraProjectBuilder name(String name) {
            this.name = name;
            return this;
        }

        public JiraProjectBuilder withVersions(String... versions) {
            Arrays.stream(versions)
                    .forEach(this::withVersion);
            return this;
        }

        public JiraProjectBuilder withVersion(String version) {
            versions.add(new JiraProjectVersionBuilder().name(version));
            return this;
        }

        public JiraProjectBuilder withIssueTypes(JiraIssueTypeBuilder... builders) {
            issueTypes.addAll(Arrays.asList(builders));
            return this;
        }

        public JiraProjectBuilder withIssues(JiraIssueBuilder... builders) {
            issues.addAll(Arrays.asList(builders));
            return this;
        }

        public void build(ScopeImporterTestDSL2 dsl, SizingImportJiraMock jira) {
            SizingImportJiraMock.ScopeImporterJiraProjectMock project = jira.createProject(key, name);
            versions.forEach(v -> v.build(dsl, project));
            issueTypes.forEach(t -> t.build(dsl, project));
            issues.forEach(i -> i.build(dsl, project));
        }
    }

    static class JiraProjectVersionBuilder {

        private String name;

        public JiraProjectVersionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public void build(ScopeImporterTestDSL2 dsl, SizingImportJiraMock.ScopeImporterJiraProjectMock project) {
            project.createVersion(name);
        }
    }

    enum Lane {
        DEMAND,
        FEATURE,
        SUBTASK,
        CONTINUOUS
    }

    static class JiraIssueTypeBuilder {

        private String name;
        private Lane lane;
        private List<JiraIssueTypeCustomFieldBuilder> fields = new ArrayList<>();

        public JiraIssueTypeBuilder isDemand() {
            this.lane = Lane.DEMAND;
            return this;
        }

        public JiraIssueTypeBuilder isFeature() {
            this.lane = Lane.FEATURE;
            return this;
        }

        public JiraIssueTypeBuilder isSubtask() {
            this.lane = Lane.SUBTASK;
            return this;
        }

        public JiraIssueTypeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public JiraIssueTypeBuilder withCustomFields(JiraIssueTypeCustomFieldBuilder... builders) {
            fields.addAll(Arrays.asList(builders));
            return this;
        }

        public JiraIssueTypeBuilder withCustomFields(String... fieldNames) {
            Arrays.stream(fieldNames)
                    .map(name -> {
                        JiraIssueTypeCustomFieldBuilder field = new JiraIssueTypeCustomFieldBuilder();
                        field.name = name;
                        return field;
                    })
                    .forEach(fields::add);
            return this;
        }

        public void build(ScopeImporterTestDSL2 dsl, SizingImportJiraMock.ScopeImporterJiraProjectMock project) {
            SizingImportJiraMock.ScopeImporterJiraIssueTypeMock issueType = dsl.jiraMock.createIssueType(name, lane == Lane.SUBTASK);
            this.fields.forEach(field -> field.build(dsl, issueType));

            if (lane != null) {
                JiraProperties.IssueType.IssueTypeDetails details = new JiraProperties.IssueType.IssueTypeDetails();
                switch (lane) {
                    case DEMAND:
                        details.setId(issueType.id);
                        dsl.jiraProperties.getIssuetype().setDemand(details);
                        break;
                    case FEATURE:
                        details.setId(issueType.id);
                        dsl.jiraProperties.getIssuetype().getFeatures().add(details);
                        break;
                    default:
                    	break;
                }
            }

            project.addIssueType(issueType);
        }
    }

    static class JiraIssueTypeCustomFieldBuilder {

        private String name;
        private boolean required;
        private boolean isTshirtSize;

        public JiraIssueTypeCustomFieldBuilder isTshirtSize() {
            isTshirtSize = true;
            return this;
        }

        public JiraIssueTypeCustomFieldBuilder name(String name) {
            this.name = name;
            return this;
        }

        public JiraIssueTypeCustomFieldBuilder required(boolean required) {
            this.required = required;
            return this;
        }

        public void build(ScopeImporterTestDSL2 dsl, SizingImportJiraMock.ScopeImporterJiraIssueTypeMock issueType) {
            JiraCreateIssue.FieldInfoMetadata field = issueType.createField(name, required);
            if(isTshirtSize)
                dsl.jiraProperties.getCustomfield().getTShirtSize().getIds().add(field.id);
            if("Release".equals(name)) {
                dsl.jiraProperties.getCustomfield().getRelease().setId(field.id);
            }
        }
    }

    static class JiraIssueBuilder {

        private String key;
        private String type;
        private String summary;

        public JiraIssueBuilder key(String key) {
            this.key = key;
            return this;
        }

        public JiraIssueBuilder isDemand() {
            return type("Demand");
        }

        public JiraIssueBuilder isFeature() {
            return type("Feature");
        }

        public JiraIssueBuilder isSubtask() {
            return type("Sub-task");
        }

        public JiraIssueBuilder type(String type) {
            this.type = type;
            return this;
        }

        public JiraIssueBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public void build(ScopeImporterTestDSL2 dsl, SizingImportJiraMock.ScopeImporterJiraProjectMock project) {
            SizingImportJiraMock.ScopeImporterJiraIssueMock issue = project.createIssue(key);
            if(type != null)
                issue.setField("issuetype", type);
            if(summary != null)
                issue.setField("summary", summary);
        }
    }

    static class SizingInvocationBuilder {

        private List<SizingImportLineScope> lines = new ArrayList<>();
        private ScopeImporterTestDSL2 dsl;

        private SizingInvocationBuilder(ScopeImporterTestDSL2 dsl) {
            this.dsl = dsl;
        }

        public SizingInvocationBuilder intoProject(String projectKey) {
            SizingImporterRecorder recorder = new SizingImporterRecorder();
            dsl.importerNotifier.addListener(recorder);
            ScopeImporter scopeImporter = new ScopeImporter(dsl.importConfig, dsl.jiraFacade, dsl.importerNotifier);

            scopeImporter.executeImport(projectKey, lines);
            return this;
        }
    }

    static class SizingLineBuilder {

        private static final SheetColumn PHASE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.PHASE, "A");
        private static final SheetColumn DEMAND_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.DEMAND, "B");
        private static final SheetColumn FEATURE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.FEATURE, "C");
        private static final SheetColumn TYPE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.TYPE, "D");
        private static final SheetColumn KEY_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.KEY, "C");
        private static final SheetColumn TIMEBOX_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.TIMEBOX, "S");

        private Map<String, SizingImportLine.ImportValue> values = new HashMap<>();

        public SizingLineBuilder phase(String phase) {
            values.put(SheetColumnDefinitionProviderScope.PHASE.getName(), new SizingImportLine.ImportValue(PHASE_COLUMN, phase));
            return this;
        }

        public SizingLineBuilder demand(String demand) {
            values.put(SheetColumnDefinitionProviderScope.DEMAND.getName(), new SizingImportLine.ImportValue(DEMAND_COLUMN, demand));
            return this;
        }

        public SizingLineBuilder feature(String feature) {
            values.put(SheetColumnDefinitionProviderScope.FEATURE.getName(), new SizingImportLine.ImportValue(FEATURE_COLUMN, feature));
            values.put(SheetColumnDefinitionProviderScope.TYPE.getName(), new SizingImportLine.ImportValue(TYPE_COLUMN, "Feature"));
            return this;
        }

        public SizingLineBuilder timebox(String timebox) {
            values.put(SheetColumnDefinitionProviderScope.TIMEBOX.getName(), new SizingImportLine.ImportValue(TIMEBOX_COLUMN, timebox));
            values.put(SheetColumnDefinitionProviderScope.TYPE.getName(), new SizingImportLine.ImportValue(TYPE_COLUMN, "Timebox"));
            return this;
        }

        public SizingLineBuilder key(String key) {
            values.put(SheetColumnDefinitionProviderScope.KEY.getName(), new SizingImportLine.ImportValue(KEY_COLUMN, key));
            return this;
        }

        public void build(SizingInvocationBuilder sizingInvocationBuilder) {
            int index = sizingInvocationBuilder.lines.size();
            sizingInvocationBuilder.lines.add(
                    new SizingImportLineScope(index, new ArrayList<>(values.values()))
            );
        }
    }


}
