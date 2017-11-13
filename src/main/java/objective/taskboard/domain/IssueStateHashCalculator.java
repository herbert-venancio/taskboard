package objective.taskboard.domain;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import objective.taskboard.data.Issue;

@Service
public class IssueStateHashCalculator {

    private static final Logger log = LoggerFactory.getLogger(IssueStateHashCalculator.class);

    private ObjectWriter writer;

    @PostConstruct
    public void setupWriter() {
        ObjectMapper objectMapper = new ObjectMapper();
        FilterProvider filters = new SimpleFilterProvider().addFilter("stateHash", SimpleBeanPropertyFilter.serializeAllExcept("stateHash"));
        writer = objectMapper
                .setFilterProvider(filters)
                .addMixIn(Issue.class, PropertyFilterMixIn.class)
                .writer();
    }

    public int calculateHash(Issue issue) {
        try {
            return writer.writeValueAsString(issue).hashCode();
        } catch (JsonProcessingException ex) {
            log.error("Could not calculate issue state hash", ex);
        }
        return issue.hashCode();
    }

    @JsonFilter("stateHash")
    static class PropertyFilterMixIn {}
}
