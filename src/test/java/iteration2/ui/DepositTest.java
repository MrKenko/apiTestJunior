package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import iteration2.api.BaseTest;
import models.CreateUserRequest;
import models.GetUserAccountResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositTest extends BaseTest {
    static String userAuthHeader;
    static double defaultAmount = 50;
    static double invalidAmount = -50;
    static double emptyAmount;
    static String notSelectAccountAlert = "❌ Please select an account.";
    static String invalidAmountAlert = "❌ Please enter a valid amount.";

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
    public void userCanMakeDepositTest() {

        //Пред шаги

        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeader = UserSteps.loginAndGetToken(userRequestFirst);
        String accountNumberUser = UserSteps.createAccountAndGetNumber(userAuthHeader);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeader);

        Selenide.open("/dashboard");

        //ШАГИ ТЕСТА
        //Юзер делает депозит
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumberUser)).shouldBe(Condition.visible);
        $(Selectors.byText(accountNumberUser)).click();
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(defaultAmount));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();

        //Проверка алерта
        softly.assertThat(alert.getText()).isEqualTo("✅ Successfully deposited $" + defaultAmount + " to account " + accountNumberUser + "!");
        alert.accept();

        //Проверка что сумма депозита равна сумме баланса в UI
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $(Selectors.byText("-- Choose an account --")).click();

        double resultBalance = getBalanceAsDouble(accountNumberUser);

        softly.assertThat(defaultAmount).isEqualTo(resultBalance);

        //Проверка, что сумма депозита равна сумме баланса в API
        GetUserAccountResponse getUserBalance = UserSteps.getUserAccount(userAuthHeader);
        softly.assertThat(defaultAmount).isEqualTo(getUserBalance.getBalance());
    }


    public static Stream<Arguments> invalidDepositData() {
        return Stream.of(
                Arguments.of(invalidAmount, invalidAmountAlert),
                Arguments.of(emptyAmount, invalidAmountAlert)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userEnterInvalidDataTest(double Amount, String alertText) {
        //Пред шаги

        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeader = UserSteps.loginAndGetToken(userRequestFirst);
        String accountNumberUser = UserSteps.createAccountAndGetNumber(userAuthHeader);
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeader);
        Selenide.open("/dashboard");

        //Получение баланса до попытки сделать депозит с невалидной суммой
        GetUserAccountResponse getUserBalanceBefore = UserSteps.getUserAccount(userAuthHeader);

        //Шаги теста
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumberUser)).shouldBe(Condition.visible);
        $(Selectors.byText(accountNumberUser)).click();
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(Amount));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        //Проверка алерта
        softly.assertThat(alertText).isEqualTo(alert.getText());
        alert.accept();

        //Получение баланса после попытки сделать депозит с невалидной суммой
        GetUserAccountResponse getUserBalanceAfter = UserSteps.getUserAccount(userAuthHeader);

        //Проверка, что баланс не поменялся, после попытки сделать депозит с некорректным значением в UI
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);


        double resultBalance = getBalanceAsDouble(accountNumberUser);

        softly.assertThat(getUserBalanceAfter.getBalance()).isEqualTo(resultBalance);

        //Проверка, что баланс не поменялся, после попытки сделать депозит с некорректным значением в API
        softly.assertThat(getUserBalanceBefore.getBalance()).isEqualTo(getUserBalanceAfter.getBalance());
    }

    @Test
    public void userDontSelectAccountTest() {

        //Пред шаги

        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeader = UserSteps.loginAndGetToken(userRequestFirst);
        String accountNumberUser = UserSteps.createAccountAndGetNumber(userAuthHeader);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeader);

        Selenide.open("/dashboard");

        //Получение баланса до попытки сделать депозит с невалидной суммой
        GetUserAccountResponse getUserBalanceBefore = UserSteps.getUserAccount(userAuthHeader);

        //ШАГИ ТЕСТА
        //Юзер делает депозит
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(defaultAmount));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();

        //Проверка алерта
        softly.assertThat(alert.getText()).isEqualTo(notSelectAccountAlert);
        alert.accept();

        //Получение баланса после попытки сделать депозит не выбрав аккаунт
        GetUserAccountResponse getUserBalanceAfter = UserSteps.getUserAccount(userAuthHeader);

        //Проверка, что баланс не поменялся, после попытки сделать депозит не выбрав аккаунт в UI
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);


        double resultBalance = getBalanceAsDouble(accountNumberUser);

        softly.assertThat(getUserBalanceAfter.getBalance()).isEqualTo(resultBalance);

        //Проверка, что баланс не поменялся, после попытки сделать депозит не выбрав аккаунт в API
        softly.assertThat(getUserBalanceBefore.getBalance()).isEqualTo(getUserBalanceAfter.getBalance());
    }


    public double getBalanceAsDouble(String accountNumber) {
        String text = $(Selectors.byText(accountNumber)).getText();

        // Разбиваем строку на "слова" по пробелам и скобкам
        String[] parts = text.split("[\\s()]+"); // ["ACC3", "Balance:", "$11.00"]

        // Находим часть, которая начинается с $
        String numberPart = Arrays.stream(parts)
                .filter(s -> s.startsWith("$"))
                .findFirst()
                .orElse("$0"); // если вдруг не нашли

        // Убираем $ и запятые
        numberPart = numberPart.replace("$", "").replace(",", "");

        return Double.parseDouble(numberPart);

    }
}
