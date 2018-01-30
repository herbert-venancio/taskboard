package objective.taskboard.followup.impl;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.followup.FollowUpScopeByTypeDataItem;
import objective.taskboard.followup.FollowUpScopeByTypeDataSet;
import objective.taskboard.followup.FollowupCluster;
import objective.taskboard.followup.FollowupClusterProvider;
import objective.taskboard.followup.FollowupDataProvider;
import objective.taskboard.followup.FromJiraRowService;

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

    @Autowired
    private FollowUpFacade followUpFacade;

    @Autowired
    private FollowupClusterProvider followUpClusterProvider;

    @Autowired
    private FromJiraRowService rowService;

    public FollowUpScopeByTypeDataSet getScopeByTypeData(String projectKey, String date, ZoneId zoneId) {

        final Map<String, Double> map = initTypes();

        FollowUpDataSnapshot snapshot = getSnapshot(projectKey, date, zoneId);
        snapshot.forEachRow(r -> {
            Double effortEstimate = r.calcutatedData.getEffortEstimate();
            if (rowService.isIntangible(r.rowData) && rowService.isDone(r.rowData))
                sum(map, INTANGIBLE_DONE, effortEstimate);
            else if (rowService.isIntangible(r.rowData) && rowService.isBacklog(r.rowData))
                sum(map, INTANGIBLE_BACKLOG, effortEstimate);
            else if (rowService.isNewScope(r.rowData) && rowService.isDone(r.rowData))
                sum(map, NEW_SCOPE_DONE, effortEstimate);
            else if (rowService.isNewScope(r.rowData) && rowService.isBacklog(r.rowData))
                sum(map, NEW_SCOPE_BACKLOG, effortEstimate);
            else if (rowService.isRework(r.rowData) && rowService.isDone(r.rowData))
                sum(map, REWORK_DONE, effortEstimate);
            else if (rowService.isRework(r.rowData) && rowService.isBacklog(r.rowData))
                sum(map, REWORK_BACKLOG, effortEstimate);
            else if (rowService.isBaselineDone(r.rowData))
                sum(map, BASELINE_DONE, effortEstimate);
            else if (rowService.isBaselineBacklog(r.rowData))
                sum(map, BASELINE_BACKLOG, effortEstimate);
        });

        return transform(map, projectKey, date, zoneId);
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

    private FollowUpScopeByTypeDataSet transform(Map<String, Double> map, String projectKey, String date, ZoneId zoneId) {
        FollowUpScopeByTypeDataSet dataSet = new FollowUpScopeByTypeDataSet();
        dataSet.total = map.values().stream().mapToDouble(i -> i).sum();
        dataSet.projectKey = projectKey;
        dataSet.date = date;
        dataSet.zoneId = zoneId.getId();

        Iterator<Entry<String, Double>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Entry<String, Double> entry = entries.next();
            dataSet.values.add(new FollowUpScopeByTypeDataItem(entry.getKey(), entry.getValue(), getPercent(entry.getValue(), dataSet.total)));
        }
        return dataSet;
    }

    private Double getPercent(Double value, Double total) {
        Double percent = (value / total) * 100D;
        return percent.isNaN() ? 0D : percent;
    }

    private FollowUpDataSnapshot getSnapshot(String projectKey, String date, ZoneId zoneId) {
        FollowupDataProvider provider = followUpFacade.getProvider(Optional.of(date));
        String[] projects = {projectKey};
        Optional<FollowupCluster> cluster = followUpClusterProvider.getForProject(projectKey);
        if (cluster.isPresent())
            return provider.getJiraData(cluster.get(), projects, zoneId);
        throw new IllegalArgumentException("Cluster for project " + projectKey + " not found.");
    }

}
