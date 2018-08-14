package objective.taskboard.jira.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byId;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byIds;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byKey;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import objective.taskboard.jira.properties.JiraProperties;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public class JiraIssue {

    public String key;
    
    public JiraIssue(){}

    public JiraIssue(String key) {
        this.key = key;
    }

    public interface Service {
        @POST("/rest/api/latest/issue")
        JiraIssue create(@Body Input input);

        @PUT("/rest/api/latest/issue/{id}")
        Response update(@Path("id") String id, @Body Input input);

        @POST("/rest/api/latest/issueLink")
        Response linkIssue(@Body LinkInput input);
    }

    public static class Input {

        public static InputBuilder<?> builder() {
            return new InputBuilder<>();
        }

        public static InputBuilder<?> builder(String projectKey, long issueTypeId) {
            return new InputBuilder<>()
                    .project(byKey(projectKey))
                    .issueType(byId(issueTypeId));
        }

        public static CustomInputBuilder builder(JiraProperties jiraProperties) {
            return new CustomInputBuilder(jiraProperties);
        }

        public static CustomInputBuilder builder(JiraProperties jiraProperties, String projectKey, long issueTypeId) {
            return new CustomInputBuilder(jiraProperties)
                    .project(byKey(projectKey))
                    .issueType(byId(issueTypeId));
        }

        public final Map<String, Object> fields;

        public Input(Map<String, Object> fields) {
            this.fields = fields;
        }
    }

    public static class InputBuilder<T extends InputBuilder<T>> {

        @SuppressWarnings("unchecked")
        private T self = (T) this;

        private InputBuilder() {}

        protected final Map<String, Object> fields = new LinkedHashMap<>();

        public T project(FieldBuilder fieldBuilder) {
            return field("project", fieldBuilder);
        }

        public T issueType(FieldBuilder fieldBuilder) {
            return field("issuetype", fieldBuilder);
        }

        public T parent(FieldBuilder fieldBuilder) {
            return field("parent", fieldBuilder);
        }

        public T priority(FieldBuilder fieldBuilder) {
            return field("priority", fieldBuilder);
        }

        public T summary(String value) {
            return field("summary", value);
        }

        public T reporter(FieldBuilder fieldBuilder) {
            return field("reporter", fieldBuilder);
        }

        public T assignee(FieldBuilder fieldBuilder) {
            return field("assignee", fieldBuilder);
        }

        public T originalEstimate(String value) {
            return field("timetracking", singletonMap("originalEstimate", value));
        }

        public T field(String field, FieldBuilder fieldBuilder) {
            fieldBuilder.op.accept(this, field);
            return self;
        }

        public T field(String field, Object value) {
            fields.put(field, value);
            return self;
        }

        public Input build() {
            return new Input(fields);
        }
    }

    public static class CustomInputBuilder extends InputBuilder<CustomInputBuilder> {

        private final JiraProperties jiraProperties;

        public CustomInputBuilder(JiraProperties jiraProperties) {
            this.jiraProperties = jiraProperties;
        }

        public CustomInputBuilder release(Version release) {
            return field(jiraProperties.getCustomfield().getRelease().getId(), release);
        }

        public CustomInputBuilder blocked(boolean value) {
            String yesOptionId = jiraProperties.getCustomfield().getBlocked().getYesOptionId().toString();
            return field(jiraProperties.getCustomfield().getBlocked().getId(), byIds(value ? yesOptionId : null));
        }

        public CustomInputBuilder lastBlockReason(String lastBlockReason) {
            return field(jiraProperties.getCustomfield().getLastBlockReason().getId(), lastBlockReason);
        }

        public CustomInputBuilder coAssignees(FieldBuilder fieldBuilder) {
            return field(jiraProperties.getCustomfield().getCoAssignees().getId(), fieldBuilder);
        }

        public CustomInputBuilder classOfService(FieldBuilder fieldBuilder) {
            return field(jiraProperties.getCustomfield().getClassOfService().getId(), fieldBuilder);
        }
    }

    public static class FieldBuilder {

        protected final BiConsumer<InputBuilder<?>, String> op;

        private FieldBuilder(BiConsumer<InputBuilder<?>, String> op) {
            this.op = op;
        }

        public static FieldBuilder byId(long id) {
            return byId(Long.toString(id));
        }

        public static FieldBuilder byId(String id) {
            return new FieldBuilder((builder, field) ->
                    builder.fields.put(field, singletonMap("id", id))
            );
        }

        public static FieldBuilder byIds(String... ids) {
            return byIds(asList(ids));
        }

        public static FieldBuilder byIds(Collection<String> ids) {
            return new FieldBuilder((builder, field) ->
                    builder.fields.put(field, ids.stream()
                            .map(id -> singletonMap("id", id))
                            .collect(Collectors.toList()))
            );
        }

        public static FieldBuilder byKey(String key) {
            return new FieldBuilder(
                    (builder, field) -> builder.fields.put(field, singletonMap("key", key))
            );
        }

        public static FieldBuilder byName(String name) {
            return new FieldBuilder((builder, field) ->
                    builder.fields.put(field, singletonMap("name", name))
            );
        }

        public static FieldBuilder byNames(String... names) {
            return byNames(asList(names));
        }

        public static FieldBuilder byNames(Collection<String> names) {
            return new FieldBuilder((builder, field) ->
                    builder.fields.put(field, names.stream()
                            .map(name -> singletonMap("name", name))
                            .collect(Collectors.toList()))
            );
        }

        public static FieldBuilder byValue(String value) {
            return new FieldBuilder((builder, field) ->
                    builder.fields.put(field, singletonMap("value", value))
            );
        }
    }

    public static class LinkInput {

        public final Object type;
        public final Object inwardIssue;
        public final Object outwardIssue;

        public static LinkInputBuilder builder() {
            return new LinkInputBuilder();
        }

        public LinkInput(Object type, Object inward, Object outward) {
            this.type = type;
            this.inwardIssue = inward;
            this.outwardIssue = outward;
        }
    }

    public static class LinkInputBuilder {
        private String type;
        private String fromIssueKey;
        private String toIssueKey;

        public LinkInputBuilder type(String type) {
            this.type = type;
            return this;
        }

        public LinkInputBuilder from(String issueKey) {
            this.fromIssueKey = issueKey;
            return this;
        }

        public LinkInputBuilder to(String issueKey) {
            this.toIssueKey = issueKey;
            return this;
        }

        public LinkInput build() {
            return new LinkInput(singletonMap("name", type), singletonMap("key", fromIssueKey), singletonMap("key", toIssueKey));
        }
    }
}
