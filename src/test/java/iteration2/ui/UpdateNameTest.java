package iteration2.ui;

import api.models.GetUserProfileResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.reauests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UpdateNamePage;
import ui.pages.UserDashboard;

public class UpdateNameTest extends BaseUiTest {
    static String userAuthHeader;
    static String defaultName = "Мурка";
    static String incorrectName = "";

    @Test
    @UserSession
    public void userCanUpdateName() {
        userAuthHeader = SessionStorage.getSteps().loginAndGetTokenForUi();

        new UpdateNamePage().open().updateName(defaultName).checkAlertMessageAndAccept(BankAlert.SUCCESS_UPDATE_NAME.getMessage());

        UserDashboard dashboard = new UserDashboard().open().getPage(UserDashboard.class);

        String profileName = dashboard.getProfileName().getText();
        String welcomeName = dashboard.getWelcomeName().getText();

        //Проверка имени в UI
        softly.assertThat(profileName).isEqualTo(defaultName);
        softly.assertThat(welcomeName).isEqualTo(defaultName);

        //Проверка имени в API
        GetUserProfileResponse getUserProfile = new ValidatedCrudRequester<GetUserProfileResponse>(RequestSpecs.userSpec(userAuthHeader), Endpoint.GET_USER_PROFILE, ResponseSpecs.requestReturnOk())
                .get();
        softly.assertThat(profileName).isEqualTo(getUserProfile.getName());
        softly.assertThat(welcomeName).isEqualTo(getUserProfile.getName());

        SessionStorage.clear();
    }

    @Test
    @UserSession
    public void userCanNotUpdateIncorrectName() {
        userAuthHeader = SessionStorage.getSteps().loginAndGetTokenForUi();

        new UpdateNamePage().open().updateName(incorrectName).checkAlertMessageAndAccept(BankAlert.ENTER_A_VALID_NAME.getMessage());

        UserDashboard dashboard = new UserDashboard().open().getPage(UserDashboard.class);

        String profileName = dashboard.getProfileName().getText();
        String welcomeName = dashboard.getWelcomeName().getText();

        //Проверка имени в UI
        softly.assertThat(profileName).isNotEqualTo(incorrectName);
        softly.assertThat(welcomeName).isNotEqualTo(incorrectName);

        //Проверка имени в API
        GetUserProfileResponse getUserProfile = new ValidatedCrudRequester<GetUserProfileResponse>(RequestSpecs.userSpec(userAuthHeader),
                Endpoint.GET_USER_PROFILE,
                ResponseSpecs.requestReturnOk())
                .get();
        softly.assertThat(profileName).isNotEqualTo(getUserProfile.getName());
        softly.assertThat(welcomeName).isNotEqualTo(getUserProfile.getName());

        SessionStorage.clear();
    }
}
