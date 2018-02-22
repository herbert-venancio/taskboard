package objective.taskboard.config;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/*
 * Utility class to publish events from classes that are not managed by spring.
 */
@Service
public class EventPublisherUtil implements ApplicationContextAware {

    private static ApplicationEventPublisher staticEventPublisher;
    
    @Autowired
    public ApplicationEventPublisher ep;

    @Override
    public synchronized void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        staticEventPublisher = ep;
    }

    /**
     * Publishes an application event.
     * 
     * @param e the event to publish
     */
    public static void publishEvent(ApplicationEvent e) {
        staticEventPublisher.publishEvent(e);
    }
}