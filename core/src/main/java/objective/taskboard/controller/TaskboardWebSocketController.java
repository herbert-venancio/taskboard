package objective.taskboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ws/taskboard-websocket")
public class TaskboardWebSocketController {

    @RequestMapping("ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok().build();
    }

}
