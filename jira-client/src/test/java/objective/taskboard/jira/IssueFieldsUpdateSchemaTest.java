package objective.taskboard.jira;

import static objective.taskboard.jira.IssueFieldsUpdateSchema.makeUpdateSchema;
import static objective.taskboard.jira.IssueFieldsUpdateSchema.Operation.ADD;
import static objective.taskboard.jira.IssueFieldsUpdateSchema.Operation.SET;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.jira.IssueFieldsUpdateSchema.Operation;

public class IssueFieldsUpdateSchemaTest {

    private Map<String, Object> fields;
    private Map<String, Object> update;

    @Before
    public void setup() {
        fields = new HashMap<>();
        update = new HashMap<>();
    }

    @Test
    public void givenMultiplesFields_whenTryToGetUpdateMap_ThenReturnTheExpectedMap() {
        String commentField = "comment";
        String commentValue = "Comment to test";
        fields.put(commentField, commentValue);

        String assigneeField = "assignee";
        String assigneeValue = "foo.bar";
        fields.put(assigneeField, assigneeValue);

        update.putAll(makeUpdateSchema(fields));

        Map<String, Object> commentOperation = getOperationsListFromField(commentField).get(0);
        assertValueAndOperation(commentOperation, makeMap("body", commentValue), ADD);

        Map<String, Object> assigneeOperation = getOperationsListFromField(assigneeField).get(0);
        assertValueAndOperation(assigneeOperation, makeMap("name", assigneeValue), SET);
    }

    @Test
    public void givenComment_whenTryToGetUpdateMap_ThenReturnCommentWithOperationAdd() {
        String fieldName = "comment";
        String value = "Comment to test";
        fields.put(fieldName, value);

        update.putAll(makeUpdateSchema(fields));

        Map<String, Object> fieldOperation = getOperationsListFromField(fieldName).get(0);
        assertValueAndOperation(fieldOperation, makeMap("body", value), ADD);
    }

    @Test
    public void givenAssignee_whenTryToGetUpdateMap_ThenReturnAssigneeWithOperationSet() {
        String fieldName = "assignee";
        String value = "foo.bar";
        fields.put(fieldName, value);

        update.putAll(makeUpdateSchema(fields));

        Map<String, Object> fieldOperation = getOperationsListFromField(fieldName).get(0);
        assertValueAndOperation(fieldOperation, makeMap("name", value), SET);
    }

    @Test
    public void givenResolution_whenTryToGetUpdateMap_ThenReturnResolutionWithOperationSet() {
        String fieldName = "resolution";
        String value = "Canceled";
        fields.put(fieldName, value);

        update.putAll(makeUpdateSchema(fields));

        Map<String, Object> fieldOperation = getOperationsListFromField(fieldName).get(0);
        assertValueAndOperation(fieldOperation, makeMap("name", value), SET);
    }

    @Test
    public void givenFixVersions_whenTryToGetUpdateMap_ThenReturnFixVersionsWithOperationSet() {
        String fieldName = "fixVersions";
        List<Map<String, Object>> value = Arrays.asList(makeMap("id", "1"));
        fields.put(fieldName, value);

        update.putAll(makeUpdateSchema(fields));

        Map<String, Object> fieldOperation = getOperationsListFromField(fieldName).get(0);
        assertValueAndOperation(fieldOperation, value, SET);
    }

    private void assertValueAndOperation(Map<String, Object> valueToUpdate, Object expectedOperationValue, Operation operation) {
        assertEquals(valueToUpdate.keySet().size(), 1);
        assertEquals(valueToUpdate.get(operation.getValue()), expectedOperationValue);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOperationsListFromField(String fieldKey) {
        return (List<Map<String, Object>>) update.get(fieldKey);
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

}
