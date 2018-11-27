package objective.taskboard.it.components.guards;

import static objective.taskboard.it.AbstractUiFragment.waitUntil;

import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;

public class LeaveConfirmationGuard {

    private static final String DEFAULT_MESSAGE = "If you leave before saving, your changes will be lost.";

    public static void waitIsOpened(WebDriver webDriver) {
        waitUntil(webDriver, (w) -> {
            return isAlertOpened(webDriver) && DEFAULT_MESSAGE.equals(webDriver.switchTo().alert().getText());
        });
    }

    public static void waitIsClosed(WebDriver webDriver) {
        waitUntil(webDriver, (w) -> !isAlertOpened(webDriver) || !DEFAULT_MESSAGE.equals(webDriver.switchTo().alert().getText()));
    }

    private static boolean isAlertOpened(WebDriver webDriver) {
        try {
            webDriver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    public static void stay(WebDriver webDriver) {
        waitIsOpened(webDriver);
        webDriver.switchTo().alert().dismiss();
        waitIsClosed(webDriver);
    }

    public static void leave(WebDriver webDriver) {
        waitIsOpened(webDriver);
        webDriver.switchTo().alert().accept();
        waitIsClosed(webDriver);
    }

}
