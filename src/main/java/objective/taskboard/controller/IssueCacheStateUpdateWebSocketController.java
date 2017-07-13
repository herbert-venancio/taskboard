package objective.taskboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import objective.taskboard.issueBuffer.IssueCacheUpdateEvent;

@Controller
public class IssueCacheStateUpdateWebSocketController {
    @Autowired
    private SimpMessagingTemplate template;
    
    @EventListener
    public void handleUpdates(IssueCacheUpdateEvent event) {
        template.convertAndSend("/cache-state/updates", event.getState());
    }
}
