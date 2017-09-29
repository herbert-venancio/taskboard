package objective.taskboard.testUtils;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import objective.taskboard.utils.LocalDateTimeProviderInterface;

@Component
public class LocalDateTimeProviderMock implements LocalDateTimeProviderInterface {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.of(2017, 6, 1, 0, 0);
    }
}
