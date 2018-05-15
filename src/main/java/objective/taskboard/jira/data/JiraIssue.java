package objective.taskboard.jira.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

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

        public static InputBuilder builder() {
            return new InputBuilder();
        }

        public final Map<String, Object> fields;

        public Input(Map<String, Object> fields) {
            this.fields = fields;
        }
    }

    public static class InputBuilder {

        private InputBuilder() {}

        protected final Map<String, Object> fields = Maps.newLinkedHashMap();

        public FieldBuilder field(String field) {
            return new FieldBuilder(this, field);
        }

        public Input build() {
            return new Input(fields);
        }
    }

    public static class FieldBuilder {
        private final InputBuilder builder;
        private final String field;

        public FieldBuilder(InputBuilder builder, String field) {
            this.builder = builder;
            this.field = field;
        }

        public InputBuilder byId(long id) {
            return byId(Long.toString(id));
        }

        public InputBuilder byId(String id) {
            builder.fields.put(field, singletonMap("id", id));
            return builder;
        }

        public InputBuilder byIds(String... ids) {
            builder.fields.put(field, Arrays.stream(ids).map(id -> singletonMap("id", id)));
            return builder;
        }

        public InputBuilder byKey(String key) {
            builder.fields.put(field, singletonMap("key", key));
            return builder;
        }

        public InputBuilder byName(String name) {
            builder.fields.put(field, singletonMap("name", name));
            return builder;
        }

        public InputBuilder byNames(String... names) {
            return byNames(asList(names));
        }

        public InputBuilder byNames(Collection<String> names) {
            builder.fields.put(field, names.stream()
                    .map(name -> singletonMap("name", name))
                    .collect(Collectors.toList()));
            return builder;
        }

        public InputBuilder byValue(String value) {
            builder.fields.put(field, singletonMap("value", value));
            return builder;
        }

        public InputBuilder set(Object value) {
            builder.fields.put(field, value);
            return builder;
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
