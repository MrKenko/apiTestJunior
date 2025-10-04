package iteration2.api;

import api.models.CreateUserRequest;
import api.models.DepositUserRequest;
import api.models.TransferUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.reauests.CrudRequester;
import api.requests.skelethon.reauests.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest extends BaseTest {
    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;
    static String userAuthHeaderWithOutAccount;
    static int userIdFirst;
    static int userIdSecond;
    static int userIdWithOutAccount;
    static int startDeposit = 300;


    @BeforeAll
    public static void setupUserName() {
        // Первый юзер
        CreateUserRequest userRequestFirst = AdminSteps.createUser();
        userAuthHeaderFirst = UserSteps.loginAndGetToken(userRequestFirst);
        userIdFirst = UserSteps.createAccount(userAuthHeaderFirst);

        // Второй юзер
        CreateUserRequest userRequestSecond = AdminSteps.createUser();
        userAuthHeaderSecond = UserSteps.loginAndGetToken(userRequestSecond);
        userIdSecond = UserSteps.createAccount(userAuthHeaderSecond);


        // Третий юзер
        CreateUserRequest userRequestWithOutAccount = AdminSteps.createUser();
        userAuthHeaderWithOutAccount = UserSteps.loginAndGetToken(userRequestWithOutAccount);
        userIdWithOutAccount = UserSteps.getUserProfile(userAuthHeaderWithOutAccount);

        // делаем депозит первому юзеру и проверяем что баланс поменялся

        double userFirstBalanceBeforeDeposit = UserSteps.getUserBalance(userAuthHeaderFirst);

        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userIdFirst)
                .balance(startDeposit)
                .build();

        new ValidatedCrudRequester<DepositUserRequest>(RequestSpecs.userSpec(userAuthHeaderFirst), Endpoint.DEPOSIT, ResponseSpecs.requestReturnOk())
                .post(depositRequest);

        double userFirstBalanceAfterDeposit = UserSteps.getUserBalance(userAuthHeaderFirst);

        assertThat(userFirstBalanceAfterDeposit)
                .as("Баланс увеличился на сумму перевода")
                .isCloseTo(userFirstBalanceBeforeDeposit + startDeposit, within(0.0001));
        ;
    }


    public static Stream<Arguments> userValidTransferData() {
        return Stream.of(
                Arguments.of(userAuthHeaderFirst, userAuthHeaderSecond, userIdFirst, userIdSecond, 50.0, 50.0f, 250.0f, 50.0f),
                Arguments.of(userAuthHeaderFirst, userAuthHeaderSecond, userIdFirst, userIdSecond, 0.01, 0.01f, 249.99f, 50.01f),
                Arguments.of(userAuthHeaderSecond, userAuthHeaderFirst, userIdSecond, userIdFirst, 10, 10.0f,  40.01f, 259.99f)
        );
    }

    @ParameterizedTest
    @MethodSource("userValidTransferData")
    public void userCanTransfer(String userAuthSender, String userAuthReceiver, int senderId, int receiverId, double transferBalance) {

        //  Балансы ДО перевода
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthSender);
        double receiverBalanceBefore = UserSteps.getUserBalance(userAuthReceiver);

        //Делаем перевод
        UserSteps.userMakeTransfer(userAuthSender,senderId,receiverId,transferBalance);
//
        //Балансы после перевода
        double senderAccountAfter = UserSteps.getUserBalance(userAuthSender);
        double receiverAccountAfter = UserSteps.getUserBalance(userAuthReceiver);


        softly.assertThat(senderAccountAfter)
                .as("Баланс отправителя уменьшился на сумму перевода")
                .isCloseTo(senderBalanceBefore - transferBalance, within(0.0001));

        softly.assertThat(receiverAccountAfter)
                .as("Баланс получателя увеличился на сумму перевода")
                .isCloseTo(receiverBalanceBefore + transferBalance, within(0.0001));
}

    public static Stream<Arguments> userInvalidTransferData() {
        return Stream.of(
                Arguments.of(userAuthHeaderFirst, userAuthHeaderSecond, userIdFirst, userIdSecond, 0),
                Arguments.of(userAuthHeaderFirst, userAuthHeaderSecond, userIdFirst, userIdSecond, -100)
        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidTransferData")
    public void userInvalidTransfer(String userAuthSender, String userAuthReceiver, int senderId, int receiverId, double transferBalance) {
        //  Балансы ДО перевода
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthSender);
        double receiverBalanceBefore = UserSteps.getUserBalance(userAuthReceiver);

        //Делаем перевод

        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(transferBalance)
                .build();

        new CrudRequester(RequestSpecs.userSpec(userAuthSender), Endpoint.TRANSFER, ResponseSpecs.requestReturnsBadRequestText("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferUserRequest);

        //Балансы после перевода
        double senderAccountAfter = UserSteps.getUserBalance(userAuthSender);
        double receiverAccountAfter = UserSteps.getUserBalance(userAuthReceiver);

        softly.assertThat(senderAccountAfter)
                .as("Баланс отправителя не уменьшился на сумму перевода")
                .isCloseTo(senderBalanceBefore, within(0.0001));

        softly.assertThat(receiverAccountAfter)
                .as("Баланс получателя не увеличился на сумму перевода")
                .isCloseTo(receiverBalanceBefore, within(0.0001));


    }
    @Test
    public void userInvalidTransferToUserWithoutAccount() {
        // У отправителя баланс ДО
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthHeaderFirst);

        // Перевод на юзера без аккаунта
        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(userIdFirst)
                .receiverAccountId(userIdWithOutAccount)
                .amount(50)
                .build();

        new CrudRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestText("Invalid transfer: insufficient funds or invalid accounts")
        ).post(transferUserRequest);

        // Баланс отправителя после перевода (не должен измениться)
        double senderBalanceAfter = UserSteps.getUserBalance(userAuthHeaderFirst);

        softly.assertThat(senderBalanceAfter)
                .as("Баланс отправителя не должен измениться при переводе на пользователя без аккаунта")
                .isCloseTo(senderBalanceBefore, within(0.0001));
    }

    @Test
    public void userWithoutAccountCantTransfer() {
        // У юреза с аккаунтом баланс ДО
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthHeaderFirst);

        // Перевод на юзера без аккаунта
        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(userIdWithOutAccount)
                .receiverAccountId(userIdFirst)
                .amount(50)
                .build();

        new CrudRequester(
                RequestSpecs.userSpec(userAuthHeaderWithOutAccount),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsForbiddenDeposit()
        ).post(transferUserRequest);

        // Баланс пользователя с аккаунтом после перевода (не должен измениться)
        double senderBalanceAfter = UserSteps.getUserBalance(userAuthHeaderFirst);

        softly.assertThat(senderBalanceAfter)
                .as("Баланс пользователя с аккаунтом не должен измениться при переводе на пользователя без аккаунта")
                .isCloseTo(senderBalanceBefore, within(0.0001));
    }

    public static Stream<Arguments> userInvalidTokenData() {
        return Stream.of(
                Arguments.of(userAuthHeaderFirst, userIdSecond, userIdFirst, 50), //перевод с другого id на свой
                Arguments.of("Basic YWRtaW46YWRtaW4=",  userIdFirst, userIdSecond, 50)  //токен админа
        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidTokenData")
    public void userInvalidTransferToken(String userAuthSender, int senderId, int receiverId, double transferBalance) {
        //  Балансы ДО перевода
        double senderBalanceBefore = UserSteps.getUserBalance(userAuthHeaderFirst);
        double receiverBalanceBefore = UserSteps.getUserBalance(userAuthHeaderSecond);

        //Делаем перевод

        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(transferBalance)
                .build();

        new CrudRequester(RequestSpecs.userSpec(userAuthSender),Endpoint.TRANSFER, ResponseSpecs.requestReturnsForbiddenDeposit())
                .post(transferUserRequest);

        //Балансы после перевода

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
