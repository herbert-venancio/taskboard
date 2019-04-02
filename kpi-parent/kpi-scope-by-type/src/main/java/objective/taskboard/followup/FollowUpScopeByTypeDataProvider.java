package objective.taskboard.followup;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.followup.kpi.KpiDataService;

@Service
public class FollowUpScopeByTypeDataProvider {

    public static final String INTANGIBLE_DONE = "Intangible Done";
    public static final String INTANGIBLE_BACKLOG = "Intangible Backlog";
    public static final String NEW_SCOPE_DONE = "New Scope Done";
    public static final String NEW_SCOPE_BACKLOG = "New Scope Backlog";
    public static final String REWORK_DONE = "Rework Done";
    public static final String REWORK_BACKLOG = "Rework Backlog";
    public static final String BASELINE_DONE = "Baseline Done";
    public static final String BASELINE_BACKLOG = "Baseline Backlog";

    private final KpiDataService kpiService;
    private final FromJiraRowService rowService;
    
    @Autowired
    public FollowUpScopeByTypeDataProvider(KpiDataService kpiService, FromJiraRowService rowService) {
        this.kpiService = kpiService;
        this.rowService = rowService;
    }

    public FollowUpScopeByTypeDataSet getScopeByTypeData(String projectKey, ZoneId zoneId)
            throws ClusterNotConfiguredException {

        final FollowUpSnapshot snapshot = kpiService.getSnapshotFromCurrentState(zoneId, projectKey);
        final Map<String, Double> map = initTypes();

        if (!snapshot.hasClusterConfiguration())
            throw new ClusterNotConfiguredException();
        
        for (FromJiraRowCalculation r : snapshot.getFromJiraRowCalculations()) {
            Double effortEstimate = r.getEffortEstimate();
            if (rowService.isIntangible(r.getRow()) && rowService.isDone(r.getRow()))
                sum(map, INTANGIBLE_DONE, effortEstimate);
            else if (rowService.isIntangible(r.getRow()) && rowService.isBacklog(r.getRow()))
                sum(map, INTANGIBLE_BACKLOG, effortEstimate);
            else if (rowService.isNewScope(r.getRow()) && rowService.isDone(r.getRow()))
                sum(map, NEW_SCOPE_DONE, effortEstimate);
            else if (rowService.isNewScope(r.getRow()) && rowService.isBacklog(r.getRow()))
                sum(map, NEW_SCOPE_BACKLOG, effortEstimate);
            else if (rowService.isRework(r.getRow()) && rowService.isDone(r.getRow()))
                sum(map, REWORK_DONE, effortEstimate);
            else if (rowService.isRework(r.getRow()) && rowService.isBacklog(r.getRow()))
                sum(map, REWORK_BACKLOG, effortEstimate);
            else if (rowService.isBaselineDone(r.getRow()))
                sum(map, BASELINE_DONE, effortEstimate);
            else if (rowService.isBaselineBacklog(r.getRow()))
                sum(map, BASELINE_BACKLOG, effortEstimate);
        }

        return transform(map, projectKey, zoneId);
    }

    private Map<String, Double> initTypes() {
        final Map<String, Double> map = new HashMap<>();
        map.put(INTANGIBLE_DONE, 0D);
        map.put(INTANGIBLE_BACKLOG, 0D);
        map.put(NEW_SCOPE_DONE, 0D);
        map.put(NEW_SCOPE_BACKLOG, 0D);
        map.put(REWORK_DONE, 0D);
        map.put(REWORK_BACKLOG, 0D);
        map.put(BASELINE_DONE, 0D);
        map.put(BASELINE_BACKLOG, 0D);
        return map;
    }

    private void sum(Map<String, Double> map, String type, Double effortEstimate) {
        Double actualEffortEstimate = map.get(type);
        map.put(type, actualEffortEstimate + effortEstimate);
    }

    private FollowUpScopeByTypeDataSet transform(Map<String, Double> map, String projectKey, ZoneId zoneId) {
        FollowUpScopeByTypeDataSet dataSet = new FollowUpScopeByTypeDataSet();
        dataSet.total = map.values().stream().mapToDouble(i -> i).sum();
        dataSet.projectKey = projectKey;
        dataSet.zoneId = zoneId.getId();

        Iterator<Entry<String, Double>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Entry<String, Double> entry = entries.next();
            dataSet.values.add(new FollowUpScopeByTypeDataItem(entry.getKey(), entry.getValue()));
        }
        return dataSet;
    }
}
