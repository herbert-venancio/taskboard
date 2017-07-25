package objective.taskboard.sizingImport;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;

import objective.taskboard.sizingImport.SheetDefinition.SheetColumnDefinition;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.DefaultColumn;

class DynamicColumnsDefinitionBuilder {

    private final Collection<String> configuredTShirtSizeFieldsId;
    private final Collection<CimFieldInfo> featureIssueFields;
    private Collection<DefaultColumn> defaultColumns = Collections.emptyList();

    public DynamicColumnsDefinitionBuilder(Collection<String> configuredTShirtSizeFieldsId, Collection<CimFieldInfo> featureIssueFields) {
        this.configuredTShirtSizeFieldsId = configuredTShirtSizeFieldsId;
        this.featureIssueFields = featureIssueFields;
    }
    
    public DynamicColumnsDefinitionBuilder setDefaultColumns(Collection<DefaultColumn> defaultColumns) {
        this.defaultColumns = defaultColumns;
        return this;
    }
    
    public List<SheetColumnDefinition> build() {
        List<CimFieldInfo> tShirtFields = featureIssueFields.stream()
            .filter(f -> configuredTShirtSizeFieldsId.contains(f.getId()))
            .collect(toList());
        
        Map<String, String> defaultColumnByFieldId = defaultColumns.stream()
                .collect(toMap(DefaultColumn::getFieldId, DefaultColumn::getColumn));

        List<SheetColumnDefinition> result = tShirtFields.stream()
                .map(fieldInfo -> {
                    String defaultColumnLetter = defaultColumnByFieldId.get(fieldInfo.getId());
                    boolean required = fieldInfo.isRequired();
                    
                    return new SheetColumnDefinition(fieldInfo.getId(), fieldInfo.getName(), required, defaultColumnLetter);
                })
                .sorted(comparing(SheetColumnDefinition::getDefaultColumnLetter, nullsLast(naturalOrder())))
                .collect(toList());
        
        return result;
    }
}
