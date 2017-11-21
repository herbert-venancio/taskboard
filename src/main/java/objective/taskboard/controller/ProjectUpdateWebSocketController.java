package objective.taskboard.controller;

import objective.taskboard.data.ProjectsUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ProjectUpdateWebSocketController {

    private SimpMessagingTemplate template;

    @Autowired
    public ProjectUpdateWebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @EventListener
    public void handleUpdates(ProjectsUpdateEvent event) {
        template.convertAndSend("/topic/projects/updates", event.projects);
    }
}
