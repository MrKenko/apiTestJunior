package iteration2.ui;

import api.models.CreateUserRequest;
import api.models.DepositUserResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest extends BaseUiTest {
    static String accountNumberUserFirst;
    static String accountNumberUserSecond;
    static double startAmount = 50;
    static double transferAmount = 10;
    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;
    static String alertTextEmptyField = BankAlert.FILL_ALL_FIELDS.getMessage();
    static String alertTextInvalidAmount = BankAlert.NO_USER_FOUND.getMessage();

    @BeforeAll
    public static void setupUserName() {
        CreateUserRequest userRequestFirst = AdminSteps.createUser();

        userAuthHeaderFirst = UserSteps.loginAndGetToken(userRequestFirst);
        accountNumberUserFirst = UserSteps.createAccountAndGetNumber(userAuthHeaderFirst);
        int userIdFirst = UserSteps.getUserAccount(userAuthHeaderFirst).getId();
        UserSteps.deposit(userAuthHeaderFirst, userIdFirst, startAmount);

        CreateUserRequest userRequestSecond = AdminSteps.createUser();
        userAuthHeaderSecond = UserSteps.loginAndGetToken(userRequestSecond);
        accountNumberUserSecond = UserSteps.createAccountAndGetNumber(userAuthHeaderSecond);
        authAsUser(userRequestFirst);
    }


    @Test
    public void userCanMakeTransfer() {

        //  Балансы ДО перевода
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthHeaderFirst);
        double receiverBalanceBefore = UserSteps.getUserBalance(userAuthHeaderSecond);

        new UserDashboard().open().makeATransfer().getMakeATransfer().shouldBe(Condition.visible).shouldHave(Condition.text("\uD83D\uDD04 Make a Transfer"));
        new TransferPage().open().selectAccount(accountNumberUserFirst).transfer(accountNumberUserFirst, accountNumberUserSecond, transferAmount)
                .checkAlertMessageAndAccept(BankAlert.SUCCESS_TRANSFER.format(transferAmount, accountNumberUserSecond));

        //Получение значения баланса и UI
        refresh();
        new TransferPage().open().selectAccount(accountNumberUserFirst);
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

        //  Балансы ДО перевода
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthHeaderFirst);
        double receiverBalanceBefore = UserSteps.getUserBalance(userAuthHeaderSecond);

        new TransferPage().open().selectAccount(accountNumberUserFirst).transfer(accountNumberUserFirst, receiverNumber, amount).checkAlertMessageAndAccept(textAlert);

        //Получение значения баланса и UI
        refresh();
        new TransferPage().open().selectAccount(accountNumberUserFirst);
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
