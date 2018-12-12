package objective.taskboard.config.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TaskboardJacksonModuleTest {

    private ObjectMapper objectMapper;

    private final ZonedDateTime sampleDate = ZonedDateTime.of(2013, 12, 11, 10, 9, 8, 0, ZoneId.systemDefault());

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new TaskboardJacksonModule());
    }

    @Test
    public void instantSerialization() throws JsonProcessingException {
        Instant sample = sampleDate.toInstant();
        String expected = "1386763748000";
        String actual = objectMapper.writeValueAsString(sample);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void instantDeserialization() throws IOException {
        String sample = "1386763748000";
        Instant expected = sampleDate.toInstant();
        Instant actual = objectMapper.readValue(sample, Instant.class);
        assertThat(actual).isEqualTo(expected);
    }
}