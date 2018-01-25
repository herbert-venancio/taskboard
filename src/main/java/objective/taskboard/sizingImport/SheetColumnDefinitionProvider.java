package objective.taskboard.sizingImport;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static objective.taskboard.utils.StreamUtils.distinctByKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.SheetColumnDefinition.ColumnTag;
import objective.taskboard.sizingImport.SheetColumnDefinition.PreviewBehavior;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.DefaultColumn;

@Component
class SheetColumnDefinitionProvider {
    public static final SheetColumnDefinition PHASE    = new SheetColumnDefinition("Phase");
    public static final SheetColumnDefinition DEMAND   = new SheetColumnDefinition("Demand");
    public static final SheetColumnDefinition FEATURE  = new SheetColumnDefinition("Feature / Task");
    public static final SheetColumnDefinition TYPE     = new SheetColumnDefinition("Type");
    public static final SheetColumnDefinition KEY      = new SheetColumnDefinition("Key",      PreviewBehavior.HIDE);
    public static final SheetColumnDefinition INCLUDE  = new SheetColumnDefinition("Include",  PreviewBehavior.HIDE);
    
    public static final String SIZING_FIELD_ID_TAG = "sizing-field";
    public static final String EXTRA_FIELD_ID_TAG = "extra-field";

    private final SizingImportConfig importConfig;
    private final JiraFacade jiraFacade;
    private final List<StaticMappingDefinition> staticMappings = new ArrayList<>();

    @Autowired
    public SheetColumnDefinitionProvider(SizingImportConfig importConfig, JiraFacade jiraFacade) {
        this.importConfig = importConfig;
        this.jiraFacade = jiraFacade;
        
        this.staticMappings.addAll(getRegularMappings());
        this.staticMappings.addAll(getExtraFieldMappings());

        sort(this.staticMappings, comparing(c -> c.getColumnLetter(), SpreadsheetUtils.COLUMN_LETTER_COMPARATOR));
    }

    private List<StaticMappingDefinition> getRegularMappings() {
        return Arrays.asList(
                new StaticMappingDefinition(PHASE,    importConfig.getSheetMap().getIssuePhase()),
                new StaticMappingDefinition(DEMAND,   importConfig.getSheetMap().getIssueDemand()),
                new StaticMappingDefinition(FEATURE,  importConfig.getSheetMap().getIssueFeature()),
                new StaticMappingDefinition(TYPE,     importConfig.getSheetMap().getType()),
                new StaticMappingDefinition(KEY,      importConfig.getSheetMap().getIssueKey()),
                new StaticMappingDefinition(INCLUDE,  importConfig.getSheetMap().getInclude()));
    }

    private List<StaticMappingDefinition> getExtraFieldMappings() {
        return importConfig.getSheetMap().getExtraFields().stream()
                .map(extraField -> {
                    ColumnTag extraFieldTag = new ColumnTag(EXTRA_FIELD_ID_TAG, extraField.getFieldId());
                    SheetColumnDefinition columnDefinition = new SheetColumnDefinition(extraField.getColumnHeader(), extraFieldTag);

                    return new StaticMappingDefinition(columnDefinition, extraField.getColumnLetter());
                })
                .collect(toList());
    }

    public List<StaticMappingDefinition> getStaticMappings() {
        return staticMappings;
    }
    
    public List<DynamicMappingDefinition> getDynamicMappings(String projectKey) {
        List<CimFieldInfo> sizingFields = getConfiguredSizingFields(projectKey);

        List<DefaultColumn> defaultColumns = importConfig.getSheetMap().getDefaultColumns();
        Map<String, String> defaultColumnByFieldId = defaultColumns.stream().collect(toMap(DefaultColumn::getFieldId, DefaultColumn::getColumn));

        return sizingFields.stream()
                .map(fieldInfo -> {
                    ColumnTag sizingFieldTag = new ColumnTag(SIZING_FIELD_ID_TAG, fieldInfo.getId());
                    SheetColumnDefinition columnDefinition = new SheetColumnDefinition(fieldInfo.getName(), sizingFieldTag);

                    String columnId = "sizing:" + fieldInfo.getId();
                    Optional<String> defaultColumnLetter = Optional.ofNullable(defaultColumnByFieldId.get(fieldInfo.getId()));
                    boolean mappingRequired = fieldInfo.isRequired();

                    return new DynamicMappingDefinition(columnDefinition, columnId, mappingRequired, defaultColumnLetter);
                })
                .sorted(comparing(md -> md.getDefaultColumnLetter().orElse(null), nullsLast(SpreadsheetUtils.COLUMN_LETTER_COMPARATOR)))
                .collect(toList());
    }

    private List<CimFieldInfo> getConfiguredSizingFields(String projectKey) {
        List<CimIssueType> featureTypes = jiraFacade.requestFeatureTypes(projectKey);
        List<String> configuredSizingFieldIds = jiraFacade.getSizingFieldIds();

        return featureTypes.stream()
                .flatMap(featureType -> featureType.getFields().values().stream())
                .filter(field -> configuredSizingFieldIds.contains(field.getId()))
                .filter(distinctByKey(CimFieldInfo::getId))
                .sorted(comparing(CimFieldInfo::getName))
                .collect(toList());
    }
}
