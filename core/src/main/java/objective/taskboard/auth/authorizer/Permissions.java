package objective.taskboard.auth.authorizer;

public interface Permissions {

    String TASKBOARD_ADMINISTRATION = "taskboard.administration";

    String USER_VISIBILITY = "user.visibility";

    String PROJECT_ADMINISTRATION = "project.administration";
    String PROJECT_DASHBOARD_VIEW = "project.dashboard.view";
    String PROJECT_DASHBOARD_TACTICAL = "project.dashboard.tactical";
    String PROJECT_DASHBOARD_OPERATIONAL = "project.dashboard.operational";

    String FOLLOWUP_TEMPLATE_EDIT = "followup.template.edit";
    String SIZING_IMPORT_VIEW = "sizing.import.view";

    String TEAMS_EDIT_VIEW = "teams.edit.view";
    String TEAM_EDIT = "team.edit";

}
