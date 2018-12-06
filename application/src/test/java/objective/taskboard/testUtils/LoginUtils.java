package objective.taskboard.testUtils;

import org.openqa.selenium.WebDriver;

import objective.taskboard.it.LoginPage;
import objective.taskboard.it.MainPage;

public class LoginUtils {

    public static final MainPage doLoginAsAdmin(WebDriver webDriver) {
        LoginPage loginPage = LoginPage.to(webDriver);
        loginPage.login("admin", "admin");

        return doLoginAndWaitIsOpened(webDriver, "Admin");
    }

    public static final MainPage doLoginAsProjectAdministrator(WebDriver webDriver) {
        LoginPage loginPage = LoginPage.to(webDriver);
        loginPage.login("foo", "bar");

        return doLoginAndWaitIsOpened(webDriver, "Foo");
    }

    public static final MainPage doLoginAsDeveloper(WebDriver webDriver) {
        LoginPage loginPage = LoginPage.to(webDriver);
        loginPage.login("thomas.developer", "thomas.developer");

        return doLoginAndWaitIsOpened(webDriver, "Thomas.developer");
    }

    public static final MainPage doLoginAsCustomer(WebDriver webDriver) {
        LoginPage loginPage = LoginPage.to(webDriver);
        loginPage.login("albert.customer", "albert.customer");

        return doLoginAndWaitIsOpened(webDriver, "Albert.customer");
    }

    private static MainPage doLoginAndWaitIsOpened(WebDriver webDriver, String userName) {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.waitUserLabelToBe(userName);
        mainPage.lane("Operational")
            .boardStep("Done")
            .assertIssueList(
                    "TASKB-638",
                    "TASKB-678",
                    "TASKB-679",
                    "TASKB-656",
                    "TASKB-657",
                    "TASKB-658",
                    "TASKB-660",
                    "TASKB-662"
                    );
        return mainPage;
    }

}
