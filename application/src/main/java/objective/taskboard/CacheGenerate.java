package objective.taskboard;

import objective.taskboard.followup.FollowUpHistoryKeeper;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.issueBuffer.IssueBufferState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication(
    exclude = {
        EmbeddedServletContainerAutoConfiguration.class, WebMvcAutoConfiguration.class, SecurityAutoConfiguration.class
    }
)
@EnableWebSecurity
@ConditionalOnNotWebApplication
public class CacheGenerate implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacheGenerate.class);

    @Autowired private ConfigurableApplicationContext context;
    @Autowired private IssueBufferService issueBufferService;

    @Bean
    @Primary
    public static FollowUpHistoryKeeper disablingFollowUpHistoryKeeper() {
        return new FollowUpHistoryKeeper(null ) {
            @Override
            public void initialize() {
                return;
            }
        };
    }


    public static void main(String[] args ) {
        log.info( "Running the CacheGenerate!" );
        SpringApplication app = new SpringApplication(CacheGenerate.class);
        app.setWebEnvironment(false);
        app.run(args);
    }

    @Override
    public void run(String... strings) throws Exception {
        try {
            assert(issueBufferService.getState() == IssueBufferState.ready);
            SpringApplication.exit( context );

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}