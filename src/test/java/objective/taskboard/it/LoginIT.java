package objective.taskboard.it;

import org.junit.Test;

import objective.taskboard.testUtils.LoginUtils;

public class LoginIT extends AbstractUIWithCoverageIntegrationTest {

    private static final String INCORRECT_USER_OR_PASSWORD = "Incorrect user or password";
    private static final String THE_PASSWORD_CAN_T_BE_EMPTY = "The password can't be empty";

    @Test
    public void givenProjectAdministratorUser_whenLogin_thenProjectAdministratorAccess() {
        MainPage mainPage = LoginUtils.doLoginAsProjectAdministrator(webDriver);

        mainPage.assertFollowupButtonIsVisible()
            .assertDashboardButtonIsVisible()
            .assertSizingImportButtonIsVisible();
    }

    @Test
    public void givenDeveloperUser_whenLogin_thenHasDeveloperAccess() {
        MainPage mainPage = LoginUtils.doLoginAsDeveloper(webDriver);

        mainPage.assertFollowupButtonIsNotVisible()
            .assertDashboardButtonIsVisible()
            .assertSizingImportButtonIsNotVisible();
    }

    @Test
    public void givenCustomerUser_whenLogin_thenHasCustomerAccess() {
        MainPage mainPage = LoginUtils.doLoginAsCustomer(webDriver);

        mainPage.assertFollowupButtonIsNotVisible()
            .assertDashboardButtonIsNotVisible()
            .assertSizingImportButtonIsNotVisible();
    }

    @Test
    public void givenIncorrectUser_whenLogin_thenHasIncorrectUserOrPasswordMessage() {
        LoginPage.to(webDriver)
            .login("unknownuser", "xxxxxxxx")
            .assertValidationMessage(INCORRECT_USER_OR_PASSWORD);
    }

    @Test
    public void givenNoPassword_whenLogin_thenHasPasswordCantBeEmpty() {
        LoginPage.to(webDriver)
            .login("foo", "")
            .assertValidationMessage(THE_PASSWORD_CAN_T_BE_EMPTY);
    }
}
