package objective.taskboard.sizingImport;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("sizing-import")
@Validated
public class SizingImportConfig {

    public static final String SHEET_TITLE = "Scope";

    @NotNull
    private Integer dataStartingRowNumber;

    @Valid
    private SheetMap sheetMap = new SheetMap();

    @NotNull
    private String valueToIgnore;

    public Integer getDataStartingRowNumber() {
        return dataStartingRowNumber;
    }
    
    public Integer getDataStartingRowIndex() {
        return dataStartingRowNumber - 1;
    }

    public void setDataStartingRowNumber(Integer dataStartingRowNumber) {
        this.dataStartingRowNumber = dataStartingRowNumber;
    }

    public SheetMap getSheetMap() {
        return sheetMap;
    }

    public void setSheetMap(SheetMap sheetMap) {
        this.sheetMap = sheetMap;
    }

    public String getValueToIgnore() {
        return valueToIgnore;
    }

    public void setValueToIgnore(String valueToIgnore) {
        this.valueToIgnore = valueToIgnore;
    }

    public static class SheetMap {

        @NotNull
        private String issuePhase;

        @NotNull
        private String issueKey;

        @NotNull
        private String issueDemand;

        @NotNull
        private String issueFeature;

        @NotNull
        private String type;
        
        @NotNull
        private String include;

        @Valid
        private List<DefaultColumn> defaultColumns = new ArrayList<>();
        
        @Valid
        private List<ExtraField> extraFields = new ArrayList<>();
        
        public String getIssuePhase() {
            return issuePhase;
        }

        public void setIssuePhase(String issuePhase) {
            this.issuePhase = issuePhase;
        }

        public String getIssueKey() {
            return issueKey;
        }

        public void setIssueKey(String issueKey) {
            this.issueKey = issueKey;
        }

        public String getIssueDemand() {
            return issueDemand;
        }

        public void setIssueDemand(String issueDemand) {
            this.issueDemand = issueDemand;
        }

        public String getIssueFeature() {
            return issueFeature;
        }

        public void setIssueFeature(String issueFeature) {
            this.issueFeature = issueFeature;
        }

        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getInclude() {
            return include;
        }

        public void setInclude(String include) {
            this.include = include;
        }

        public List<DefaultColumn> getDefaultColumns() {
            return defaultColumns;
        }

        public void setDefaultColumns(List<DefaultColumn> defaultColumns) {
            this.defaultColumns = defaultColumns;
        }

        public List<ExtraField> getExtraFields() {
            return extraFields;
        }

        public void setExtraFields(List<ExtraField> extraFields) {
            this.extraFields = extraFields;
        }

        public static class DefaultColumn {
            @NotNull
            private String fieldId;
            
            @NotNull
            private String column;

            public String getFieldId() {
                return fieldId;
            }

            public void setFieldId(String fieldId) {
                this.fieldId = fieldId;
            }

            public String getColumn() {
                return column;
            }

            public void setColumn(String column) {
                this.column = column;
            }
        }
        
        public static class ExtraField {
            @NotNull
            private String fieldId;
            
            @NotNull
            private String columnHeader;
            
            @NotNull
            private String columnLetter;

            public ExtraField(String fieldId, String columnHeader, String columnLetter) {
                this.fieldId = fieldId;
                this.columnHeader = columnHeader;
                this.columnLetter = columnLetter;
            }
            
            public ExtraField() {
            }

            public String getFieldId() {
                return fieldId;
            }

            public void setFieldId(String fieldId) {
                this.fieldId = fieldId;
            }

            public String getColumnHeader() {
                return columnHeader;
            }

            public void setColumnHeader(String columnHeader) {
                this.columnHeader = columnHeader;
            }

            public String getColumnLetter() {
                return columnLetter;
            }

            public void setColumnLetter(String columnLetter) {
                this.columnLetter = columnLetter;
            }
        }
    }
}
