package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import objective.taskboard.followup.WipDataSet;
import objective.taskboard.followup.WipRow;
import objective.taskboard.jira.properties.StatusConfiguration;

@Service
public class WipKPIService extends KPIUsingStatusService<WipDataSet,WipRow>{
 
    @Override
    protected List<WipRow> makeRows(String[] statuses, Map<String, List<IssueStatusFlow>> issues, ZonedDateTime date) {
        List<WipRow> rows = new LinkedList<>();
        
        for (String status : statuses) {
          List<WipRow> wipRows = makeRows(issues, status, date);
          rows.addAll(wipRows);
      }
        return rows;
    }
    
    private List<WipRow> makeRows(Map<String, List<IssueStatusFlow>> issues, String status, ZonedDateTime date) {
        
        return issues.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .map(entry -> new WipRow(date, entry.getKey(), status, countIssuesInWip(entry.getValue(), date,status)))
                .collect(Collectors.toList());

    }

    private Long countIssuesInWip(List<IssueStatusFlow> issues, ZonedDateTime date,String status) {
        return issues.stream().filter(i -> i.isOnStatusOnDay(status, date)).count();
    }

    @Override
    protected StatusConfiguration getConfiguration() {
        return jiraProperties.getStatusCountingOnWip();
    }

    @Override
    protected WipDataSet makeDataSet(String type, List<WipRow> rows) {
        return new WipDataSet(type, rows);
    }
    
}
