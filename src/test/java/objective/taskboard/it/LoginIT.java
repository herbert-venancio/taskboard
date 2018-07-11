package objective.taskboard.it;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoginIT extends AbstractUIWithCoverageIntegrationTest {
    
    private static final String INCORRECT_USER_OR_PASSWORD = "Incorrect user or password";
    private static final String THE_PASSWORD_CAN_T_BE_EMPTY = "The password can't be empty";
    private LoginPage loginPage;
    private MainPage mainPage;
    
    @Before
    public void beforeTest() {
        loginPage = LoginPage.to(webDriver);
        mainPage = MainPage.produce(webDriver);
    }
    
    @After
    public void afterTest() {
        loginPage = null;
        mainPage = null;
    }
    
    @Test
    public void givenAdminUser_whenLogin_thenHasAdminAccess() {
        loginPage.login("foo", "bar");

        mainPage.waitUserLabelToBe("Foo");
        mainPage.assertFollowupButtonIsVisible()
            .assertDashboardButtonIsVisible()
            .assertSizingImportButtonIsVisible();
    }

    @Test
    public void givenDeveloperUser_whenLogin_thenHasDeveloperAccess() {
        loginPage.login("thomas.developer", "thomas.developer");

        mainPage.waitUserLabelToBe("Thomas.developer");
        mainPage.assertFollowupButtonIsNotVisible()
            .assertDashboardButtonIsVisible()
            .assertSizingImportButtonIsNotVisible();
    }

    @Test
    public void givenCustomerUser_whenLogin_thenHasCustomerAccess() {
        loginPage.login("albert.customer", "albert.customer");

        mainPage.waitUserLabelToBe("Albert.customer");
        mainPage.assertFollowupButtonIsNotVisible()
            .assertDashboardButtonIsNotVisible()
            .assertSizingImportButtonIsNotVisible();
    }
    
    @Test
    public void givenIncorrectUser_whenLogin_thenHasIncorrectUserOrPasswordMessage() {
        loginPage
            .login("unknownuser", "xxxxxxxx")
            .assertValidationMessage(INCORRECT_USER_OR_PASSWORD);
    }
    
    @Test
    public void givenNoPassword_whenLogin_thenHasPasswordCantBeEmpty() {
        loginPage
            .login("foo", "")
            .assertValidationMessage(THE_PASSWORD_CAN_T_BE_EMPTY);
    }
}
