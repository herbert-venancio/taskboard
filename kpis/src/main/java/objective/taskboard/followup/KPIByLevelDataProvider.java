package objective.taskboard.followup;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import objective.taskboard.followup.kpi.services.KpiDataService;

public abstract class KPIByLevelDataProvider<C,D> {

    @Autowired
    private KpiDataService kpiService;
    
    public C getDataSet(String projectKey, String level, ZoneId timezone) {

        FollowUpSnapshot followupSnapshot = kpiService.getSnapshotFromCurrentState(projectKey, ZoneId.systemDefault());
        final FollowUpTimeline timeline = followupSnapshot.getTimeline();
        
        FollowUpData followupData = followupSnapshot.getData();
        
        final Level selectedLevel = Level.valueOf(level.toUpperCase());
        List<D> dataSets = getDataSetsByLevel(followupData,selectedLevel);
        
        return transform(dataSets,timeline,timezone);
    }

    private List<D> getDataSetsByLevel(FollowUpData followupData, Level level) {
        final List<D> dss = getDataSets(followupData);
        
        return (level == Level.ALL) ? dss : Arrays.asList(dss.get(level.index));
    }

    protected abstract List<D> getDataSets(FollowUpData followupData);

    protected abstract C transform(List<D> dataSets, FollowUpTimeline timeline, ZoneId timezone); 

    
    private enum Level {
        ALL(-1)
        , DEMAND(0)
        , FEATURE(1)
        , SUBTASK(2);

        public final int index;

        Level(int index) {
            this.index = index;
        }
        
    }
    
}
