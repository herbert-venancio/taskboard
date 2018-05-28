package objective.taskboard.it;

import static org.openqa.selenium.support.PageFactory.initElements;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FollowupReport extends AbstractUiFragment {
    private WebElement generateButton;
    private WebElement dateDropdown;
    private WebElement projectAutocomplete;
    private WebElement clearDateButton;
    private WebElement addLink;

    public FollowupReport(WebDriver driver) {
        super(driver);
    }

    @FindBy(css=".followup-button")
    private WebElement followupButton;
    
    @FindBy(id="followupReport")
    private WebElement followupReport;
    
    public static FollowupReport open(WebDriver webDriver) {
        return initElements(webDriver, FollowupReport.class).open();
    }
    
    private FollowupReport open() {
        waitForClick(followupButton);
        waitVisibilityOfElement(followupReport);
        generateButton = followupReport.findElement(By.id("generate"));
        projectAutocomplete = followupReport.findElement(By.id("projectAutocomplete"));
        dateDropdown = followupReport.findElement(By.name("date"));
        clearDateButton = followupReport.findElement(By.cssSelector(".clear-button"));
        addLink = followupReport.findElement(By.className("add-link"));
        waitVisibilityOfElements(generateButton, projectAutocomplete, dateDropdown, addLink);
        return this;
    }

    public FollowupReport selectDate(String date) {
        selectPaperDropdownItem(dateDropdown, date);
        return this;
    }

    public FollowupReport clickClearDate() {
        waitForClick(clearDateButton);
        return this;
    }

    public FollowupReportType clickAddLink() {
        waitForClick(addLink);
        return FollowupReportType.open(webDriver);
    }

    public FollowupReport assertGenerateButtonIsDisabled() {
        waitElementIsDisabled(generateButton);
        return this;
    }
    
    public FollowupReport assertGenerateButtonIsEnabled() {
        waitElementIsEnabled(generateButton);
        return this;
    }

    public FollowupReport assertProjectIsDisabled() {
        waitElementIsDisabled(projectAutocomplete);
        return this;
    }

    public FollowupReport assertProjectIsEnabled() {
        waitElementIsEnabled(projectAutocomplete);
        return this;
    }

    public FollowupReport assertDateIsToday() {
        waitAttributeValueInElement(dateDropdown, "value", "Today");
        return this;
    }

    public FollowupReport assertDateDropdownIsDisabled() {
        waitElementIsDisabled(dateDropdown);
        return this;
    }

    public FollowupReport assertDateDropdownIsEnabled() {
        waitElementIsEnabled(dateDropdown);
        return this;
    }

    public FollowupReport close() {
        WebElement close = followupReport.findElement(By.cssSelector(".modal__close"));
        waitForClick(close);
        return this;
    }

    public FollowupReport assertReportTypes(List<String> reportTypesExpected) {
        List<WebElement> reportTypes = followupReport.findElements(By.cssSelector("paper-radio-button"));
        if (reportTypes.size() > 0)
            waitVisibilityOfElementList(reportTypes);
        assertEquals(reportTypesExpected.size(), reportTypes.size());
        List<String> reportTypesActual = reportTypes.stream()
            .map(r -> r.getText())
            .collect(Collectors.toList());
        assertThat(reportTypesActual, equalTo(reportTypesExpected));
        return this;
    }

    public FollowupReportType clickEditReportType() {
        WebElement editButton = followupReport.findElement(By.id("editButton"));
        waitForClick(editButton);
        return FollowupReportType.open(webDriver);
    }

    public FollowupReport clickDeleteReportType() {
        WebElement deleteButton = followupReport.findElement(By.id("deleteButton"));
        waitForClick(deleteButton);
        return this;
    }

    public FollowupReport clickOnCancelDelete() {
        WebElement cancel = followupReport.findElement(By.cssSelector("#followupReportConfirmModal #cancel"));
        waitForClick(cancel);
        return this;
    }

    public FollowupReport clickOnConfirmDelete() {
        WebElement confirm = followupReport.findElement(By.cssSelector("#followupReportConfirmModal #confirm"));
        waitForClick(confirm);
        return this;
    }

    public FollowupReport selectReportType(String reportTypeName) {
        List<WebElement> reportTypes = followupReport.findElements(By.cssSelector("paper-radio-button"));
        for (WebElement reportType : reportTypes)
            if (reportType.getText().equals(reportTypeName)) {
                waitForClick(reportType);
                break;
            }
        return this;
    }

    public FollowupReport setProject(String name) {
        WebElement input = projectAutocomplete.findElement(By.cssSelector("paper-input input"));
        input.sendKeys(Keys.CONTROL,"a");
        input.sendKeys(Keys.DELETE);
        input.sendKeys(name);
        input.sendKeys(Keys.ENTER);
        return this;
    }

    public FollowupReport assertProjectNotFoundIsVisible() {
        WebElement iconNotFound = projectAutocomplete.findElement(By.className("icon-not-found"));
        waitVisibilityOfElement(iconNotFound);
        return this;
    }

    public FollowupReport assertProjectNotFoundIsNotVisible() {
        try {
            WebElement iconNotFound = projectAutocomplete.findElement(By.className("icon-not-found"));
            waitInvisibilityOfElement(iconNotFound);
        } catch (NoSuchElementException e) { }
        return this;
    }

}
