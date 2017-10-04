package objective.taskboard.testUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import objective.taskboard.utils.Clock;

@Component
public class SystemClockMock implements Clock {

    @Override
    public Instant now() {
        return ZonedDateTime.of(2017, 6, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    }
}
