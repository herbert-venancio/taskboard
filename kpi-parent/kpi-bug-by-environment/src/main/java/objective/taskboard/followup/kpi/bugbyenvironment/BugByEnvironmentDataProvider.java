package objective.taskboard.followup.kpi.bugbyenvironment;

import static objective.taskboard.followup.WeekRangeNormalizer.normalizeRangeWithWeekStartAndEndDays;
import static objective.taskboard.followup.WeekRangeNormalizer.splitByWeek;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;
import objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentProperties;
import objective.taskboard.followup.kpi.services.KpiDataService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.utils.RangeUtils;

@Service
public class BugByEnvironmentDataProvider {

    private final KpiDataService kpiDataService;
    private final MetadataService metadataService;
    private final KpiBugByEnvironmentProperties properties;
    private final ProjectService projectService;
    
    @Autowired
    public BugByEnvironmentDataProvider(
            KpiDataService kpiDataService, 
            MetadataService metadataService,
            KpiBugByEnvironmentProperties properties,
            ProjectService projectService) {
        this.kpiDataService = kpiDataService;
        this.metadataService = metadataService;
        this.properties = properties;
        this.projectService = projectService;
    }
    
    public List<BugByEnvironmentDataPoint> getDataSet(String projectKey, ZoneId timezone){
        List<IssueKpi> issues = kpiDataService.getAllIssuesFromCurrentState(projectKey, timezone);
        
        BugCounterCalculator bugCounter = new BugCounterCalculator(metadataService, properties, issues); 
        
        return getWeeks(projectKey,timezone)
                .map(week -> new WeekPointTransformer(bugCounter, week))
                .map(WeekPointTransformer::getDataPoints)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }
    
    private Stream<KpiWeekRange> getWeeks(String projectKey, ZoneId timezone){
        ProjectFilterConfiguration project = projectService.getTaskboardProjectOrCry(projectKey);
        
        Range<LocalDate> projectRange = normalizeRangeWithWeekStartAndEndDays(getRangeOrCry(project), DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        Stream<Range<LocalDate>> weeksRanges = splitByWeek(projectRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        return weeksRanges.map(w -> new KpiWeekRange(w, timezone));   
    }
    
    private Range<LocalDate> getRangeOrCry(ProjectFilterConfiguration project) {
        Optional<LocalDate> opStartDate = project.getStartDate();
        Optional<LocalDate> opDeliveryDate = project.getDeliveryDate();
        if (!opStartDate.isPresent() || !opDeliveryDate.isPresent())
            throw new ProjectDatesNotConfiguredException();

        LocalDate startDate = opStartDate.get();
        LocalDate deliveryDate = opDeliveryDate.get();
        return RangeUtils.between(startDate, deliveryDate);
    }
    
    private class WeekPointTransformer {
        
        private BugCounterCalculator bugCounter;
        private KpiWeekRange weekRange;
        private Instant datePoint;

        private WeekPointTransformer(BugCounterCalculator bugCounter, KpiWeekRange weekRange) {
            this.bugCounter = bugCounter;
            this.weekRange = weekRange;
            this.datePoint = weekRange.getFirstDay().toInstant();
        }
        
        private Stream<BugByEnvironmentDataPoint> getDataPoints(){
            return bugCounter.getBugsCategorizedOnWeek(weekRange).entrySet().stream()
                    .map(this::mapDataPoint);
        }
        
        private BugByEnvironmentDataPoint mapDataPoint(Entry<String,Long> entryValue) {
            String bugCategory = entryValue.getKey();
            Long bugsCount = entryValue.getValue();
            return new BugByEnvironmentDataPoint(datePoint, bugCategory, bugsCount);
        }
    }
    
}
