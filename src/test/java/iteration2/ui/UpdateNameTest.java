package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import iteration2.api.BaseTest;
import models.CreateUserRequest;
import models.GetUserProfileResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import requests.skelethon.Endpoint;
import requests.skelethon.reauests.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;

public class UpdateNameTest extends BaseTest {
    static String userAuthHeader;
    static String defaultName = "Мурка";
    static String incorrectName = "";
    static String alertHappy = "✅ Name updated successfully!";
    static String alertNegative = "❌ Please enter a valid name.";


    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://192.168.1.9:4444/wd/hub";
        Configuration.baseUrl = "http://172.23.96.1:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    public void userCanUpdateName() {
        //Пред шаги
        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeader = UserSteps.loginAndGetToken(userRequestFirst);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeader);

        Selenide.open("/dashboard");

        //Шаги теста
        $(By.cssSelector(".profile-header .user-name")).click();
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(String.valueOf(defaultName));
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        //Проверка алерта
        softly.assertThat(alert.getText()).isEqualTo(alertHappy);
        alert.accept();

        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        refresh();

        String profileName = $(".user-name").getText(); // возьмёт из хедера блока
        String welcomeName = $(".welcome-text span").getText(); // возьмёт из welcome

        //Проверка имени в UI
        softly.assertThat(profileName).isEqualTo(defaultName);
        softly.assertThat(welcomeName).isEqualTo(defaultName);

        //Проверка имени в API
        GetUserProfileResponse getUserProfile = new ValidatedCrudRequester<GetUserProfileResponse>(RequestSpecs.userSpec(userAuthHeader), Endpoint.GET_USER_PROFILE, ResponseSpecs.requestReturnOk())
                .get();
        softly.assertThat(profileName).isEqualTo(getUserProfile.getName());
        softly.assertThat(welcomeName).isEqualTo(getUserProfile.getName());
    }

    @Test
    public void userCanNotUpdateIncorrectName() {
        //Пред шаги
        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeader = UserSteps.loginAndGetToken(userRequestFirst);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeader);

        Selenide.open("/dashboard");

        //Шаги теста
        $(By.cssSelector(".profile-header .user-name")).click();
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(String.valueOf(incorrectName));
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        //Проверка алерта
        softly.assertThat(alert.getText()).isEqualTo(alertNegative);
        alert.accept();

        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        refresh();

        String profileName = $(".user-name").getText(); // возьмёт из хедера блока
        String welcomeName = $(".welcome-text span").getText(); // возьмёт из welcome

        //Проверка имени в UI
        softly.assertThat(profileName).isNotEqualTo(incorrectName);
        softly.assertThat(welcomeName).isNotEqualTo(incorrectName);

        //Проверка имени в API
        GetUserProfileResponse getUserProfile = new ValidatedCrudRequester<GetUserProfileResponse>(RequestSpecs.userSpec(userAuthHeader), Endpoint.GET_USER_PROFILE, ResponseSpecs.requestReturnOk())
                .get();
        softly.assertThat(profileName).isNotEqualTo(getUserProfile.getName());
        softly.assertThat(welcomeName).isNotEqualTo(getUserProfile.getName());
    }
}
