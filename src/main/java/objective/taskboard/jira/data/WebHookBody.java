package objective.taskboard.jira.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebHookBody {
    public Long timestamp;
    public WebhookEvent webhookEvent;
    public Map<String, Object> user = new HashMap<>();
    public Map<String, Object> issue = new HashMap<>();
    public Version version;
    public Changelog changelog = new Changelog();
    public static class Changelog {
        public List<Map<String, Object>> items = new ArrayList<>();
    }
}
