package objective.taskboard.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IssueFieldsUpdateSchema {

    public static Map<String, Object> makeUpdateSchema(Map<String, Object> fields) {
        final Map<String, Object> update = new HashMap<>();
        fields.forEach((fieldName, value) -> {
            update.putAll(Field.of(fieldName).jiraValues(value));
        });
        return update;
    }

    private static class Field {

        private static final Map<String, Field> fieldByName;

        static {
            fieldByName = new LinkedHashMap<>();
            fieldByName.put("comment", new Field("comment", Operation.ADD, "body"));
            fieldByName.put("assignee", new Field("assignee", Operation.SET, "name"));
            fieldByName.put("resolution", new Field("resolution", Operation.SET, "name"));
            fieldByName.put("fixVersions", new Field("fixVersions", Operation.SET));
        }

        private final String name;
        private final Operation operation;
        private final String fieldNameToSet;

        public final static Field of(String name) {
            Field field = fieldByName.get(name);
            if (field == null)
                throw new IllegalArgumentException("No such field '" + name + "'");
            return field;
        }

        Field(String name, Operation operation, String fieldNameToSet) {
            this.name = name;
            this.operation = operation;
            this.fieldNameToSet = fieldNameToSet;
        }

        Field(String name, Operation operation) {
            this.name = name;
            this.operation = operation;
            this.fieldNameToSet = "";
        }

        public Map<String, Object> jiraValues(Object value) {
            final Map<String, Object> valueWithOperation = new HashMap<>();
            if (fieldNameToSet.isEmpty()) {
                valueWithOperation.put(operation.getValue(), value);
            } else {
                final Map<String, Object> valueWithType = new HashMap<>();
                valueWithType.put(fieldNameToSet, value);

                valueWithOperation.put(operation.getValue(), valueWithType);
            }

            final List<Map<String, Object>> valueArray = new ArrayList<>();
            valueArray.add(valueWithOperation);

            final Map<String, Object> update = new HashMap<>();
            update.put(name, valueArray);
            return update;
        }
    }

    public enum Operation {
        ADD("add"),
        SET("set"),
        REMOVE("remove");

        private final String name;

        Operation(String name) {
            this.name = name;
        }

        public String getValue() {
            return this.name;
        }
    }

}