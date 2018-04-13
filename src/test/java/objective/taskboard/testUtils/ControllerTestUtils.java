package objective.taskboard.testUtils;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

import java.time.LocalDate;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import objective.taskboard.domain.serializer.TaskboardJacksonModule;

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

}
