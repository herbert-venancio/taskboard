package objective.taskboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import objective.taskboard.issue.IssuesUpdateEvent;

@Controller
public class IssueUpdateWebSocketController {
    private SimpMessagingTemplate template;
    
    @Autowired
    public IssueUpdateWebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }
    
    @EventListener
    public void handleUpdates(IssuesUpdateEvent event) {
        template.convertAndSend("/issues/updates", event.updates);
    }
}
