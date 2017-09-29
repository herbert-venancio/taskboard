package objective.taskboard.utils;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod", "dev"})
public class LocalDateTimeProvider implements LocalDateTimeProviderInterface {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
