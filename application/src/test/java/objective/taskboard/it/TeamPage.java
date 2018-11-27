package objective.taskboard.it;
import static java.util.stream.Collectors.joining;
import static objective.taskboard.it.AbstractIntegrationTest.getAppBaseUrl;
import static objective.taskboard.it.components.ButtonComponent.BUTTON_TAG;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static objective.taskboard.it.components.TagComponent.TAG_TAG;
import static org.apache.commons.lang3.StringUtils.join;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import java.util.List;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.components.ButtonComponent;
import objective.taskboard.it.components.SearchComponent;
import objective.taskboard.it.components.SelectComponent;
import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.it.components.TagComponent;
import objective.taskboard.it.components.guards.LeaveConfirmationGuard;

public class TeamPage extends AbstractAppUiFragment {

    private String teamName;

    private static final String PAGE_TAG = "tb-team ";
    private static final String MEMBER_SELECTOR = "td:nth-child(1)";
    private static final String TAG_SELECTOR = TAG_TAG +".tag-field__tag";
    private static final String REMOVE_SELECTOR = BUTTON_TAG +".remove-button";

    @FindBy(css=PAGE_TAG +" #tb-page-title")
    private WebElement pageTitle;

    @FindBy(css=PAGE_TAG +" #tb-team-members")
    private WebElement membersList;

    private SelectComponent manager;
    private ButtonComponent addMember;
    private SearchComponent filterMembers;
    private ButtonComponent backToTeams;
    private ButtonComponent save;
    private SnackBarComponent snackbar;

    public TeamPage(WebDriver webDriver, String teamName) {
        super(webDriver);
        this.teamName = teamName;
        this.manager = new SelectComponent(webDriver, cssSelector(PAGE_TAG +" ng-select[name=\"manager\"]"));
        this.addMember = new ButtonComponent(webDriver, cssSelector(PAGE_TAG + " #tb-team-add-member"));
        this.filterMembers = new SearchComponent(webDriver, cssSelector(PAGE_TAG + " #tb-team-filter-members"));
        this.backToTeams = new ButtonComponent(webDriver, cssSelector(PAGE_TAG + " #tb-team-back-to-teams"));
        this.save = new ButtonComponent(webDriver, cssSelector(PAGE_TAG +" #tb-team-save"));
        this.snackbar = new SnackBarComponent(webDriver, cssSelector(PAGE_TAG + SNACK_BAR_TAG +"#tb-team-snackbar"));

        initElements(webDriver, this);
        assertPageIsOpen();
    }

    private TeamPage assertPageIsOpen() {
        waitTextInElement(pageTitle, "Teams > " + teamName);
        waitPageLoaderBeHide();
        return this;
    }

    public TeamPage setManager(String managerValue) {
        manager.select(managerValue);
        return this;
    }

    public TeamPage filterMembers(String value) {
        filterMembers.search(value);
        return this;
    }

    public TeamPage clearFilterMembers() {
        filterMembers.clear();
        return this;
    }

    public TeamPage addMember() {
        addMember.click();
        return this;
    }

    public TeamPage setMember(int groupIndex, String member) {
        SelectComponent select = new SelectComponent(webDriver, getGroupElementSelector(groupIndex, MEMBER_SELECTOR + " ng-select"));
        select.select(member, true);
        return this;
    }

    public TeamPage removeMember(int groupIndex) {
        ButtonComponent remove = new ButtonComponent(webDriver, getGroupElementSelector(groupIndex, REMOVE_SELECTOR));
        remove.click();
        return this;
    }

    public TeamsPage backToTeamsWithoutConfirmation() {
        backToTeams.click();
        LeaveConfirmationGuard.waitIsClosed(webDriver);
        return new TeamsPage(webDriver);
    }

    public TeamPage backToTeamsAndStay() {
        backToTeams.click();
        LeaveConfirmationGuard.stay(webDriver);
        return this;
    }

    public TeamsPage backToTeamsAndLeave() {
        backToTeams.click();
        LeaveConfirmationGuard.leave(webDriver);
        return new TeamsPage(webDriver);
    }

    public TeamPage save() {
        save.click();
        return this;
    }

    public TeamPage refreshWithoutConfirmation() {
        webDriver.navigate().refresh();
        assertPageIsOpen();
        return this;
    }

    public TeamPage assertSavedNotificationIsOpen() {
        snackbar.waitTitleToBe("Team saved");
        return this;
    }

    public TeamPage assertErrorsNotificationIsOpen(String errorTitle, String... errorsDescriptions) {
        snackbar.waitTitleToBe(errorTitle);
        snackbar.waitDescriptionsToBe(errorsDescriptions);
        return this;
    }

    public TeamPage assertManager(String managerValue) {
        manager.waitValueIsSelected(managerValue);
        return this;
    }

    public TeamPage assertVisibleMembers(String... expectedMembersGroups) {
        waitAssertEquals(join(expectedMembersGroups, "\n"), () -> {
            List<WebElement> groups = membersList.findElements(cssSelector("tbody tr"));
            return IntStream
                    .range(0, groups.size())
                    .mapToObj(index -> {
                        String tagValues = "";
                        TagComponent tag = new TagComponent(webDriver, getGroupElementSelector(index, TAG_SELECTOR));
                        if (tag.isVisible()) 
                            tagValues = tag.getFormattedValues("(", ")") + " ";

                        WebElement tdMember = getChildElementWhenExists(membersList, getGroupElementSelector(index, MEMBER_SELECTOR));
                        return tagValues + tdMember.getAttribute("data-member");
                    })
                    .collect(joining("\n"));
        });
        return this;
    }

    private By getGroupElementSelector(int groupIndex, String elementSelector) {
        return cssSelector(PAGE_TAG +" tbody tr:nth-child(" + (groupIndex + 1) + ") " + elementSelector);
    }

    public TeamPage assertSaveDisabled(boolean disabled) {
        save.waitDisabledBe(disabled);
        return this;
    }

    public static String getPageUrl(String teamName) {
        return getAppBaseUrl() + "teams/" + teamName;

    }

}
