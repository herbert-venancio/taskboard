package objective.taskboard.it;

import static org.openqa.selenium.support.PageFactory.initElements;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class FollowupReportType extends AbstractUiFragment {
    private WebElement saveButton;
    private WebElement nameInput;
    private WebElement fileInput;
    private WebElement dropFileMessage;
    private List<WebElement> rolesCheckbox;
    
    private final String ERROR_MESSAGE = "Make sure the name is not empty, the report type file has been uploaded, " +
            "and at least one role has been selected.";

    @FindBy(id="followupReportType")
    private WebElement followupReportType;

    public static FollowupReportType produce(WebDriver webDriver) {
        return initElements(webDriver, FollowupReportType.class);
    }

    public static FollowupReportType open(WebDriver webDriver) {
        return produce(webDriver).open();
    }

    private FollowupReportType open() {
        saveButton = followupReportType.findElement(By.id("save"));
        nameInput = followupReportType.findElement(By.cssSelector("#inputName"));
        rolesCheckbox = followupReportType.findElements(By.cssSelector("paper-checkbox"));
        waitVisibilityOfElements(saveButton, nameInput);
        waitVisibilityOfElementList(rolesCheckbox);

        dropFileMessage = followupReportType.findElement(By.cssSelector("drag-and-drop-file #dropFileMessage"));
        fileInput = followupReportType.findElement(By.cssSelector("drag-and-drop-file #inputFile")); // isInvisible
        return this;
    }
    
    public FollowupReportType close() {
        WebElement close = followupReportType.findElement(By.cssSelector(".modal__close"));
        waitForClick(close);
        return this;
    }

    public FollowupReportType(WebDriver driver) {
        super(driver);
    }

    public FollowupReportType clickOnSave() {
        waitForClick(saveButton);
        return this;
    }

    public FollowupReportType setName(String name) {
        nameInput.sendKeys(Keys.CONTROL,"a");
        nameInput.sendKeys(Keys.DELETE);
        nameInput.sendKeys(name);
        return this;
    }

    public FollowupReportType setFile() {
        File file = null;
        try {
            file = new File(FollowupReportType.class.getResource("/objective/taskboard/followup/OkFollowupTemplate.xlsm").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        sendKeysToFileInput(fileInput, file.toString());
        return this;
    }

    public FollowupReportType clearFile() {
        if (!dropFileMessage.isDisplayed()) {
            WebElement clearFileButton = followupReportType.findElement(By.id("clearFileButton"));
            waitForClick(clearFileButton);
        }
        return this;
    }

    public FollowupReportType setRoles(String role) {
        for(WebElement checkbox : rolesCheckbox)
            if(role.equals(checkbox.getText()))
                waitForClick(checkbox);
        return this;
    }

    public FollowupReportType clearRoles() {
        for (WebElement role : rolesCheckbox)
            if (role.getAttribute("checked") != null && role.getAttribute("checked").equals("true"))
                waitForClick(role);
        return this;
    }

    public FollowupReportType tryCreateReportType(FollowupReportTypeData reportType) {
        setName("");
        clearFile();
        clearRoles();

        if (reportType.hasName())
            setName(reportType.name);

        if (reportType.withFile)
            setFile();

        if (reportType.hasRole())
            setRoles(reportType.role);

        clickOnSave();
        return this;
    }
    
    public void waitClose() {
        waitInvisibilityOfElement(saveButton);
    }

    public FollowupReportType editReportType() {
        setName("Report Type Test edited");
        clickOnSave();
        clickOnConfirmUpdate();
        waitClose();
        return this;
    }

    private void clickOnConfirmUpdate() {
        WebElement confirm = followupReportType.findElement(By.cssSelector("#followupReportTypeConfirmModal #confirm"));
        waitForClick(confirm);
    }

    public FollowupReportType clickOnCancelDiscardMessage() {
        WebElement cancel = followupReportType.findElement(By.cssSelector("#modal #confirmationModal #cancel"));
        waitForClick(cancel);
        return this;
    }

    public FollowupReportType clickOnConfirmDiscardMessage() {
        WebElement confirm = followupReportType.findElement(By.cssSelector("#modal #confirmationModal #confirm"));
        waitForClick(confirm);
        return this;
    }

    public FollowupReportType assertErrorMessage() {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                WebElement errorMessage = followupReportType.findElement(By.className("tb-message-box__message"));
                waitVisibilityOfElement(errorMessage);
                return ERROR_MESSAGE.equals(errorMessage.getText()); 
            }
        });
        closeMessage();
        return this;
    }

    private void closeMessage() {
        WebElement close = followupReportType.findElement(By.className("tb-message-box__close"));
        waitForClick(close);
    }

    public static class FollowupReportTypeData {
        private String name = "";
        boolean withFile = false;
        private String role = "";

        FollowupReportTypeData() { }

        public FollowupReportTypeData withName() {
            this.name = "Report Type Test";
            return this;
        }

        public FollowupReportTypeData withName(String name) {
            this.name = name;
            return this;
        }

        public FollowupReportTypeData withFile() {
            this.withFile = true;
            return this;
        }

        public FollowupReportTypeData withRoles() {
            this.role = "Administrators";
            return this;
        }

        public FollowupReportTypeData withRoles(String role) {
            this.role = role;
            return this;
        }

        public boolean hasName() {
            return StringUtils.isNotEmpty(name);
        }

        public boolean hasRole() {
            return StringUtils.isNotEmpty(role);
        }
    }
}
