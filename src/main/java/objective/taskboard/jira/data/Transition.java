package objective.taskboard.jira.data;

import java.util.List;
import java.util.Map;

public class Transition {

    public final Long id;
    public final String name;
    public final Status to;
    public final Map<String, Field> fields;

    public Transition(Long id, String name, Status to, Map<String, Field> fields) {
        this.id = id;
        this.name = name;
        this.to = to;
        this.fields = fields;
    }

    public static class Field {
        public String name;
        public Boolean required;
        public List<String> allowedValues;
        public String defaultValue;
        public Boolean hasDefaultValue;
    }

}