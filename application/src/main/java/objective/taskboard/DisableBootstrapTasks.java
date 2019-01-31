package objective.taskboard;

import java.time.LocalDate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import objective.taskboard.followup.FollowUpSnapshot;
import objective.taskboard.followup.SynthesisSynchronizer;

@Configuration
@ConditionalOnExpression("#{(systemProperties['objective.taskboard.config.enableBootstrapTasks'] ?: 'false') eq 'false'}")
public class DisableBootstrapTasks {
    private static final Logger log = LoggerFactory.getLogger(DisableBootstrapTasks.class);
    private boolean syncSynthesisMessageDisplayed = false;
    
    @Primary
    @Bean
    public static FlywayMigrationStrategy migrationStratagy() {
        return flyway -> {
            log.warn("#################### Flyway migrations disabled #################### ");
        };
    }
    
    @Primary
    @Bean
    public SynthesisSynchronizer disableSynthesisSynchronizer() {
        return (Supplier<FollowUpSnapshot> lazySnapshotProvider, String projectKey, LocalDate date, boolean override) -> {
            if (syncSynthesisMessageDisplayed) return;
            log.warn("#################### Synthesis Syncronizer disabled #################### ");
            syncSynthesisMessageDisplayed = true;
        };
    }
}