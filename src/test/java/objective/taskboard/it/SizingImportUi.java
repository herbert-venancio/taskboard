package objective.taskboard.it;

import static org.openqa.selenium.support.PageFactory.initElements;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SizingImportUi extends AbstractUiFragment {

    @FindBy(css=".sizing-button")
    private WebElement buttonOpenSizing;

    @FindBy(id="sizingimport")
    private WebElement sizing;

    @FindBy(className="sizingimport-modal")
    private WebElement modal;

    @FindBy(id="error-message")
    private WebElement errorMessageTitle;

    private SizingStepOne stepOne;
    private SizingStepTwo stepTwo;
    private SizingStepThree stepThree;
    private SizingStepFour stepFour;

    public SizingImportUi(WebDriver driver) {
        super(driver);
        initElements(driver, this);
        
        stepOne = new SizingStepOne(webDriver);
        stepTwo = new SizingStepTwo(webDriver);
        stepThree = new SizingStepThree(webDriver);
        stepFour = new SizingStepFour(webDriver);
    }

    public static SizingImportUi open(WebDriver webDriver) {
        return new SizingImportUi(webDriver).open();
    }

    private SizingImportUi open() {
        buttonOpenSizing.click();
        waitVisibilityOfElement(sizing);
        return this;
    }

    public SizingStepOne waitForStepOne() {
        stepOne.waitForStep();
        return stepOne;
    }

    public SizingStepTwo waitForStepTwo() {
        stepTwo.waitForStep();
        return stepTwo;
    }

    public SizingStepThree waitForStepThree() {
        stepThree.waitForStep();
        return stepThree;
    }

    public SizingStepFour waitForStepFour() {
        stepFour.waitForStep();
        return stepFour;
    }

    public void assertErrorMessage(String expectedTitle) {
        waitTextInElement(errorMessageTitle, expectedTitle);
    }

    public void assertNoErrorMessage() {
        waitUntilElementNotExists(By.id("error-message-title"));
    }

    public void assertIsOpen() {
        waitVisibilityOfElement(modal);
    }
    
    public void assertIsClosed() {
        waitInvisibilityOfElement(modal);
    }

    private static void removeValues(WebElement element) {
        while(!"".equals(element.getAttribute("value")))
            element.sendKeys(Keys.BACK_SPACE);
    }
    
    static abstract class SizingStep extends AbstractUiFragment {
        private final Integer stepNumber;
        private final String title;

        @FindBy(css="#sizingimport #stepsNumbers .steps__number.active")
        private WebElement activeStepNumber;
        
        @FindBy(css="#sizingimport .step__title")
        private WebElement titleElement;
        
        public SizingStep(WebDriver driver, Integer stepNumber, String title) {
            super(driver);
            initElements(driver, this);

            this.stepNumber = stepNumber;
            this.title = title;
        }
        
        public void waitForStep() {
            waitTextInElement(activeStepNumber, stepNumber.toString());
            waitTextInElement(titleElement, title);
        }
    }

    static abstract class SizingMiddleStep extends SizingStep {

        @FindBy(css="#sizingimport .steps__content .sizing__back")
        protected WebElement buttonBack;
        
        @FindBy(css="#sizingimport .steps__content .sizing__cancel")
        protected WebElement buttonCancel;
        
        @FindBy(css="#sizingimport .steps__content .sizing__submit")
        protected WebElement buttonSubmit;

        public SizingMiddleStep(WebDriver driver, Integer stepNumber, String title) {
            super(driver, stepNumber, title);
        }

        public void cancel() {
            buttonCancel.click();
        }

        public void backStep() {
            buttonBack.click();
        }

        public void submitStep() {
            buttonSubmit.click();
        }
    }

    static class SizingStepOne extends SizingStep {

        @FindBy(css="#sizingimport .steps__content .sizing__cancel")
        private WebElement buttonCancel;

        @FindBy(css="#sizingimport .steps__content .sizing__submit")
        private WebElement buttonSubmit;

        @FindBy(css="#sizingimport .steps__content #projectkey")
        private WebElement selectProjectElement;
        
        @FindBy(css="#sizingimport .steps__content #spreadsheetUrl")
        private WebElement spreadsheetUrl;

        public SizingStepOne(WebDriver driver) {
            super(driver, 1, "Checking authorization");
        }

        public SizingStepOne withSpreadsheetUrl(String url) {
            spreadsheetUrl.sendKeys(url);
            waitAttributeValueInElement(spreadsheetUrl, "value",  url);
            return this;
        }

        protected void cancel() {
            buttonCancel.click();
        }

        public void submitStep() {
            buttonSubmit.click();
        }
    }

    static class SizingStepTwo extends SizingMiddleStep {

        @FindBy(css="#sizingimport .steps__content .sizing__open-advanced-mapping")
        private WebElement buttonAdvancedMapping;

        @FindBy(css="#sizingimport .sizing__mapping-column")
        private List<WebElement> columnsOfAdvancedMapping;
        
        @FindBy(css="#sizingimport .sizing__mapping-column.is-required")
        private List<WebElement> requiredColumnsOfAdvancedMapping;

        public SizingStepTwo(WebDriver driver) {
            super(driver, 2, "Google Spreadsheet Mapping");
        }

        public SizingStepTwo withRepeatedColumns() {
            columnsOfAdvancedMapping.forEach(column -> {
                removeValues(column);
                column.sendKeys("A");
            });
            return this;
        }

        public SizingStepTwo withRequiredFieldsWithoutValues() {
            requiredColumnsOfAdvancedMapping.forEach(column -> {
                removeValues(column);
            });
            return this;
        }

        public SizingStepTwo withColumnValue(Integer columnIndex, String columnValue) {
            WebElement input = requiredColumnsOfAdvancedMapping.get(columnIndex);
            removeValues(input);
            input.sendKeys(columnValue);
            return this;
        }

        public SizingStepTwo assertColumnValueEquals(Integer columnIndex, String columnValue, Boolean isEquals) {
            WebElement input = requiredColumnsOfAdvancedMapping.get(columnIndex);
            waitVisibilityOfElement(input);
            if(isEquals)
                waitAttributeValueInElement(input, "value", columnValue);
            else
                waitAttributeValueInElementIsNot(input, "value", columnValue);
            
            return this;
        }

        public SizingStepTwo openAdvancedMapping() {
            buttonAdvancedMapping.click();
            WebElement advancedMapping = webDriver.findElement(By.cssSelector("#sizingimport .sizing__advanced-mapping"));
            waitVisibilityOfElement(advancedMapping);
            return this;
        }
    }

    static class SizingStepThree extends SizingMiddleStep {

        public SizingStepThree(WebDriver driver) {
            super(driver, 3, "Confirmation");
        }
    }

    static class SizingStepFour extends SizingStep {
        
        @FindBy(css="#sizingimport .steps__content .sizing__return-to-taskboard")
        private WebElement buttonReturnToTaskboard;
        
        @FindBy(css="#sizingimport .steps__content .sizing__open-spreadsheet")
        private WebElement buttonOpenSpreadsheet;

        public SizingStepFour(WebDriver driver) {
            super(driver, 4, "Import Progress");
        }

        public SizingStepFour assertShowingButtonsAfterFinish() {
            waitVisibilityOfElements(buttonReturnToTaskboard, buttonOpenSpreadsheet);
            return this;
        }

        public void returnToTaskboard() {
            buttonReturnToTaskboard.click();
        }
    }
}