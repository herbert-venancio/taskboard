package objective.taskboard.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class StrategicalWebMvcConfiguration extends WebMvcConfigurerAdapter {

    private static final String ANGULAR_APP_PATH = "/app-strategical-static/";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/strategical-dashboard/**").setViewName("redirect:/followup-dashboard/");

        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/app-strategical-static-nocache/**")
            .addResourceLocations("classpath:" + ANGULAR_APP_PATH)
            .setCacheControl(CacheControl.noCache());

        registry.addResourceHandler("/app-strategical-static/**")
            .addResourceLocations("classpath:" + ANGULAR_APP_PATH)
            .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));
    }
}
