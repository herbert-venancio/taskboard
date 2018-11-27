package objective.taskboard.it.components.guards;

import static objective.taskboard.it.AbstractIntegrationTest.getAppBaseUrl;
import static objective.taskboard.it.AbstractUiFragment.waitUntil;

import org.openqa.selenium.WebDriver;

public class AccessGuard {

    public static void assertCantAccess(WebDriver webDriver, String url) {
        webDriver.get(url);
        waitUntil(webDriver, (w) -> {
            return !url.equals(webDriver.getCurrentUrl()) && getAppBaseUrl().equals(webDriver.getCurrentUrl());
        });
    }

}
