package iteration2.ui;

import api.models.CreateUserRequest;
import api.models.GetUserAccountResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.UserDashboard;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.$;

public class DepositTest extends BaseUiTest {
    static String userAuthHeader;
    static String accountNumberUser;
    static double defaultAmount = 50;
    static double invalidAmount = -50;
    static double emptyAmount;

    @BeforeAll
    public static void setupUserName() {
        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeader = UserSteps.loginAndGetToken(userRequestFirst);
        accountNumberUser = UserSteps.createAccountAndGetNumber(userAuthHeader);

        authAsUser(userRequestFirst);
    }


    @Test
    public void userCanMakeDepositTest() {

        new DepositPage().open().selectAccount(accountNumberUser).enterAmountAndConfirmDeposit(defaultAmount).checkAlertMessageAndAccept(BankAlert.SUCCESS_DEPOSIT.format(defaultAmount, accountNumberUser));

        //Проверка что сумма депозита равна сумме баланса в UI
        new DepositPage().open();
        double resultBalance = getBalanceAsDouble(accountNumberUser);
        softly.assertThat(defaultAmount).isEqualTo(resultBalance);

        //Проверка, что сумма депозита равна сумме баланса в API
        GetUserAccountResponse getUserBalance = UserSteps.getUserAccount(userAuthHeader);
        softly.assertThat(defaultAmount).isEqualTo(getUserBalance.getBalance());
    }


    public static Stream<Arguments> invalidDepositData() {
        return Stream.of(
                Arguments.of(invalidAmount),
                Arguments.of(emptyAmount)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userEnterInvalidDataTest(double amount) {

        //Получение баланса до попытки сделать депозит с невалидной суммой
        GetUserAccountResponse getUserBalanceBefore = UserSteps.getUserAccount(userAuthHeader);

        new UserDashboard().open().depositMoneyButton().getDepositMoneyButton().shouldBe(Condition.visible).shouldHave(Condition.text("\uD83D\uDCB0 Deposit Money"));
        new DepositPage().open().selectAccount(accountNumberUser).enterAmountAndConfirmDeposit(amount).checkAlertMessageAndAccept(BankAlert.INVALID_AMOUNT.getMessage());

        //Получение баланса после попытки сделать депозит с невалидной суммой
        GetUserAccountResponse getUserBalanceAfter = UserSteps.getUserAccount(userAuthHeader);

        //Проверка, что баланс не поменялся, после попытки сделать депозит с некорректным значением в UI
        new DepositPage().open();
        double resultBalance = getBalanceAsDouble(accountNumberUser);
        softly.assertThat(getUserBalanceAfter.getBalance()).isEqualTo(resultBalance);

        //Проверка, что баланс не поменялся, после попытки сделать депозит с некорректным значением в API
        softly.assertThat(getUserBalanceBefore.getBalance()).isEqualTo(getUserBalanceAfter.getBalance());
    }

    @Test
    public void userDontSelectAccountTest() {

        //Получение баланса до попытки сделать депозит с невалидной суммой
        GetUserAccountResponse getUserBalanceBefore = UserSteps.getUserAccount(userAuthHeader);

        new UserDashboard().open().depositMoneyButton().getDepositMoneyButton().shouldBe(Condition.visible).shouldHave(Condition.text("\uD83D\uDCB0 Deposit Money"));
        new DepositPage().open().enterAmountAndConfirmDeposit(defaultAmount).checkAlertMessageAndAccept(BankAlert.NOT_SELECT_ACCOUNT.getMessage());

        //Получение баланса после попытки сделать депозит не выбрав аккаунт
        GetUserAccountResponse getUserBalanceAfter = UserSteps.getUserAccount(userAuthHeader);

        //Проверка, что баланс не поменялся, после попытки сделать депозит не выбрав аккаунт в UI
        new DepositPage().open();
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
