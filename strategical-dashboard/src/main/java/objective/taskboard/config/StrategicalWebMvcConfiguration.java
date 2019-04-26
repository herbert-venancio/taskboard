package objective.taskboard.config;

import java.util.concurrent.TimeUnit;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

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
        registry.addViewController("/strategical-dashboard/**").setViewName("forward:/app-strategical-static-nocache/index.html");

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
