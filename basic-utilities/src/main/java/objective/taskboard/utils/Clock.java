package objective.taskboard.utils;

import java.time.Instant;

public interface Clock {
    public Instant now();
}