package objective.taskboard.sizingImport;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SizingImportLine {

    private int rowIndex;
    private String jiraKey;
    private String phase;
    private String demand;
    private String feature;
    private boolean include;
    private List<JiraField> fields = new ArrayList<>();

    public int getRowIndex() {
        return rowIndex;
    }
    
    public int getRowNumber() {
        return rowIndex + 1;
    }

    public void setIndexRow(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public List<JiraField> getFields() {
        return fields;
    }
    
    public void addField(JiraField field) {
        fields.add(field);
    }

    public Optional<String> getFieldValue(String fieldId) {
        return fields.stream()
                .filter(f -> f.getId().equals(fieldId))
                .findFirst()
                .map(JiraField::getValue);
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public void setJiraKey(String key) {
        this.jiraKey = key;
    }
    
    public boolean isImported() {
        return isNotBlank(jiraKey);
    }
    
    public boolean isNotImported() {
        return !isImported();
    }
    
    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }
    
    public String getDemand() {
        return demand;
    }

    public void setDemand(String demand) {
        this.demand = demand;
    }
    
    public String getFeature() {
        return feature;
    }
    
    public void setFeature(String feature) {
        this.feature = feature;
    }
    
    public boolean isInclude() {
        return include;
    }
    
    public void setInclude(boolean include) {
        this.include = include;
    }
    
    static class JiraField {
        private final String id;
        private final String value;

        public JiraField(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getId() {
            return id;
        }
    }
}
