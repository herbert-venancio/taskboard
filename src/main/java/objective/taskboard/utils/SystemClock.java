package objective.taskboard.utils;

import java.time.Instant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod", "dev"})
public class SystemClock implements Clock {

    public Instant now() {
        return Instant.now();
    }
}
