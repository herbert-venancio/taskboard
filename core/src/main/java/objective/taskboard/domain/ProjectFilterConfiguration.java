package objective.taskboard.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.project.ProjectDefaultTeamByIssueType;

@Entity
@Table(name = "project_filter_configuration")
public class ProjectFilterConfiguration extends TaskboardEntity implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ProjectFilterConfiguration.class);

    private static final long serialVersionUID = 765599368694090438L;

    private static final int DEFAULT_PROJECTION_TIMESPAN = 20;

    @Column
    private String projectKey;

    @Column
    private LocalDate startDate;//NOSONAR

    @Column
    private LocalDate deliveryDate;//NOSONAR

    @Column
    private Long defaultTeam;

    @Column(nullable = false)
    private boolean isArchived;

    @Column(precision = 5, scale = 4)
    private BigDecimal riskPercentage = BigDecimal.ZERO;

    @Column
    private Integer projectionTimespan;

    @Column
    private LocalDate baselineDate;//NOSONAR
    
    @Column
    private Long baseClusterId;

    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true, mappedBy="project")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ProjectDefaultTeamByIssueType> teamsByIssueTypes;

    protected ProjectFilterConfiguration() { //NOSONAR
        this.teamsByIssueTypes = new ArrayList<>();
    }

    public ProjectFilterConfiguration(String projectKey, Long defaultTeamId) {
        this.setDefaultTeam(defaultTeamId);
        this.setProjectKey(projectKey);
        this.teamsByIssueTypes = new ArrayList<>();
    }

    public Optional<Long> getTeamByIssueTypeId(Long issueTypeId) {
        return teamsByIssueTypes.stream()
                .filter(item -> item.getIssueTypeId().equals(issueTypeId))
                .map(ProjectDefaultTeamByIssueType::getTeamId)
                .findFirst();
    }

    public void addProjectTeamForIssueType(Long teamId, Long issueTypeId) {
        teamsByIssueTypes.add(new ProjectDefaultTeamByIssueType(this, teamId, issueTypeId));
    }

    public void removeDefaultTeamForIssueType(ProjectDefaultTeamByIssueType projectTeamByIssueType) {
        boolean wasRemoved = teamsByIssueTypes.removeIf(projectTeamByIssueType::equals);
        if (!wasRemoved)
            log.warn("removeDefaultTeamForIssueType: ProjectTeamByIssueType with id \""+ projectTeamByIssueType.getId() +"\" not found.");
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        if (isBlank(projectKey))
            throw new IllegalArgumentException("\"projectKey\" required");

        this.projectKey = projectKey.trim();
    }

    public void setStartDate(LocalDate startDate) {
        validateProjectDates(startDate, this.deliveryDate);
        this.startDate = startDate;
    }

    public Optional<LocalDate> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    public Optional<LocalDate> getDeliveryDate() {
        return Optional.ofNullable(deliveryDate);
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        validateProjectDates(this.startDate, deliveryDate);
        this.deliveryDate = deliveryDate;
    }

    private static void validateProjectDates(LocalDate startDate, LocalDate deliveryDate) {
        if (startDate != null && deliveryDate != null && startDate.isAfter(deliveryDate))
            throw new IllegalArgumentException("'startDate' should be before or equal to 'deliveryDate'");
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Integer getProjectionTimespan() {
        return projectionTimespan != null ? projectionTimespan : DEFAULT_PROJECTION_TIMESPAN;
    }

    public void setProjectionTimespan(Integer projectionTimespan) {
        if (projectionTimespan == null || projectionTimespan <= 0)
            throw new IllegalArgumentException("\"projectionTimespan\" must be a positive number");

        this.projectionTimespan = projectionTimespan;
    }

    public BigDecimal getRiskPercentage() {
        return riskPercentage;
    }

    public void setRiskPercentage(BigDecimal riskPercentage) {
        BigDecimal newValue = requireNonNull(riskPercentage).setScale(4, RoundingMode.HALF_EVEN);

        if (newValue.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Risk percentage should be between 0 and 1 (inclusive). Actual: " + newValue);

        this.riskPercentage = newValue;
    }

    public Long getDefaultTeam() {
        return defaultTeam;
    }

    public void setDefaultTeam(Long defaultTeamId) {
        notNull(defaultTeamId);
        this.defaultTeam = defaultTeamId;
    }

    public void setBaselineDate(LocalDate baselineDate) {
        this.baselineDate = baselineDate;
    }

    public Optional<LocalDate> getBaselineDate() {
        return Optional.ofNullable(baselineDate);
    }

    public Optional<Long> getBaseClusterId() {
        return Optional.ofNullable(baseClusterId);
    }

    public void setBaseClusterId(Long baseClusterId) {
        this.baseClusterId = baseClusterId;
    }

    public List<ProjectDefaultTeamByIssueType> getTeamsByIssueTypes() {
        return Collections.unmodifiableList(teamsByIssueTypes);
    }

    @Override
    public String toString() {
        return "ProjectFilterConfiguration [id=" + id + ", projectKey=" + projectKey + ", startDate=" + startDate
                + ", deliveryDate=" + deliveryDate + ", defaultTeam=" + defaultTeam + ", isArchived=" + isArchived
                + ", riskPercentage=" + riskPercentage + ", projectionTimespan=" + projectionTimespan
                + ", baselineDate=" + baselineDate + ", baseClusterId=" + baseClusterId + ", teamsByIssueTypes="
                + teamsByIssueTypes + "]";
    }

}
