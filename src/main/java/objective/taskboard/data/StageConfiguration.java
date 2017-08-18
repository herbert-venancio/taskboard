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

import objective.taskboard.domain.Stage;

public class StageConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final int order;
    private final String stage;
    private final String color;
    private final double weight;
    private final boolean showHeader;
    private final List<StepConfiguration> steps;

    public static StageConfiguration from(Stage stage) {
        return new StageConfiguration(stage.getId(), stage.getOrdem(), stage.getName(), stage.getColor(), stage.getWeight(),
                stage.isShowHeader(), new ArrayList<>());
    }

    public void addStepConfiguration(StepConfiguration step) {
        steps.add(step);
    }

    public long getId() {
        return this.id;
    }

    public int getOrder() {
        return this.order;
    }

    public String getStage() {
        return this.stage;
    }

    public String getColor() {
        return this.color;
    }

    public double getWeight() {
        return this.weight;
    }

    public boolean isShowHeader() {
        return this.showHeader;
    }

    public List<StepConfiguration> getSteps() {
        return this.steps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StageConfiguration that = (StageConfiguration) o;

        if (id != that.id) return false;
        if (order != that.order) return false;
        if (Double.compare(that.weight, weight) != 0) return false;
        if (showHeader != that.showHeader) return false;
        if (stage != null ? !stage.equals(that.stage) : that.stage != null) return false;
        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        return steps != null ? steps.equals(that.steps) : that.steps == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + order;
        result = 31 * result + (stage != null ? stage.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (showHeader ? 1 : 0);
        result = 31 * result + (steps != null ? steps.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StageConfiguration{" +
                "id=" + id +
                ", order=" + order +
                ", stage='" + stage + '\'' +
                ", color='" + color + '\'' +
                ", weight=" + weight +
                ", showHeader=" + showHeader +
                ", steps=" + steps +
                '}';
    }

    @java.beans.ConstructorProperties({"id", "order", "stage", "color", "weight", "showHeader", "steps"})
    private StageConfiguration(final long id, final int order, final String stage, final String color, final double weight, final boolean showHeader, final List<StepConfiguration> steps) {
        this.id = id;
        this.order = order;
        this.stage = stage;
        this.color = color;
        this.weight = weight;
        this.showHeader = showHeader;
        this.steps = steps;
    }
}
