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

    List<WebElement> stepsNumbers;

    public SizingImportUi(WebDriver driver) {
        super(driver);
    }

    public static SizingImportUi open(WebDriver webDriver) {
        return initElements(webDriver, SizingImportUi.class).open();
    }

    private SizingImportUi open() {
        buttonOpenSizing.click();
        waitVisibilityOfElement(sizing);
        stepsNumbers = sizing.findElements(By.cssSelector(".steps__number[data-step]"));
        return this;
    }

    public SizingStepOne showStepOne() {
        return new SizingStepOne();
    }

    public void assertWithShowingError(Boolean showingError) {
        if (showingError)
            waitUntilElementExists(By.id("errorMessage"));
        else
            waitUntilElementNotExists(By.id("errorMessage"));
    }

    public void asserWithShowingSizing(Boolean showingSizing) {
        if (showingSizing)
            waitVisibilityOfElement(modal);
        else
            waitInvisibilityOfElement(modal);
    }

    public void assertShowingCorrectStep(Integer stepNumber) {
        WebElement activeStepNumber = stepsNumbers.get(stepNumber - 1);    	
        waitAttributeValueInElementContains(activeStepNumber, "class",  "active");

        WebElement step = webDriver.findElement(By.cssSelector(".sizing__step.active[data-step='" + stepNumber + "'"));
        waitVisibilityOfElement(step);
    }

    private void removeValues(WebElement element) {
        while(!"".equals(element.getAttribute("value")))
            element.sendKeys(Keys.BACK_SPACE);
    }

    abstract class SizingMiddleStep {

        protected Integer stepNumber;

        protected WebElement step;
        protected WebElement buttonBack;
        protected WebElement buttonCancel;
        protected WebElement buttonSubmit;

        public void cancel() {
            buttonCancel.click();
            waitInvisibilityOfElement(modal);
        }

        protected void setDefaultMiddleStepElements() {
            step = sizing.findElement(By.cssSelector(".sizing__step[data-step='" + stepNumber + "']"));
            waitVisibilityOfElement(step);
            buttonBack = step.findElement(By.cssSelector(".sizing__back"));
            buttonCancel = step.findElement(By.cssSelector(".sizing__cancel"));
            buttonSubmit = step.findElement(By.cssSelector(".sizing__submit"));
            waitVisibilityOfElements(buttonBack, buttonCancel, buttonSubmit);
        }

    }

    class SizingStepOne {

        private Integer stepNumber;

        private WebElement step;
        private WebElement buttonCancel;
        private WebElement buttonSubmit;

        private WebElement selectProjectElement;
        private WebElement spreadsheetUrl;

        public SizingStepOne() {
            stepNumber = 1;
            setDefaultFirstStepElements();

            spreadsheetUrl = step.findElement(By.id("spreadsheetUrl"));  
            selectProjectElement = step.findElement(By.id("projectkey"));
            waitVisibilityOfElements(selectProjectElement,spreadsheetUrl);
        }

        private void setDefaultFirstStepElements() {
            step = sizing.findElement(By.cssSelector(".sizing__step[data-step='" + stepNumber + "']"));
            waitVisibilityOfElement(step);
            buttonCancel = step.findElement(By.cssSelector(".sizing__cancel"));
            buttonSubmit = step.findElement(By.cssSelector(".sizing__submit"));
            waitVisibilityOfElements(buttonCancel, buttonSubmit);
        }

        public SizingStepOne withSpreadsheetUrl(String url) {
            spreadsheetUrl.sendKeys(url);
            waitAttributeValueInElement(spreadsheetUrl, "value",  url);
            return this;
        }

        public void assertCanCancel() {
            cancel();
        }

        protected void cancel() {
            buttonCancel.click();
            waitInvisibilityOfElement(modal);
        }

        public SizingStepOne submitStep() {
            buttonSubmit.click();
            return this;
        }

        public SizingStepTwo showStepTwo() {
            return new SizingStepTwo();
        }

    }

    class SizingStepTwo extends SizingMiddleStep {

        private WebElement buttonAdvancedMapping;
        private List<WebElement> columnsOfAdvancedMapping;
        private List<WebElement> requiredColumnsOfAdvancedMapping;

        public SizingStepTwo() {
            stepNumber = 2;
            setDefaultMiddleStepElements();
            buttonAdvancedMapping = step.findElement(By.cssSelector(".sizing__open-advanced-mapping"));
            waitVisibilityOfElement(buttonAdvancedMapping);
            waitUntilElementExists(By.cssSelector(".sizing__mapping-column[data-index='1']"));
        }

        public SizingStepTwo withRepeatedColumns() {
            openAdvancedMappingAndGetElements();
            columnsOfAdvancedMapping.forEach(column -> {
                removeValues(column);
                column.sendKeys("A");
            });
            return this;
        }

        public SizingStepTwo withRequiredFieldsWithoutValues() {
            openAdvancedMappingAndGetElements();
            requiredColumnsOfAdvancedMapping.forEach(column -> {
                removeValues(column);
            });
            return this;
        }

        public SizingStepTwo withColumnValue(Integer columnIndex, String columnValue) {
            openAdvancedMappingAndGetElements();
            WebElement input = requiredColumnsOfAdvancedMapping.get(columnIndex);
            removeValues(input);
            input.sendKeys(columnValue);
            return this;
        }

        public void assertColumnValueEquals(Integer columnIndex, String columnValue, Boolean isEquals) {
            WebElement input = requiredColumnsOfAdvancedMapping.get(columnIndex);
            waitVisibilityOfElement(input);
            if(isEquals)
                waitAttributeValueInElement(input, "value", columnValue);
            else
                waitAttributeValueInElementIsNot(input, "value", columnValue);
        }

        public SizingStepOne backStep() {
            buttonSubmit.click();
            return new SizingStepOne();
        }

        public SizingStepTwo submitStep() {
            buttonSubmit.click();
            return this;
        }

        public SizingStepThree showStepThree() {
            return new SizingStepThree();
        }

        private void openAdvancedMappingAndGetElements() {
            WebElement advancedMappingAccordion = step.findElement(By.cssSelector(".tb-accordion__collapse"));
            if (!Boolean.valueOf(advancedMappingAccordion.getAttribute("aria-expanded"))) {
                buttonAdvancedMapping.click();
                WebElement advancedMapping = step.findElement(By.cssSelector(".sizing__advanced-mapping"));
                waitVisibilityOfElement(advancedMapping);
                columnsOfAdvancedMapping = step.findElements(By.cssSelector(".sizing__mapping-column"));
                requiredColumnsOfAdvancedMapping = step.findElements(By.cssSelector(".sizing__mapping-column.is-required"));
            }
        }

    }

    class SizingStepThree extends SizingMiddleStep {

        private List<WebElement> spreadSheetHeaderValues;
        private List<WebElement> spreadSheetValuesOfFirstRow;

        public SizingStepThree() {
            stepNumber = 3;
            setDefaultMiddleStepElements();

            spreadSheetHeaderValues = getElementsWhenTheyExists(By.cssSelector(".sizing__confirmation-header-value"));
            spreadSheetValuesOfFirstRow = getElementsWhenTheyExists(By.cssSelector(".sizing__confirmation-row:first-child .sizing__confirmation-row-value"));
            waitVisibilityOfElements(spreadSheetHeaderValues.get(0), spreadSheetValuesOfFirstRow.get(0));
        }

        public SizingStepTwo backStep() {
            buttonSubmit.click();
            return new SizingStepTwo();
        }

        public SizingStepThree submitStep() {
            buttonSubmit.click();
            return this;
        }

        public SizingStepFour showStepFour() {
            return new SizingStepFour();
        }

    }

    class SizingStepFour {

        private Integer stepNumber;

        private WebElement step;
        private WebElement buttonReturnToTaskboard;
        private WebElement buttonOpenSpreadsheet;

        public SizingStepFour() {
            stepNumber = 4;
            setDefaultLastStepElements();
            buttonReturnToTaskboard = getElementWhenItExists(By.cssSelector(".sizing__return-to-taskboard"));
            buttonOpenSpreadsheet = getElementWhenItExists(By.cssSelector(".sizing__open-spreadsheet"));
        }

        public void assertShowingButtonsAfterFinish() {
            waitVisibilityOfElements(buttonReturnToTaskboard, buttonOpenSpreadsheet);
        }

        public void returnToTaskboard() {
            buttonReturnToTaskboard.click();
            waitInvisibilityOfElement(modal);
        }

        private void setDefaultLastStepElements() {
            step = sizing.findElement(By.cssSelector(".sizing__step[data-step='" + stepNumber + "']"));
            waitVisibilityOfElement(step);
            waitUntilElementExists(By.cssSelector(".sizing__step[data-step='" + stepNumber + "'] .step__buttons"));
            buttonReturnToTaskboard = step.findElement(By.cssSelector(".sizing__return-to-taskboard"));
            buttonOpenSpreadsheet = step.findElement(By.cssSelector(".sizing__open-spreadsheet"));
            waitVisibilityOfElements(buttonReturnToTaskboard, buttonOpenSpreadsheet);
        }

    }

}