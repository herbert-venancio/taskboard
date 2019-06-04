package objective.taskboard.testUtils;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;

@EnableAutoConfiguration(exclude = FlywayAutoConfiguration.class)
public class UIConfig {
    
}
