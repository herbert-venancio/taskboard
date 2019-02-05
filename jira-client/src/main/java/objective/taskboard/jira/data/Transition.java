package objective.taskboard.jira.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Transition {

    public Long id;
    public String name;
    public Status to;
    public Map<String, Field> fields;
    public Long order;
    public String errorMessage;

    public Transition(){}

    public Transition(Long id, String name, Status to, Map<String, Field> fields) {
        this.id = id;
        this.name = name;
        this.to = to;
        this.fields = fields;
    }

    public static class Field {
        public String name;
        public FieldSchema schema;
        public Boolean required;
        public List<Object> allowedValues;
        public String defaultValue;
        public Boolean hasDefaultValue;

        @JsonIgnore
        public Boolean isArrayOfVersion() {
            return schema != null && schema.isArrayOfVersion();
        }

        @JsonIgnore
        public Boolean isNotSupported() {
            return !isArrayOfVersion();
        }
    }

    public static class FieldSchema {
        public String type;
        public String items;
        public String system;
        public String custom;
        public Long customId;

        Boolean isArrayOfVersion() {
            return Objects.equals(type, "array")
                   && Objects.equals(items, "version");
        }
    }

}