package objective.taskboard.it;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import objective.taskboard.utils.ThreadUtils;

class IssueDetails extends AbstractUiFragment {

    WebElement issueDetailRoot;
    public IssueDetails(WebDriver driver) {
        super(driver);
        issueDetailRoot = webDriver.findElement(By.cssSelector("paper-dialog.issue-detail"));
    }

    public IssueDetails assignToMe() {
        assertIsOpened();
        WebElement assignButton = issueDetailRoot.findElement(By.id("assignToMe"));
        waitForClick(assignButton);
        return this;
    }

    public IssueDetails addAssignee(String assigneeName) {
        assertIsOpened();
        WebElement assignButton = issueDetailRoot.findElement(By.id("addAssigneeButton"));
        waitForClick(assignButton);

        return selectStringInPicker(assigneeName, "#pickerForAddAssignee");
    }

    public IssueDetails removeAssignee(String assigneeToRemove) {
        assertIsOpened();
        By removeButtonSelector = By.cssSelector(".assignees paper-material[data-assignee='"+assigneeToRemove+"'] .remove-button");
        waitUntilElementExists(removeButtonSelector);
        WebElement teamTag = issueDetailRoot.findElement(removeButtonSelector);
        waitForClick(teamTag);

        return this;
    }

    public IssueDetails addTeam(String teamName) {
        assertIsOpened();
        WebElement assignButton = issueDetailRoot.findElement(By.id("addTeamButton"));
        waitForClick(assignButton);

        String pickerSelector = "#teamSelector";
        return selectStringInPicker(teamName, pickerSelector);
    }

    public IssueDetails replaceTeam(String teamToReplace, String replacement) {
        assertIsOpened();
        String pickerSelector = "#teamSelector";

        WebElement teamTag = issueDetailRoot.findElement(By.cssSelector(".teams paper-material[data-team='"+teamToReplace+"'] span"));
        waitForClick(teamTag);

        return selectStringInPicker(replacement, pickerSelector);
    }

    public IssueDetails removeTeam(String teamToRemove) {
        assertIsOpened();
        WebElement teamTag = getElementWhenItExists(By.cssSelector(".teams paper-material[data-team='"+teamToRemove+"'] .remove-button"));
        waitForClick(teamTag);

        return this;
    }

    private IssueDetails selectStringInPicker(String valueToSelect, String pickerSelector) {
        WebElement pickerForAddTeam = issueDetailRoot.findElement(By.cssSelector(pickerSelector));
        waitVisibilityOfElement(pickerForAddTeam);
        WebElement pickerInput = pickerForAddTeam.findElement(By.cssSelector("input[slot='input']"));
        waitVisibilityOfElement(pickerInput);
        waitForClick(pickerInput);
        pickerInput.sendKeys(valueToSelect);

        waitUntilElementExistsWithText(By.cssSelector(pickerSelector + " #suggestionsWrapper paper-item.active .paper-autocomplete-suggestions"), valueToSelect);
        waitForClick(By.cssSelector(pickerSelector +" #suggestionsWrapper paper-item.active"));
        return this;
    }

    public IssueDetails assertAssignees(String... assigneeList) {
        assertIsOpened();
        assertListOfItems(cssSelector(".assignees .assignee span"), assigneeList);
        return this;
    }

    public IssueDetails assertTeams(String... teamList) {
        assertIsOpened();
        assertListOfItems(cssSelector(".teams .team span"), teamList);
        return this;
    }

    public IssueDetails assertIsDefaultTeam(String team) {
        assertIsOpened();
        assertListOfItems(cssSelector(".teams .default-team span"), team);
        waitElementExistenceAndVisibilityIs(true, replaceButtonSelector(team));
        return this;
    }

    public IssueDetails assertIsTeamByIssueType(String team, String issueType, String project) {
        assertIsOpened();
        assertListOfItems(cssSelector(".teams .team-by-issue-type span"), team);
        waitUntilElementExists(cssSelector(".teams .team-by-issue-type[title='" +team +" is the default team for issue type "+ issueType +" on project "+ project +"']"));
        waitElementExistenceAndVisibilityIs(true, replaceButtonSelector(team));
        return this;
    }

    public IssueDetails assertAreInheritedTeams(String... teamList) {
        assertIsOpened();
        assertListOfItems(cssSelector(".teams .parent-team span"), teamList);
        Stream.of(teamList).forEach(team -> {
            waitElementExistenceAndVisibilityIs(true, replaceButtonSelector(team));
        });
        return this;
    }

    public void assertIsClosed() {
        waitUntil(attributeToBe(issueDetailRoot, "data-status", "closed"));
        waitInvisibilityOfElement(issueDetailRoot);
    }

    public IssueDetails assertCardName(String cardNameExpected) {
        waitTextInElement(getElementWhenItExists(By.id("card-name")), cardNameExpected);
        return this;
    }

    public IssueDetails openParentCard() {
        waitForClick(getElementWhenItExists(By.id("parent-card-name")));
        return this;
    }

    public IssueDetails openFirstChildCard() {
        waitForClick(getElementWhenItExists(By.id("child-issue-0")));
        return this;
    }

    public IssueDetails transitionClick(String transitionName) {
        assertIsOpened();
        waitUntilElementExists(By.cssSelector("[data-transition-name='"+transitionName+"']"));
        WebElement transitionButton = issueDetailRoot.findElement(By.cssSelector("[data-transition-name='"+transitionName+"']"));
        waitForClick(transitionButton);
        return this;
    }

    public void setDescription(String description) {
        assertIsOpened();

        waitForHover(By.cssSelector(".description-box.issue-detail"));

        waitForClick(By.cssSelector(".wrapper-edit"));

        WebElement descriptionField = issueDetailRoot.findElement(By.className("description-box__field"));
        setInputValue(descriptionField, description);

        waitForClick(By.cssSelector(".description--save-button"));

        assertDescription(description);
    }

    public void assertDescription(String text) {
        WebElement descriptionContentArea = webDriver.findElement(By.cssSelector(".description-box marked-element"));
        waitTextInElement(descriptionContentArea, text);
    }

    public IssueDetails confirm() {
        WebElement confirmationModal = webDriver.findElement(By.id("confirmModal"));
        waitVisibilityOfElement(confirmationModal);
        WebElement confirmButton = confirmationModal.findElement(By.id("confirm"));
        waitForClick(confirmButton);
        assertIsClosed();
        return this;
    }

    public IssueDetails closeDialog() {
        assertIsOpened();
        WebElement close = issueDetailRoot.findElement(By.className("button-close"));
        waitForClick(close);
        assertIsClosed();
        return this;
    }

    public IssueDetails assertRefreshWarnIsOpen() {
        assertIsOpened();
        waitUntilElementExists(By.id("glasspane-updated"));
        return this;
    }

    public IssueDetails clickOnRefreshWarning() {
        WebElement glasspane = webDriver.findElement(By.id("glasspane-updated"));
        waitForClick(glasspane);
        return this;
    }

    public IssueDetails assertDeleteWarnIsOpen() {
        assertIsOpened();
        waitUntilElementExists(By.id("glasspane-deleted"));
        return this;
    }

    public IssueDetails clickOnDeleteWarning() {
        WebElement glasspane = webDriver.findElement(By.id("glasspane-deleted"));
        waitForClick(glasspane);
        return this;
    }

    public IssueDetails assertInvalidTeamWarnIsVisible() {
        WebElement icon = issueDetailRoot.findElement(By.cssSelector(".assignee .icon"));
        waitVisibilityOfElement(icon);
        return this;
    }

    public IssueDetails assertInvalidTeamWarnIsInvisible() {
        waitUntilElementNotExists(By.cssSelector(".assignee .icon"));
        return this;
    }

    public IssueDetails assertHasError(String text) {
        WebElement error = getElementWhenItExists(By.className("message-box--error"));
        waitVisibilityOfElement(error);
        WebElement errorText = error.findElement(By.className("message-box__message"));
        waitTextInElement(errorText, text);
        return this;
    }

    public IssueDetails closeError() {
        WebElement error = getElementWhenItExists(By.className("message-box--error"));
        WebElement closeError = error.findElement(By.className("message-box__close"));
        waitForClick(closeError);
        waitInvisibilityOfElement(error);
        return this;
    }

    public IssueDetails assertClassOfService(String classOfServiceExpected) {
        assertIsOpened();
        WebElement classOfServiceValue = issueDetailRoot.findElement(By.id("class-of-service-value"));
        waitTextInElement(classOfServiceValue, classOfServiceExpected);
        return this;
    }

    public IssueDetails assertIssueType(String issueTypeExpected) {
        assertIsOpened();
        waitUntilElementExistsWithText(cssSelector(".issue-detail.issue-type .text"), issueTypeExpected);
        return this;
    }

    public IssueDetails assertColor(String colorExpected) {
        assertIsOpened();
        waitAttributeValueInElement(issueDetailRoot, "background-color", colorExpected);
        return this;
    }

    private void assertIsOpened() {
        waitAttributeValueInElement(issueDetailRoot, "data-status", "opened");
    }

    public void assertListOfItems(By by, String... expectedItemList) {
        waitUntil(new ExpectedCondition<Boolean>() {
            private String[] actualTeamList;
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    actualTeamList = driver.findElements(by).stream()
                        .map(i -> i.getText().trim())
                        .toArray(String[]::new);
                    return Arrays.equals(expectedItemList, actualTeamList);
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }
            @Override
            public String toString() {
                return String.format("team list to be \"%s\". Current team list is: \"%s\"",
                        StringUtils.join(expectedItemList, ","),
                        StringUtils.join(actualTeamList, ","));
            }
        });
    }

    public IssueDetails assertReleaseNotVisible() {
        assertIsOpened();

        try {
            WebElement element = issueDetailRoot.findElement(By.className("release"));
            waitInvisibilityOfElement(element);
        } catch (NoSuchElementException e) { }

        return this;
    }

    public IssueDetails assertRelease(String release) {
        assertIsOpened();

        WebElement releaseElement = getElementWhenItExists(By.className("release"));
        waitTextInElement(releaseElement, release);

        return this;
    }

    private By replaceButtonSelector(String team) {
        return cssSelector(".assignee-button[title='Replace "+ team +" by another team']");
    }

}