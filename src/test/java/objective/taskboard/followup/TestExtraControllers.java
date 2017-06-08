package objective.taskboard.followup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestExtraControllers {

    @Autowired
    private FollowupDataProvider followupProvider;

    @RequestMapping("followup")
    public String followup() {
        List<FollowUpData> jiraData = followupProvider.getJiraData();
        
        StringBuilder sb = new StringBuilder();
        for (FollowUpData followUpData : jiraData) {
            sb.append(followUpData);
        }
        return sb.toString();
    }
}
