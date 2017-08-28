package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.SizingImportLine.JiraField;

@Component
class SizingSheetParser {

    private final SizingImportConfig properties;
    
    @Autowired
    public SizingSheetParser(SizingImportConfig properties) {
        this.properties = properties;
    }

    public List<SizingImportLine> getSpreedsheetData(List<List<Object>> rows, List<SheetColumnMapping> dynamicColumnsMapping) {
        if (rows == null || rows.isEmpty())
            return Collections.emptyList();

        List<SizingImportLine> lines = new ArrayList<>();
        int startingRowIndex = properties.getDataStartingRowIndex();

        for (int i = startingRowIndex; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            
            if (row.isEmpty())
                continue;

            lines.add(parseRow(row, i, dynamicColumnsMapping));
        }

        List<SizingImportLine> includedLines = lines.stream()
                .filter(SizingImportLine::isInclude)
                .collect(toList());

        return includedLines;
    }

    private SizingImportLine parseRow(List<Object> row, int rowIndex, List<SheetColumnMapping> dynamicColumnsMapping) {
        String includeColumn = properties.getSheetMap().getInclude();
        
        SizingImportLine line = new SizingImportLine();
        line.setIndexRow(rowIndex);
        line.setPhase(getValue(row, properties.getSheetMap().getIssuePhase()));
        line.setDemand(getValue(row, properties.getSheetMap().getIssueDemand()));
        line.setFeature(getValue(row, properties.getSheetMap().getIssueFeature()));
        line.setAcceptanceCriteria(getValue(row, properties.getSheetMap().getIssueAcceptanceCriteria()));
        line.setJiraKey(getValue(row, properties.getSheetMap().getIssueKey()));
        line.setInclude("true".equalsIgnoreCase(getValue(row, includeColumn)));
        
        for (SheetColumnMapping dynamicColumnMapping : dynamicColumnsMapping) {
            int columnIndex = SpreadsheetUtils.columnLetterToIndex(dynamicColumnMapping.getColumnLetter());

            if (columnIndex < row.size()) {
                String value = getValue(row, columnIndex);
                JiraField jiraValue = new JiraField(dynamicColumnMapping.getFieldId(), value);
                line.addField(jiraValue);
            }
        }

        return line;
    }

    private String getValue(List<Object> row, int index) {
        String value = (String) row.get(index);

        if (value == null || properties.getValueToIgnore().equals(value)) {
            return null;
        }

        return StringUtils.trimToEmpty(value);
    }
    
    private String getValue(List<Object> row, String columnLetter) {
        int index = SpreadsheetUtils.columnLetterToIndex(columnLetter);
        return getValue(row, index);
    }
    
    static class SheetColumnMapping {
        private final String fieldId;
        private final String columnLetter;
        
        public SheetColumnMapping(String fieldId, String columnLetter) {
            this.fieldId = fieldId;
            this.columnLetter = columnLetter.toUpperCase();
        }
        
        public String getFieldId() {
            return fieldId;
        }
        
        public String getColumnLetter() {
            return columnLetter;
        }
    }
}