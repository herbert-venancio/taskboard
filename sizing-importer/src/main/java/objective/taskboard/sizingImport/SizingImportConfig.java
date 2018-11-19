package objective.taskboard.sizingImport;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("sizing-import")
@Validated
public class SizingImportConfig {

    public static final String SHEET_SCOPE = "Scope";
    public static final String SHEET_COST = "Cost";
    public static final String SHEET_SIZING_METADATA = "Sizing-Meta-Data";

    @NotNull
    private Integer dataStartingRowNumber;

    @Valid
    private SheetMap sheetMap = new SheetMap();

    @Valid
    private IndirectCosts indirectCosts;

    @NotNull
    private String valueToIgnore;

    @NotEmpty
    private String minimalVersionForCost = "4.1";
    @NotNull
    @Min(0)
    private Integer versionRowIndex = 1;
    @NotEmpty
    private String versionColumnLetter = "B";

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

    public IndirectCosts getIndirectCosts() {
        return indirectCosts;
    }

    public void setIndirectCosts(IndirectCosts indirectCosts) {
        this.indirectCosts = indirectCosts;
    }

    public Double getMinimalVersionForCostDouble() {
        try {
            return Double.parseDouble(minimalVersionForCost);
        } catch (NumberFormatException e) {
            return 4.1D;
        }
    }

    public String getMinimalVersionForCost() {
        return minimalVersionForCost;
    }

    public void setMinimalVersionForCost(String minimalVersionForCost) {
        this.minimalVersionForCost = minimalVersionForCost;
    }

    public Integer getVersionRowIndex() {
        return versionRowIndex;
    }

    public void setVersionRowIndex(Integer versionRowIndex) {
        this.versionRowIndex = versionRowIndex;
    }

    public String getVersionColumnLetter() {
        return versionColumnLetter;
    }

    public void setVersionColumnLetter(String versionColumnLetter) {
        this.versionColumnLetter = versionColumnLetter;
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

        @NotNull
        private String timebox;

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

        public String getTimebox() {
            return timebox;
        }

        public void setTimebox(String timebox) {
            this.timebox = timebox;
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

    public static class IndirectCosts {
        @NotNull
        @NotEmpty
        private String indirectCostsColumn;

        @NotNull
        @NotEmpty
        private String issueKeyColumn;

        @NotNull
        @NotEmpty
        private String effortColumn;

        @NotNull
        @NotEmpty
        private String totalIndirectCostsColumn;

        @NotNull
        @Min(1)
        private Long parentTypeId;

        @NotNull
        @Min(1)
        private Long subtaskTypeId;

        public String getIndirectCostsColumn() {
            return indirectCostsColumn;
        }

        public void setIndirectCostsColumn(String indirectCostsColumn) {
            this.indirectCostsColumn = indirectCostsColumn;
        }

        public String getIssueKeyColumn() {
            return issueKeyColumn;
        }

        public void setIssueKeyColumn(String issueKeyColumn) {
            this.issueKeyColumn = issueKeyColumn;
        }

        public String getEffortColumn() {
            return effortColumn;
        }

        public void setEffortColumn(String effortColumn) {
            this.effortColumn = effortColumn;
        }

        public String getTotalIndirectCostsColumn() {
            return totalIndirectCostsColumn;
        }

        public void setTotalIndirectCostsColumn(String totalIndirectCostsColumn) {
            this.totalIndirectCostsColumn = totalIndirectCostsColumn;
        }

        public Long getParentTypeId() {
            return parentTypeId;
        }

        public void setParentTypeId(Long parentTypeId) {
            this.parentTypeId = parentTypeId;
        }

        public Long getSubtaskTypeId() {
            return subtaskTypeId;
        }

        public void setSubtaskTypeId(Long subtaskTypeId) {
            this.subtaskTypeId = subtaskTypeId;
        }
    }
}
