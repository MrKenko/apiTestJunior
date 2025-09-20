package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import iteration2.api.BaseTest;
import models.CreateUserRequest;
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
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest extends BaseTest {
    static String accountNumberUserFirst;
    static String accountNumberUserSecond;
    static double startAmount = 50;
    static double transferAmount = 10;
    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;
    static String alertTextEmptyField = "❌ Please fill all fields and confirm.";
    static String alertTextInvalidAmount = "❌ No user found with this account number.";

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
    public void userCanMakeTransfer() {
        //Пред шаги
        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeaderFirst = UserSteps.loginAndGetToken(userRequestFirst);
        String accountNumberUserFirst = UserSteps.createAccountAndGetNumber(userAuthHeaderFirst);
        int userIdFirst = UserSteps.getUserProfile(userAuthHeaderFirst);
        UserSteps.deposit(userAuthHeaderFirst, userIdFirst, startAmount);

        CreateUserRequest userRequestSecond = AdminSteps.createUser();
        userAuthHeaderSecond = UserSteps.loginAndGetToken(userRequestSecond);
        String accountNumberUserSecond = UserSteps.createAccountAndGetNumber(userAuthHeaderSecond);

        //  Балансы ДО перевода
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthHeaderFirst);
        double receiverBalanceBefore = UserSteps.getUserBalance(userAuthHeaderSecond);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeaderFirst);

        Selenide.open("/dashboard");
        // ШАГИ теста
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumberUserFirst)).shouldBe(Condition.visible);
        $(Selectors.byText(accountNumberUserFirst)).click();
        $$(".form-group input").get(0).setValue(accountNumberUserFirst); // Recipient Name
        $$(".form-group input").get(1).setValue(accountNumberUserSecond);      // Recipient Account Number
        $$(".form-group input").get(2).setValue(String.valueOf(transferAmount));

        // Подтверждаем чекбокс
        $(Selectors.byAttribute("id", "confirmCheck")).click();

        // Жмём кнопку отправки
        $(Selectors.byText("🚀 Send Transfer")).click();
        Alert alert = switchTo().alert();

        //Проверка алерта
        softly.assertThat(alert.getText()).isEqualTo("✅ Successfully transferred $" + transferAmount + " to account " + accountNumberUserSecond + "!");
        alert.accept();

        //Получение значения баланса и UI
        refresh();
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumberUserFirst)).shouldBe(Condition.visible);
        double resultBalance = getBalanceAsDouble(accountNumberUserFirst);

        //Балансы после перевода
        double senderAccountAfter = UserSteps.getUserBalance(userAuthHeaderFirst);
        double receiverAccountAfter = UserSteps.getUserBalance(userAuthHeaderSecond);

        //Проверка что баланс поменялся на UI
        softly.assertThat(senderAccountAfter).isEqualTo(resultBalance);

        //Проверка баланса после перевода, на API
        softly.assertThat(senderAccountAfter)
                .as("Баланс отправителя уменьшился на сумму перевода")
                .isCloseTo(senderBalanceBefore - transferAmount, within(0.0001));

        softly.assertThat(receiverAccountAfter)
                .as("Баланс получателя увеличился на сумму перевода")
                .isCloseTo(receiverBalanceBefore + transferAmount, within(0.0001));
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


    public static Stream<Arguments> invalidDepositData() {
        return Stream.of(
                Arguments.of("", 10, alertTextEmptyField),
                Arguments.of("1", transferAmount, alertTextInvalidAmount)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userCanNotTransfer(String receiverNumber, double amount, String textAlert) {
        //Пред шаги
        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeaderFirst = UserSteps.loginAndGetToken(userRequestFirst);
        String accountNumberUserFirst = UserSteps.createAccountAndGetNumber(userAuthHeaderFirst);
        int userIdFirst = UserSteps.getUserProfile(userAuthHeaderFirst);
        UserSteps.deposit(userAuthHeaderFirst, userIdFirst, startAmount);

        CreateUserRequest userRequestSecond = AdminSteps.createUser();
        userAuthHeaderSecond = UserSteps.loginAndGetToken(userRequestSecond);
        String accountNumberUserSecond = UserSteps.createAccountAndGetNumber(userAuthHeaderSecond);

        //  Балансы ДО перевода
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthHeaderFirst);
        double receiverBalanceBefore = UserSteps.getUserBalance(userAuthHeaderSecond);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeaderFirst);

        Selenide.open("/dashboard");
        // ШАГИ теста
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumberUserFirst)).shouldBe(Condition.visible);
        $(Selectors.byText(accountNumberUserFirst)).click();
        $$(".form-group input").get(0).setValue(accountNumberUserFirst); // Recipient Name
        $$(".form-group input").get(1).setValue(receiverNumber);      // Recipient Account Number
        $$(".form-group input").get(2).setValue(String.valueOf(amount));
        // Подтверждаем чекбокс
        $(Selectors.byAttribute("id", "confirmCheck")).click();

        // Жмём кнопку отправки
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();

        //Проверка алерта
        softly.assertThat(alert.getText()).isEqualTo(textAlert);
        alert.accept();

        //Получение значения баланса и UI
        refresh();
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumberUserFirst)).shouldBe(Condition.visible);
        double resultBalance = getBalanceAsDouble(accountNumberUserFirst);

        //Проверка что баланс поменялся на UI
        softly.assertThat(senderBalanceBefore).isEqualTo(resultBalance);


        double senderBalanceAfter = UserSteps.getUserBalance(userAuthHeaderFirst);
        double receiverBalanceAfter = UserSteps.getUserBalance(userAuthHeaderSecond);

        softly.assertThat(senderBalanceAfter)
                .as("Баланс пользователя с аккаунтом не должен измениться при переводе на пользователя без аккаунта")
                .isCloseTo(senderBalanceBefore, within(0.0001));

        softly.assertThat(receiverBalanceAfter)
                .as("Баланс пользователя не должен измениться при переводе на пользователя без аккаунта")
                .isCloseTo(receiverBalanceBefore, within(0.0001));
    }
}
