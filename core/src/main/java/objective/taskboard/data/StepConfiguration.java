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
package objective.taskboard.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import objective.taskboard.domain.Step;

public class StepConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final int order;
    private final double weight;
    private final String step;
    private final String color;
    private final boolean showHeader;
    private final List<IssuesConfiguration> issuesConfiguration;

    public static StepConfiguration from(Step step) {
        return new StepConfiguration(step.getId(), step.getOrdem(), step.getWeight(), step.getName(), step.getColor(), step.getShowHeader(),
                new ArrayList<>());
    }

    public void addIssueConfiguration(IssuesConfiguration issue) {
        issuesConfiguration.add(issue);
    }

    public long getId() {
        return this.id;
    }

    public int getOrder() {
        return this.order;
    }

    public double getWeight() {
        return this.weight;
    }

    public String getStep() {
        return this.step;
    }

    public String getColor() {
        return this.color;
    }

    public boolean isShowHeader() {
        return this.showHeader;
    }

    public List<IssuesConfiguration> getIssuesConfiguration() {
        return this.issuesConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepConfiguration that = (StepConfiguration) o;

        if (id != that.id) return false;
        if (order != that.order) return false;
        if (Double.compare(that.weight, weight) != 0) return false;
        if (showHeader != that.showHeader) return false;
        if (step != null ? !step.equals(that.step) : that.step != null) return false;
        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        return issuesConfiguration != null ? issuesConfiguration.equals(that.issuesConfiguration) : that.issuesConfiguration == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + order;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (step != null ? step.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (showHeader ? 1 : 0);
        result = 31 * result + (issuesConfiguration != null ? issuesConfiguration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StepConfiguration{" +
                "id=" + id +
                ", order=" + order +
                ", weight=" + weight +
                ", step='" + step + '\'' +
                ", color='" + color + '\'' +
                ", showHeader=" + showHeader +
                ", issuesConfiguration=" + issuesConfiguration +
                '}';
    }

    @java.beans.ConstructorProperties({"id", "order", "weight", "step", "color", "showHeader", "issuesConfiguration"})
    private StepConfiguration(final long id, final int order, final double weight, final String step, final String color, final boolean showHeader, final List<IssuesConfiguration> issuesConfiguration) {
        this.id = id;
        this.order = order;
        this.weight = weight;
        this.step = step;
        this.color = color;
        this.showHeader = showHeader;
        this.issuesConfiguration = issuesConfiguration;
    }
}
