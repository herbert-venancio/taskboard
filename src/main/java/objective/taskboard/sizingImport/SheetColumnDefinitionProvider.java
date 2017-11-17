package objective.taskboard.sizingImport;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.SheetColumnDefinition.ColumnTag;
import objective.taskboard.sizingImport.SheetColumnDefinition.PreviewBehavior;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.DefaultColumn;

@Component
class SheetColumnDefinitionProvider {
    public static final SheetColumnDefinition PHASE    = new SheetColumnDefinition("Phase");
    public static final SheetColumnDefinition DEMAND   = new SheetColumnDefinition("Demand");
    public static final SheetColumnDefinition FEATURE  = new SheetColumnDefinition("Feature");
    public static final SheetColumnDefinition KEY      = new SheetColumnDefinition("Key",      PreviewBehavior.HIDE);
    public static final SheetColumnDefinition INCLUDE  = new SheetColumnDefinition("Include",  PreviewBehavior.HIDE);
    public static final SheetColumnDefinition ACCEPTANCE_CRITERIA = new SheetColumnDefinition("Acceptance Criteria");
    
    public static final String SIZING_FIELD_ID_TAG = "sizing-field";

    private final SizingImportConfig importConfig;
    private final List<StaticMappingDefinition> staticMappings;

    @Autowired
    public SheetColumnDefinitionProvider(SizingImportConfig importConfig) {
        this.importConfig = importConfig;
        
        this.staticMappings = Arrays.asList(
                new StaticMappingDefinition(PHASE,    importConfig.getSheetMap().getIssuePhase()),
                new StaticMappingDefinition(DEMAND,   importConfig.getSheetMap().getIssueDemand()),
                new StaticMappingDefinition(FEATURE,  importConfig.getSheetMap().getIssueFeature()),
                new StaticMappingDefinition(KEY,      importConfig.getSheetMap().getIssueKey()),
                new StaticMappingDefinition(INCLUDE,  importConfig.getSheetMap().getInclude()),
                new StaticMappingDefinition(ACCEPTANCE_CRITERIA, importConfig.getSheetMap().getIssueAcceptanceCriteria()));

        Collections.sort(this.staticMappings, comparing(c -> c.getColumnLetter(), SpreadsheetUtils.COLUMN_LETTER_COMPARATOR));
    }

    public List<StaticMappingDefinition> getStaticMappings() {
        return staticMappings;
    }
    
    public List<DynamicMappingDefinition> getDynamicMappings(List<CimFieldInfo> sizingFields) {
        return getSizingColumns(sizingFields);
    }

    private List<DynamicMappingDefinition> getSizingColumns(List<CimFieldInfo> sizingFields) {
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
}
