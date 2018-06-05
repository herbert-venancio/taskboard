package objective.taskboard.jira.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JSONObjectAdapterTest {

    private static final String COMPLEX_JSON = "{"
            + "\"n\":null"
            + ",\"b\":true"
            + ",\"d\":1.25"
            + ",\"s\":\"test\""
            + ",\"o\":{\"hello\":\"world\"}"
            + ",\"a\":[\"test\",1,true]"
            + "}";

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    @Test
    public void deserializeObject() throws IOException {
        Properties object = objectMapper.readValue(COMPLEX_JSON, Properties.class);
        assertThat(object.n).isNull();
        assertThat(object.b.object).isEqualTo(true);
        assertThat(object.d.object).isEqualTo(1.25);
        assertThat(object.s.object).isEqualTo("test");
        assertThat(object.o.object).isInstanceOf(JSONObject.class);
        assertThat(object.a.object).isInstanceOf(JSONArray.class);
    }

    public static class Properties {
        @JsonProperty
        JiraIssueDto.JSONObjectAdapter n;
        @JsonProperty
        JiraIssueDto.JSONObjectAdapter b;
        @JsonProperty
        JiraIssueDto.JSONObjectAdapter d;
        @JsonProperty
        JiraIssueDto.JSONObjectAdapter s;
        @JsonProperty
        JiraIssueDto.JSONObjectAdapter o;
        @JsonProperty
        JiraIssueDto.JSONObjectAdapter a;
    }
}