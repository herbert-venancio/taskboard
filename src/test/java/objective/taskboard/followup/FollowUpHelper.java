package objective.taskboard.followup;

import java.util.Collections;
import java.util.List;

public class FollowUpHelper {

    public static FollowUpData getFollowUpDataDefault() {
        FollowUpData followUpData = new FollowUpData();
        followUpData.planningType = "Ballpark";
        followUpData.project = "PROJECT TEST";
        followUpData.demandType = "Demand";
        followUpData.demandStatus = "Doing";
        followUpData.demandId = 1L;
        followUpData.demandNum = "I-1";
        followUpData.demandSummary = "Summary Demand";
        followUpData.demandDescription = "Description Demand";
        followUpData.taskType = "Feature";
        followUpData.taskStatus = "Doing";
        followUpData.taskId = 2L;
        followUpData.taskNum = "I-2";
        followUpData.taskSummary = "Summary Feature";
        followUpData.taskDescription = "Description Feature";
        followUpData.taskFullDescription = "Full Description Feature";
        followUpData.taskRelease = "Release";
        followUpData.subtaskType = "Sub-task";
        followUpData.subtaskStatus = "Doing";
        followUpData.subtaskId = 3L;
        followUpData.subtaskNum = "I-3";
        followUpData.subtaskSummary = "Summary Sub-task";
        followUpData.subtaskDescription = "Description Sub-task";
        followUpData.subtaskFullDescription = "Full Description Sub-task";
        followUpData.tshirtSize = "M";
        followUpData.worklog = 1D;
        followUpData.wrongWorklog = 1D;
        followUpData.demandBallpark = 1D;
        followUpData.taskBallpark = 1D;
        followUpData.queryType = "Type";
        return followUpData;
    }

    public static List<FollowUpData> getFollowUpDataDefaultList() {
        return Collections.singletonList(getFollowUpDataDefault());
    }
}
