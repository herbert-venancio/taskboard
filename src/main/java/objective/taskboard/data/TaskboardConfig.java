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

public class TaskboardConfig {

    private String levelDesc;
    private String levelOrder;
    private String levelWeight;
    private String stage;
    private String stageOrder;
    private String step;
    private String stepOrder;
    private String weight;
    private String stageWeight;
    private long issueType;
    private String status;
    private String color;
    private String levelShowHeader;
    private String stageShowHeader;
    private String stepShowHeader;

    public static TaskboardConfig from(String levelDesc, String levelOrder, String levelWeight, String stage,
                                       String stageOrder, String step, String stepOrder, String weight, String stageWeight, long issueType,
                                       String status, String color, String levelShowHeader, String stageShowHeader, String stepShowHeader) {

        return new TaskboardConfig(levelDesc, levelOrder, levelWeight, stage, stageOrder, step, stepOrder, weight,
                stageWeight, issueType, status, color, levelShowHeader, stageShowHeader, stepShowHeader);
    }

    public String getLevelDesc() {
        return this.levelDesc;
    }

    public String getLevelOrder() {
        return this.levelOrder;
    }

    public String getLevelWeight() {
        return this.levelWeight;
    }

    public String getStage() {
        return this.stage;
    }

    public String getStageOrder() {
        return this.stageOrder;
    }

    public String getStep() {
        return this.step;
    }

    public String getStepOrder() {
        return this.stepOrder;
    }

    public String getWeight() {
        return this.weight;
    }

    public String getStageWeight() {
        return this.stageWeight;
    }

    public long getIssueType() {
        return this.issueType;
    }

    public String getStatus() {
        return this.status;
    }

    public String getColor() {
        return this.color;
    }

    public String getLevelShowHeader() {
        return this.levelShowHeader;
    }

    public String getStageShowHeader() {
        return this.stageShowHeader;
    }

    public String getStepShowHeader() {
        return this.stepShowHeader;
    }

    public void setLevelDesc(final String levelDesc) {
        this.levelDesc = levelDesc;
    }

    public void setLevelOrder(final String levelOrder) {
        this.levelOrder = levelOrder;
    }

    public void setLevelWeight(final String levelWeight) {
        this.levelWeight = levelWeight;
    }

    public void setStage(final String stage) {
        this.stage = stage;
    }

    public void setStageOrder(final String stageOrder) {
        this.stageOrder = stageOrder;
    }

    public void setStep(final String step) {
        this.step = step;
    }

    public void setStepOrder(final String stepOrder) {
        this.stepOrder = stepOrder;
    }

    public void setWeight(final String weight) {
        this.weight = weight;
    }

    public void setStageWeight(final String stageWeight) {
        this.stageWeight = stageWeight;
    }

    public void setIssueType(final long issueType) {
        this.issueType = issueType;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public void setLevelShowHeader(final String levelShowHeader) {
        this.levelShowHeader = levelShowHeader;
    }

    public void setStageShowHeader(final String stageShowHeader) {
        this.stageShowHeader = stageShowHeader;
    }

    public void setStepShowHeader(final String stepShowHeader) {
        this.stepShowHeader = stepShowHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskboardConfig that = (TaskboardConfig) o;

        if (issueType != that.issueType) return false;
        if (levelDesc != null ? !levelDesc.equals(that.levelDesc) : that.levelDesc != null) return false;
        if (levelOrder != null ? !levelOrder.equals(that.levelOrder) : that.levelOrder != null) return false;
        if (levelWeight != null ? !levelWeight.equals(that.levelWeight) : that.levelWeight != null) return false;
        if (stage != null ? !stage.equals(that.stage) : that.stage != null) return false;
        if (stageOrder != null ? !stageOrder.equals(that.stageOrder) : that.stageOrder != null) return false;
        if (step != null ? !step.equals(that.step) : that.step != null) return false;
        if (stepOrder != null ? !stepOrder.equals(that.stepOrder) : that.stepOrder != null) return false;
        if (weight != null ? !weight.equals(that.weight) : that.weight != null) return false;
        if (stageWeight != null ? !stageWeight.equals(that.stageWeight) : that.stageWeight != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        if (levelShowHeader != null ? !levelShowHeader.equals(that.levelShowHeader) : that.levelShowHeader != null)
            return false;
        if (stageShowHeader != null ? !stageShowHeader.equals(that.stageShowHeader) : that.stageShowHeader != null)
            return false;
        return stepShowHeader != null ? stepShowHeader.equals(that.stepShowHeader) : that.stepShowHeader == null;
    }

    @Override
    public int hashCode() {
        int result = levelDesc != null ? levelDesc.hashCode() : 0;
        result = 31 * result + (levelOrder != null ? levelOrder.hashCode() : 0);
        result = 31 * result + (levelWeight != null ? levelWeight.hashCode() : 0);
        result = 31 * result + (stage != null ? stage.hashCode() : 0);
        result = 31 * result + (stageOrder != null ? stageOrder.hashCode() : 0);
        result = 31 * result + (step != null ? step.hashCode() : 0);
        result = 31 * result + (stepOrder != null ? stepOrder.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (stageWeight != null ? stageWeight.hashCode() : 0);
        result = 31 * result + (int) (issueType ^ (issueType >>> 32));
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (levelShowHeader != null ? levelShowHeader.hashCode() : 0);
        result = 31 * result + (stageShowHeader != null ? stageShowHeader.hashCode() : 0);
        result = 31 * result + (stepShowHeader != null ? stepShowHeader.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaskboardConfig{" +
                "levelDesc='" + levelDesc + '\'' +
                ", levelOrder='" + levelOrder + '\'' +
                ", levelWeight='" + levelWeight + '\'' +
                ", stage='" + stage + '\'' +
                ", stageOrder='" + stageOrder + '\'' +
                ", step='" + step + '\'' +
                ", stepOrder='" + stepOrder + '\'' +
                ", weight='" + weight + '\'' +
                ", stageWeight='" + stageWeight + '\'' +
                ", issueType=" + issueType +
                ", status='" + status + '\'' +
                ", color='" + color + '\'' +
                ", levelShowHeader='" + levelShowHeader + '\'' +
                ", stageShowHeader='" + stageShowHeader + '\'' +
                ", stepShowHeader='" + stepShowHeader + '\'' +
                '}';
    }

    public TaskboardConfig() {
    }

    @java.beans.ConstructorProperties({"levelDesc", "levelOrder", "levelWeight", "stage", "stageOrder", "step", "stepOrder", "weight", "stageWeight", "issueType", "status", "color", "levelShowHeader", "stageShowHeader", "stepShowHeader"})
    private TaskboardConfig(final String levelDesc, final String levelOrder, final String levelWeight, final String stage, final String stageOrder, final String step, final String stepOrder, final String weight, final String stageWeight, final long issueType, final String status, final String color, final String levelShowHeader, final String stageShowHeader, final String stepShowHeader) {
        this.levelDesc = levelDesc;
        this.levelOrder = levelOrder;
        this.levelWeight = levelWeight;
        this.stage = stage;
        this.stageOrder = stageOrder;
        this.step = step;
        this.stepOrder = stepOrder;
        this.weight = weight;
        this.stageWeight = stageWeight;
        this.issueType = issueType;
        this.status = status;
        this.color = color;
        this.levelShowHeader = levelShowHeader;
        this.stageShowHeader = stageShowHeader;
        this.stepShowHeader = stepShowHeader;
    }
}
