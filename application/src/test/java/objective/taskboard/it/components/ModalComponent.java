package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;

public class ModalComponent extends AbstractComponent {

    private ButtonComponent closeButton;

    public ModalComponent(WebDriver driver, By by) {
        super(driver, by);
        closeButton = new ButtonComponent(driver, new ByChained(by, By.cssSelector(".modal__close obj-icon")));
    }

    public ModalComponent close() {
        closeButton.click();
        return this;
    }
}
