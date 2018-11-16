package objective.taskboard.it.config.project;

import static java.util.stream.Collectors.joining;
import static objective.taskboard.it.components.ButtonComponent.BUTTON_TAG;
import static objective.taskboard.it.components.SelectComponent.SELECT_TAG;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.apache.commons.lang3.StringUtils.join;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import java.util.List;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.ProjectConfigurationDialog;
import objective.taskboard.it.components.ButtonComponent;
import objective.taskboard.it.components.SelectComponent;
import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.it.components.TabComponent;
import objective.taskboard.it.components.TabsRouterComponent;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectDefaultTeamsConfiguration extends ProjectAdvancedConfigurationTab<ProjectDefaultTeamsConfiguration> {

    public static final String TEAMS_TAB_NAME = "Teams";
    public static final String PROJECT_TEAMS_CONFIGURATION_TAG = "tb-project-teams";

    private static final String TEAM_SELECTOR = SELECT_TAG +"[name='team']";
    private static final String ISSUE_TYPE_SELECTOR = SELECT_TAG +"[name='issueType']";
    private static final String REMOVE_SELECTOR = BUTTON_TAG +".remove-button";

    @FindBy(id="tb-project-default-team")
    private WebElement defaultTeamSelect;

    @FindBy(id="tb-project-teams-add-item")
    private WebElement addDefaultTeamByIssueTypeButton;

    @FindBy(id="tb-project-teams-back-to-project")
    private WebElement backToProjectButton;

    @FindBy(id="tb-project-teams-save")
    private WebElement saveButton;

    @FindBy(id="tb-project-teams-items")
    private WebElement defaultTeamsByIssueType;

    private SelectComponent defaultTeam;
    private SnackBarComponent snackbar;

    public static TabsRouterComponent.TabFactory<ProjectDefaultTeamsConfiguration> factory(ProjectInfo projectInfo) {
        return (webDriver, tab) -> new ProjectDefaultTeamsConfiguration(webDriver, tab, projectInfo);
    }

    public ProjectDefaultTeamsConfiguration(WebDriver webDriver, TabComponent<ProjectDefaultTeamsConfiguration> tab, ProjectInfo projectInfo) {
        super(webDriver, tab, projectInfo);
        initElements(webDriver, this);
        snackbar = new SnackBarComponent(webDriver, cssSelector(SNACK_BAR_TAG +"#tb-project-teams-snackbar"));
        defaultTeam = new SelectComponent(webDriver, cssSelector(SELECT_TAG + "[name='defaultTeam']"));
    }

    public ProjectDefaultTeamsConfiguration setDefaultTeam(String teamName) {
        defaultTeam.select(teamName);
        return this;
    }

    public ProjectDefaultTeamsConfiguration addDefaultTeamByIssueType() {
        waitForClick(addDefaultTeamByIssueTypeButton);
        return this;
    }

    public ProjectDefaultTeamsConfiguration setIssueType(int groupIndex, String value) {
        setSelectValue(groupIndex, ISSUE_TYPE_SELECTOR, value);
        return this;
    }

    public ProjectDefaultTeamsConfiguration setTeam(int groupIndex, String value) {
        setSelectValue(groupIndex, TEAM_SELECTOR, value);
        return this;
    }

    public ProjectConfigurationDialog backToProject() {
        waitForClick(backToProjectButton);
        return new ProjectConfigurationDialog(webDriver, projectInfo).assertIsOpen();
    }

    public ProjectDefaultTeamsConfiguration save() {
        waitForClick(saveButton);
        return this;
    }

    private void setSelectValue(int groupIndex, String elementSelector, String optionName) {
        By cssSelector = getGroupElementSelector(groupIndex, elementSelector);
        SelectComponent select = new SelectComponent(webDriver, cssSelector);
        select.select(optionName);
    }

    public ProjectDefaultTeamsConfiguration assertSavedNotificationIsOpen() {
        snackbar.waitTitleToBe("Project teams saved");
        return this;
    }

    public ProjectDefaultTeamsConfiguration assertErrorsNotificationIsOpen(String... errorsDescriptions) {
        snackbar.waitTitleToBe("Failed to save");
        snackbar.waitDescriptionsToBe(errorsDescriptions);
        return this;
    }

    public ProjectDefaultTeamsConfiguration assertDefaultTeam(String teamName) {
        defaultTeam.waitValueIsSelected(teamName);
        return this;
    }

    public ProjectDefaultTeamsConfiguration assertDefaultTeamsByIssueType(String... expectedGroups) {
        waitAssertEquals(join(expectedGroups, "\n"), () -> {
            List<WebElement> groups = defaultTeamsByIssueType.findElements(cssSelector("tbody tr"));
            return IntStream
                    .range(0, groups.size())
                    .mapToObj(index -> {
                        SelectComponent issueTypeSelect = new SelectComponent(webDriver, getGroupElementSelector(index, ISSUE_TYPE_SELECTOR));
                        SelectComponent teamSelect = new SelectComponent(webDriver, getGroupElementSelector(index, TEAM_SELECTOR));
                        ButtonComponent removeButton = new ButtonComponent(webDriver, getGroupElementSelector(index, REMOVE_SELECTOR));
                        String disabledText = "";
                        if (issueTypeSelect.isDisabled() && teamSelect.isDisabled() && removeButton.isDisabled())
                            disabledText = "- (disabled)";
                        return issueTypeSelect.getValue() +" | "+ teamSelect.getValue() + disabledText;
                    })
                    .collect(joining("\n"));
        });
        return this;
    }

    public ProjectDefaultTeamsConfiguration removeTeamByIssueType(int groupIndex) {
        WebElement removeButton = getChildElementWhenExists(defaultTeamsByIssueType, getGroupElementSelector(groupIndex, REMOVE_SELECTOR));
        waitForClick(removeButton);
        return this;
    }

    private By getGroupElementSelector(int groupIndex, String elementSelector) {
        return cssSelector("tbody tr:nth-of-type(" + (groupIndex + 1) + ") " + elementSelector);
    }
}
