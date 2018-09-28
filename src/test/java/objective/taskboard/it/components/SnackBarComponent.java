package objective.taskboard.it.components;

import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SnackBarComponent extends AbstractComponent {

    public static final String SNACK_BAR_TAG = "obj-snackbar";

    public SnackBarComponent(WebDriver driver, By by) {
        super(driver, by);
    }

    public void waitTitleToBe(String title) {
        waitTextInElement(getChildElementWhenExists(component(), cssSelector(".title")), title);
    }

    public void waitDescriptionsToBe(String... descriptions) {
        if (descriptions.length == 0) {
            waitUntilChildElementNotExists(component(), cssSelector(".description-item"));
        } else {
            List<WebElement> descriptionsElements = getChildrenElementsWhenTheyExists(component(), cssSelector(".description-item"));
            for(int i = 0; i < descriptions.length; i++)
                waitTextInElement(descriptionsElements.get(i), descriptions[i]);
        }
    }

}
