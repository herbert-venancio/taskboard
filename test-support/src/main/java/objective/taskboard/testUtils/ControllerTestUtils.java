package objective.taskboard.testUtils;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import objective.taskboard.config.converter.TaskboardJacksonModule;

public class ControllerTestUtils {

    public static MockMvc getDefaultMockMvc(Object... controllers) {
        return MockMvcBuilders
                    .standaloneSetup(controllers)
                    .setMessageConverters(getTaskboardJacksonModule())
                    .build();
    }

    private static MappingJackson2HttpMessageConverter getTaskboardJacksonModule() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new TaskboardJacksonModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    public static String getLocalDateSerialized(LocalDate date) throws NumberFormatException, JsonProcessingException {
        return getTaskboardJacksonModule().getObjectMapper().writeValueAsString(date);
    }

    public static String asJsonStringResponseOnly(String content) {
        return "\"" + escapeJava(content) + "\"";
    }

    public static class AssertResponse {

        private ResponseEntity<?> response;

        private AssertResponse(ResponseEntity<?> response) {
            this.response = response;
        }

        public static AssertResponse of(ResponseEntity<?> response) {
            return new AssertResponse(response);
        }

        public AssertResponse httpStatus(HttpStatus expectedHttpStatus) {
            assertEquals(expectedHttpStatus.value(), response.getStatusCodeValue());
            return this;
        }

        public AssertResponse bodyClass(Class<?> expectedInstanceClass) {
            assertTrue(expectedInstanceClass.isInstance(response.getBody()));
            return this;
        }

        public AssertResponse bodyClassWhenList(int index, Class<?> expectedInstanceOfElement) {
            List<?> body = (List<?>) response.getBody();
            assertTrue(expectedInstanceOfElement.isInstance(body.get(index)));

            return this;
        }

        public AssertResponse bodyAsString(String expectedBodyString) {
            assertEquals(expectedBodyString, response.getBody().toString());
            return this;
        }

        public AssertResponse bodyAsJson(String expectedBodyJson) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new TaskboardJacksonModule());
                ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
                Object expectedBody = mapper.readValue(expectedBodyJson, Object.class);

                String bodyJsonFormatted = writer.writeValueAsString(response.getBody());
                String expectedBodyJsonFormatted = writer.writeValueAsString(expectedBody);

                assertEquals(expectedBodyJsonFormatted, bodyJsonFormatted);
                return this;
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error while generating \"json\"", e);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error while generating \"object\"", e);
            }
        }

        public AssertResponse emptyBody() {
            assertNull(response.getBody());
            return this;
        }

        public ResponseEntity<?> getResponse() {
            return response;
        }

    }

}
