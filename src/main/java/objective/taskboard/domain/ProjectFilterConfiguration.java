/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

package objective.taskboard.domain;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "project_filter_configuration")
public class ProjectFilterConfiguration implements Serializable {
    private static final long serialVersionUID = 765599368694090438L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String projectKey;
    
    @Column
    private LocalDate startDate;//NOSONAR

    @Column
    private LocalDate deliveryDate;//NOSONAR

    @Column
    private Boolean isArchived;
    
    @Column(precision = 5, scale = 4)
    private BigDecimal riskPercentage = BigDecimal.ZERO;

    public Integer getId() {
        return id;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Boolean isArchived() {
        return isArchived != null ? isArchived : false;
    }

    public void setArchived(Boolean isArchived) {
        this.isArchived = isArchived;
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

    @OneToMany(fetch=FetchType.EAGER)
    @JoinColumn(name="projectKey", referencedColumnName="projectKey")
    private List<ProjectTeam> projectTeams;

    public List<Long> getTeamsIds() {
        return projectTeams.stream().map(el->el.getTeamId()).collect(Collectors.toList());
    }

}
