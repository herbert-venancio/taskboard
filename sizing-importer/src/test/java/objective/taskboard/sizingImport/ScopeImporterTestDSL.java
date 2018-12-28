package objective.taskboard.sizingImport;

import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.sizingImport.SheetColumnDefinition.ColumnTag;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.EXTRA_FIELD_ID_TAG;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.SIZING_FIELD_ID_TAG;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.StringUtils.hasText;

import com.google.gson.Gson;

public class ScopeImporterTestDSL {

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final JiraFacade jiraFacade = mock(JiraFacade.class);
    private final SizingSheetImporterNotifier importerNotifier = new SizingSheetImporterNotifier();
    private static SizingImporterRecorder recorder;

    public ScopeImporterTestDSL() {
        when(jiraFacade.findDemandBySummary(any(), any()))
                .thenReturn(Optional.empty());
    }

    protected DSLJiraBuilder jira() {
        return new DSLJiraBuilder(this);
    }

    protected DSLSizingBuilder sizing() {
        recorder = new SizingImporterRecorder();
        return new DSLSizingBuilder();
    }

    class DSLJiraBuilder {

        private ScopeImporterTestDSL scopeImporterTestDSL;

        public DSLJiraBuilder(ScopeImporterTestDSL scopeImporterTestDSL) {
            this.scopeImporterTestDSL = scopeImporterTestDSL;
        }

        public DSLProjectJiraBuilder withProject() {
            return new DSLProjectJiraBuilder(this);
        }

        public ScopeImporterTestDSL eoJ() {
            return this.scopeImporterTestDSL;
        }
    }

    class DSLProjectJiraBuilder {

        private DSLJiraBuilder dslJiraBuilder;

        private String projectKey;
        private String projectName;
        private String version;
        private DSLIssues dslIssues;

        public DSLProjectJiraBuilder(final DSLJiraBuilder dslJiraBuilder) {
            this.dslJiraBuilder = dslJiraBuilder;
        }

        public DSLProjectJiraBuilder key(final String projectKey) {
            this.projectKey = projectKey;
            return this;
        }

        public DSLIssues withIssues() {
            dslIssues = new DSLIssues(this);
            return dslIssues;
        }

        public DSLFeatureTypeBuilder withFeatureType() {
            return new DSLFeatureTypeBuilder(this);
        }

        public DSLProjectJiraBuilder name(final String projectName) {
            this.projectName = projectName;
            return this;
        }

        public DSLProjectJiraBuilder withVersion(final String version) {
            this.version = version;

            when(jiraFacade.createVersion(projectKey, this.version))
                    .thenReturn(new Version("1", this.version));
            return this;
        }

        public ScopeImporterTestDSL eoP() {
            when(jiraFacade.getProject(projectKey))
                .thenReturn(jiraProject(projectKey, projectName, getVersion()));

            return this.dslJiraBuilder.eoJ();
        }

        private List<Version> getVersion() {
            if (hasText(version))
                return asList(new Version("1", this.version));

            return emptyList();
        }

        private JiraProject jiraProject(String key, String name, List<Version> versions) {
            return new JiraProject("0", key, versions, name);
        }
    }

    class DSLIssues {

        DSLProjectJiraBuilder dslProjectJiraBuilder;

        public DSLIssues(final DSLProjectJiraBuilder dslProjectJiraBuilder) {
            this.dslProjectJiraBuilder = dslProjectJiraBuilder;
        }

        public DSLProjectJiraBuilder eoIs() {
            return dslProjectJiraBuilder;
        }

        public DSLIssue issue() {
            return new DSLIssue(this);
        }
    }

    class DSLIssue {

        private DSLIssues dslIssues;

        private String demandKey = null;
        private String name = null;
        private boolean isDemand = false;

        public DSLIssue(final DSLIssues dslIssues) {
            this.dslIssues = dslIssues;
        }

        public DSLIssue key(final String demandKey) {
            this.demandKey = demandKey;
            return this;
        }

        public DSLIssue name(final String name) {
            this.name = name;
            return this;
        }

        public DSLIssue isDemand() {
            this.isDemand = true;
            return this;
        }

        public DSLIssues eoI() {
            if (isDemand) {
                when(jiraFacade.createDemand(any(), any(), any()))
                        .thenReturn(new JiraIssue(demandKey));

                when(jiraFacade.findDemandBySummary(any(), eq(name)))
                        .thenReturn(Optional.of(createDemand(demandKey, name)));
            } else {

                if (hasText(name)) {

                    when(jiraFacade.createFeature(any(), any(), any(),  eq(name), any(), any()))
                            .thenReturn(new JiraIssue(demandKey));
                } else {

                    when(jiraFacade.createFeature(any(), any(), any(),  any(), any(), any()))
                            .thenReturn(new JiraIssue(demandKey));
                }

                when(jiraFacade.createTimebox(any(), any(), any(),  any(), any(), any(), any()))
                        .thenReturn(new JiraIssue(demandKey));
            }

            return dslIssues;
        }
    }

    class DSLFeatureTypeBuilder {

        DSLProjectJiraBuilder jiraBuilder;
        private List<DSLFeatureIssueBuilder> features = new ArrayList<>();

        public DSLFeatureTypeBuilder(final DSLProjectJiraBuilder jiraBuilder) {
            this.jiraBuilder = jiraBuilder;
        }

        public DSLFeatureIssueBuilder feature() {
            return new DSLFeatureIssueBuilder(this);
        }

        public DSLProjectJiraBuilder eoFt() {
            when(jiraFacade.requestFeatureTypes(jiraBuilder.projectKey))
                .thenReturn(getIssueTypeMetadataList());

            if (!features.isEmpty()) {
                List <String> fields = new ArrayList<>();

                features.forEach(f -> f.customFields.forEach(c -> fields.add(c.id)));

                when(jiraFacade.getSizingFieldIds())
                        .thenReturn(fields);
            }
            return jiraBuilder;
        }

        private List<JiraCreateIssue.IssueTypeMetadata> getIssueTypeMetadataList() {
            return features.stream()
                .map(f -> new JiraCreateIssue.IssueTypeMetadata(f.id, f.featureTypeName, f.isSubTask, toFields(f.customFields)))
                .collect(toList());
        }

        private Map<String, JiraCreateIssue.FieldInfoMetadata> toFields(final List<DSLCustomFieldBuilder> dslFields) {
            Map<String, JiraCreateIssue.FieldInfoMetadata> fields = new HashMap<>();
            dslFields.forEach(f -> fields.put(f.id, new JiraCreateIssue.FieldInfoMetadata(f.id, f.isRequired, f.name)));

            return fields;
        }
    }

    class DSLFeatureIssueBuilder {

        private final Long id = 30L;
        private final DSLFeatureTypeBuilder featureTypeBuilder;
        private String featureTypeName;
        private boolean isSubTask;
        private List<DSLCustomFieldBuilder> customFields = new ArrayList<>();

        public DSLFeatureIssueBuilder(final DSLFeatureTypeBuilder dslFeatureTypeBuilder) {
            this.featureTypeBuilder = dslFeatureTypeBuilder;
        }

        public DSLCustomFieldBuilder withCustomField() {
            return new DSLCustomFieldBuilder(this);
        }

        public DSLFeatureIssueBuilder name(final String featureTypeName) {
            this.featureTypeName = featureTypeName;
            return this;
        }

        public DSLFeatureTypeBuilder eoF() {
            featureTypeBuilder.features.add(this);
            return featureTypeBuilder;
        }
    }

    class DSLCustomFieldBuilder {

        private final DSLFeatureIssueBuilder featureIssueBuilder;
        private String name;
        private String id;
        private boolean isRequired;

        public DSLCustomFieldBuilder(DSLFeatureIssueBuilder featureIssueBuilder) {
            this.featureIssueBuilder = featureIssueBuilder;
        }

        public DSLCustomFieldBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public DSLCustomFieldBuilder withId(final String id) {
            this.id = id;
            return this;
        }

        public DSLCustomFieldBuilder isRequired() {
            this.isRequired = true;
            return this;
        }

        public DSLFeatureIssueBuilder eoCf() {
            featureIssueBuilder.customFields.add(this);
            return featureIssueBuilder;
        }
    }

    class DSLSizingBuilder {

        private ScopeImporter scopeImporter = null;
        private DSLLines dslLines = new DSLLines(this);

        public DSLConfigBuilder config() {
            return new DSLConfigBuilder(this);
        }

        public DSLLines lines() {
            return this.dslLines;
        }

        public DSLSizingBuilder importedToProject(final String projectKey) {
            importerNotifier.addListener(recorder);
            scopeImporter = new ScopeImporter(importConfig, jiraFacade, importerNotifier);

            scopeImporter.executeImport(projectKey, dslLines.getLinesToImport());
            return this;
        }

        public DSLAssertBuilder then() {
            return new DSLAssertBuilder(this);
        }

        public void noIssuesHaveBeenCreated() {
            verify(jiraFacade, never()).createVersion(any(), any());
            verify(jiraFacade, never()).createDemand(any(), any(), any());
            verify(jiraFacade, never()).createFeature(any(), any(), any(), any(), any(), any());
        }

        public void noVersionHaveBeenCreated() {
            verify(jiraFacade, never()).createVersion(any(), any());
        }
    }

    class DSLConfigBuilder {

        private DSLSizingBuilder sizingBuilder;
        private List<DSLExtraField> extraFields = new ArrayList<DSLExtraField>();
        private String timeboxColumn = "";

        public DSLConfigBuilder(DSLSizingBuilder sizingBuilder) {
            this.sizingBuilder = sizingBuilder;
        }

        public DSLExtraField extraField() {
            return new DSLExtraField(this);
        }

        public DSLSizingBuilder eoC() {
            this.extraFields
                .forEach(e ->
                    importConfig.getSheetMap().getExtraFields()
                        .add(new SizingImportConfig.SheetMap.ExtraField(e.id, e.columnHeader, e.mappedColumn))
                );

            if (hasText(timeboxColumn))
                importConfig.getSheetMap().setTimebox(timeboxColumn);

            return this.sizingBuilder;
        }

        public DSLConfigBuilder withTimeboxColumnLetter(final String letter) {
            this.timeboxColumn = letter;
            return this;
        }
    }

    class DSLExtraField {

        private DSLConfigBuilder config;
        private String id;
        private String columnHeader;
        private String mappedColumn;

        public DSLExtraField(final DSLConfigBuilder config) {
            this.config = config;
        }

        public DSLExtraField id(final String id) {
            this.id = id;
            return this;
        }

        public DSLExtraField columnHeader(final String columnHeader) {
            this.columnHeader = columnHeader;
            return this;
        }

        public DSLExtraField mappedToColumn(final String mappedColumn) {
            this.mappedColumn = mappedColumn;
            return this;
        }

        public DSLConfigBuilder eoEf() {
            config.extraFields.add(this);
            return config;
        }
    }

    class DSLLines {

        private DSLSizingBuilder sizingBuilder;
        private List<DSLLine> lines = new ArrayList<>();

        private final SheetColumn PHASE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.PHASE, "A");
        private final SheetColumn DEMAND_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.DEMAND, "B");
        private final SheetColumn FEATURE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.FEATURE, "C");
        private final SheetColumn TYPE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.TYPE, "D");
        private final SheetColumn KEY_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.KEY, "C");
        private final SheetColumn TIMEBOX_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.TIMEBOX, "S");

        public DSLLines(final DSLSizingBuilder sizingBuilder) {
            this.sizingBuilder = sizingBuilder;
        }

        public DSLLine line() {
            return new DSLLine(this);
        }

        protected DSLSizingBuilder eoLs() {
            return sizingBuilder;
        }

        public List<SizingImportLineScope> getLinesToImport() {
            return this.lines.stream()
                .map(this::toSizingImportLineScope)
                    .collect(toList());
        }

        private SizingImportLineScope toSizingImportLineScope(final DSLLine line) {
            List<SizingImportLine.ImportValue> values = new ArrayList<>();
            values.addAll(
                asList(
                    new SizingImportLine.ImportValue(PHASE_COLUMN, line.phase),
                    new SizingImportLine.ImportValue(DEMAND_COLUMN, line.name),
                    new SizingImportLine.ImportValue(FEATURE_COLUMN, line.featureName),
                    new SizingImportLine.ImportValue(TYPE_COLUMN, line.type),
                    new SizingImportLine.ImportValue(KEY_COLUMN, line.key),
                    new SizingImportLine.ImportValue(TIMEBOX_COLUMN, line.timeboxValue)
                )
            );
            List<DSLExtraColumn> extraColumns = line.extraColumns.columns;

            if (!extraColumns.isEmpty()) {
                List<SizingImportLine.ImportValue> extraValues = extraColumns.stream()
                        .map(e -> new SizingImportLine.ImportValue(toSheetColumn(e), e.value))
                        .collect(toList());

                values.addAll(extraValues);
            }
            AtomicInteger lineNumber = new AtomicInteger();

            return new SizingImportLineScope(lineNumber.getAndIncrement(), values);
        }

        private SheetColumn toSheetColumn(final DSLExtraColumn extraColumn) {
            String definition = EXTRA_FIELD_ID_TAG;

            if (extraColumn.isTypeSizing)
                definition = SIZING_FIELD_ID_TAG;

            Optional<SizingImportConfig.SheetMap.ExtraField> extraFieldConfiguration =
                importConfig.getSheetMap().getExtraFields()
                    .stream()
                    .filter(f -> f.getColumnHeader().equals(extraColumn.headerName))
                    .findFirst();

            String tagValue = "xxxx";
            String sheetLetter = "xxxx";

            if (extraFieldConfiguration.isPresent()) {
                tagValue = extraFieldConfiguration.get().getFieldId();
                sheetLetter = extraFieldConfiguration.get().getColumnLetter();
            }

            return new SheetColumn(new SheetColumnDefinition(extraColumn.headerName, new ColumnTag(definition, tagValue)), sheetLetter);
        }
    }

    class DSLLine {

        private DSLLines dsLlines;

        private String phase;
        private String name;
        private String featureName;
        private String timeboxValue;
        private String type;
        private String key;

        private DSLExtraColumns extraColumns = new DSLExtraColumns(this);

        public DSLLine(final DSLLines dsLlines) {
            this.dsLlines = dsLlines;
        }

        public DSLLine phase(final String phase) {
            this.phase = phase;
            return this;
        }

        public DSLLine demand(final String name) {
            this.name = name;
            return this;
        }

        public DSLLine feature(final String featureName) {
            this.featureName = featureName;
            this.type = "Feature";
            return this;
        }

        public DSLLine task(final String featureName) {
            this.featureName = featureName;
            this.type = "Task";
            return this;
        }

        public DSLLine key(final String key) {
            this.key = key;
            return this;
        }

        public DSLLine type(final String type) {
            this.type = type;
            return this;
        }

        public DSLLine name(final String featureName) {
            this.featureName = featureName;
            return this;
        }

        public DSLLine timebox(final String timeboxValue) {
            this.type = "Timebox";
            this.timeboxValue = timeboxValue;
            return this;
        }

        public DSLLines eoL() {
            this.dsLlines.lines.add(this);
            return this.dsLlines;
        }

        public DSLExtraColumns withExtraColumns() {
            return this.extraColumns;
        }
    }

    class DSLExtraColumns {

        private DSLLine dslLine;
        private List<DSLExtraColumn> columns = new ArrayList<>();

        public DSLExtraColumns(final DSLLine dslLine) {
            this.dslLine = dslLine;
        }

        public DSLExtraColumn column() {
            return new DSLExtraColumn(this);
        }

        public DSLLine eoEc() {
            return  dslLine;
        }
    }

    class DSLExtraColumn {

        private DSLExtraColumns dslExtraColumns;
        private String headerName;
        private String value;
        private boolean isTypeSizing = false;

        public DSLExtraColumn(final DSLExtraColumns dslExtraColumns) {
            this.dslExtraColumns = dslExtraColumns;
        }

        public DSLExtraColumn name(final String headerName) {
            this.headerName = headerName;
            return this;
        }

        public DSLExtraColumn value(final String value) {
            this.value = value;
            return this;
        }

        public DSLExtraColumns eoC() {
            this.dslExtraColumns.columns.add(this);
            return dslExtraColumns;
        }

        public DSLExtraColumn isTypeSizing() {
            this.isTypeSizing = true;
            return this;
        }
    }

    class DSLAssertBuilder {

        private DSLSizingBuilder sizingBuilder;

        public DSLAssertBuilder(final DSLSizingBuilder sizingBuilder) {
            this.sizingBuilder = sizingBuilder;
        }

        public DSLAssertBuilder rejectLine(final int expectedLineError) {
            String errorLine = getEventsReturned()
                    .stream()
                    .filter(f -> f.startsWith("Line error - Row index: "))
                    .findFirst().orElse("");

            int errorNumber = 0;
            if (hasText(errorLine)) {
                int errorNumberIndex = new Integer(removeNonNumericCharacters(errorLine));
                errorNumber = errorNumberIndex + 1;
            }
            assertEquals(expectedLineError, errorNumber);
            return this;
        }

        public DSLAssertBuilder withError(final String expected) {
            List<String> events = getEventsReturned();

            String errorLine = events
                .stream()
                .filter(f -> f.contains("errors: "))
                .collect(joining(", "));

            String[] split = errorLine.split("errors: ");
            assertEquals(expected, split[split.length - 1]);
            return this;
        }

        public DSLAssertBuilder withLinesToImport(final int expectedNumberOfLinesToImport) {
            List<String> events = getEventsReturned();
            int linesToImport = getQuantityOfLinesToImport(events);

            assertEquals(expectedNumberOfLinesToImport, linesToImport);
            return this;
        }

        public DSLAssertBuilder importIsFinished() {
            List<String> events = getEventsReturned();

            int lastIndex = events.size() - 1;
            String lastLine = events.get(lastIndex);

            assertEquals("Import finished", lastLine);
            return this;
        }

        public DSLAssertBuilder withSuccessfulIssueImported(final String expectedKey) {
            List<String> events = getEventsReturned();
            String keys = extractIssueKeys(events);

            assertEquals(expectedKey, keys);
            return this;
        }

        public DSLAssertBuilder withSuccessfulIssuesImported(String...expectedIssueKeys) {
            List<String> events = getEventsReturned();
            String keys = extractIssueKeys(events);

            String current = join(expectedIssueKeys, ", ");
            assertEquals(current, keys);
            return this;
        }

        public DSLSizingBuilder and() {
            return sizingBuilder;
        }

        private String removeNonNumericCharacters(final String string) {
            return string.replaceAll("\\D+","");
        }

        private int getQuantityOfLinesToImport(final List<String> events) {
            String firstLineEvent = events.get(0);

            if (!hasText(firstLineEvent))
                return -1;

            int linesToImportIndex = firstLineEvent.lastIndexOf(':') + 1;
            String linesToImportString = firstLineEvent.substring(linesToImportIndex).trim();
            return new Integer(linesToImportString);
        }

        private String extractIssueKeys(final List<String> events) {
            return events
                    .stream()
                    .filter(f -> f.contains("issue key: "))
                    .map(e -> e.substring(e.indexOf("issue key: ") + 11))
                    .collect(joining(", "));
        }
        private List<String> getEventsReturned() {
            List<String> events = recorder.getEvents();
            if (events.isEmpty()) {
                fail("No events found!");
            }
            return events;
        }
    }

    private static JiraIssueDto createDemand(String key, String summary) {
        Map<String, Object> json = new HashMap<>();
        json.put("key", key);
        json.put("fields", singletonMap("summary", summary));

        Gson gson = new Gson();
        return gson.fromJson(gson.toJsonTree(json), JiraIssueDto.class);
    }
}
