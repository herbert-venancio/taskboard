package objective.taskboard.it;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
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

import static org.openqa.selenium.support.PageFactory.initElements;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class TemplateFollowupDialog extends AbstractUiFragment {
    private WebElement newTemplateItem;
    private WebElement createButton;
    private WebElement templateNameInput;
    private WebElement templateFileInput;
    private List<WebElement> projectsCheckbox;
    
    private final String DEFAUT_ERROR_MESSAGE = "Make sure the name is not empty, at least one project has been selected, " + 
            "and that the template file has been uploaded.";
    
    @FindBy(css=".template-followup-button")
    private WebElement teplateFollowupButton;
    
    @FindBy(id="followupCrudModal")
    private WebElement dialog;
    
    public static TemplateFollowupDialog open(WebDriver webDriver) {
        return initElements(webDriver, TemplateFollowupDialog.class).open();
    }
    
    private TemplateFollowupDialog open() {
        teplateFollowupButton.click();
        waitVisibilityOfElement(dialog);
        newTemplateItem = dialog.findElement(By.id("newTemplateItem"));
        createButton = dialog.findElement(By.id("createTemplate"));
        templateNameInput = dialog.findElement(By.id("templateNameInputl"));
        projectsCheckbox = dialog.findElements(By.cssSelector("paper-checkbox"));
        templateFileInput = dialog.findElement(By.id("inputTemplate"));
        return this;
    }
    
    public TemplateFollowupDialog close() {
        dialog.findElement(By.cssSelector(".button-close")).click();
        return this;
    }

    public TemplateFollowupDialog(WebDriver driver) {
        super(driver);
    }
    
    public TemplateFollowupDialog tryToCreateATemplateWithoutName() {
        newTemplateItem.click();
        createButton.click();
        System.out.println("antes do asser error message");
        assertErrorMessage(DEFAUT_ERROR_MESSAGE);
        System.out.println("depois do asser error message");
        closeAlertDialog();
        System.out.println("fechou dialog do error message");
        return this;
    }
    
    public TemplateFollowupDialog tryToCreateATemplateWithoutSelectAProject() {
        newTemplateItem.click();
        templateNameInput.sendKeys("Template test");
        createButton.click();
        assertErrorMessage(DEFAUT_ERROR_MESSAGE);
        closeAlertDialog();
        return this;
    }
    
    public TemplateFollowupDialog tryToCreateATemplateWithoutSelectAFile() {
        newTemplateItem.click();
        projectsCheckbox.get(0).click();
        createButton.click();
        assertErrorMessage(DEFAUT_ERROR_MESSAGE);
        closeAlertDialog();
        return this;
    }
    
    public TemplateFollowupDialog createATemplate(Integer projectIndex) {
        String templateName = "Template Success Test";
        File file = null;
        
        try {
            file = new File(UploadTemplateIT.class.getResource("/objective/taskboard/followup/OkFollowupTemplate.xlsm").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        close();
        open();
        newTemplateItem.click();
        templateNameInput.sendKeys(templateName);
        projectsCheckbox.get(projectIndex).click();
        templateFileInput.sendKeys(file.toString());
        createButton.click();
        
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                List<WebElement> templates = dialog
                        .findElements(By.cssSelector(".template-item"));
                
                Optional<WebElement> templateCreated = templates
                        .stream()
                        .filter(t -> templateName.equals(t.getText()))
                        .findFirst();
                
                return templateCreated.isPresent(); 
            }
        });
        return this;
    }
    
    private void assertErrorMessage(String message) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                WebElement errorMessage = dialog.findElement(By.cssSelector("#alertModalTemplate .text"));
                waitVisibilityOfElement(errorMessage);
                return message.equals(errorMessage.getText()); 
            }
        });
    }
    
    private void closeAlertDialog() {
        dialog.findElement(By.cssSelector("#alertModalTemplate #ok")).click();
    }

}
