package objective.taskboard.config;

import objective.taskboard.TaskboardProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;


@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    @Autowired
    TaskboardProperties taskboardProperties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler conditionalThreadPoolTaskScheduler;
        if (taskboardProperties.isExecutingApplication())
            conditionalThreadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        else
            conditionalThreadPoolTaskScheduler = new DisabledScheduler();


        conditionalThreadPoolTaskScheduler.setThreadNamePrefix("TaskBoardSchedule");
        conditionalThreadPoolTaskScheduler.initialize();

        scheduledTaskRegistrar.setTaskScheduler(conditionalThreadPoolTaskScheduler);
    }

    @Bean
    public ForkJoinPoolFactoryBean forkJoinPoolFactory() {
        ForkJoinPoolFactoryBean factory = new ForkJoinPoolFactoryBean();
        factory.setCommonPool(true);
        return factory;
    }

    @SuppressWarnings("serial")
	private class DisabledScheduler extends ThreadPoolTaskScheduler {
        @Override
        public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
            return null;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
            return null;
        }
    }
}
