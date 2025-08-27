package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
        CreateUserRequest userRequestFirst = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequestFirst);

        userAuthHeaderFirst = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnOk())
                .post(LoginUserRequest.builder()
                        .username(userRequestFirst.getUsername())
                        .password(userRequestFirst.getPassword())
                        .build())
                .extract()
                .header("Authorization");

        userIdFirst = new CreateAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        // Второй юзер
        CreateUserRequest userRequestSecond = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequestSecond);

        userAuthHeaderSecond = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnOk())
                .post(LoginUserRequest.builder()
                        .username(userRequestSecond.getUsername())
                        .password(userRequestSecond.getPassword())
                        .build())
                .extract()
                .header("Authorization");

        userIdSecond = new CreateAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderSecond),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        // Третий юзер
        CreateUserRequest userRequestWithOutAccount = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequestWithOutAccount);

        userAuthHeaderWithOutAccount = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnOk())
                .post(LoginUserRequest.builder()
                        .username(userRequestWithOutAccount.getUsername())
                        .password(userRequestWithOutAccount.getPassword())
                        .build())
                .extract()
                .header("Authorization");

        userIdWithOutAccount = new UserProfileRequester(
                RequestSpecs.userSpec(userAuthHeaderWithOutAccount),
                ResponseSpecs.requestReturnOk())
                .get(null)
                .extract()
                .path("id");

        double userFirstBalanceBeforeDeposit = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk())
                .get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();


        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userIdFirst)
                .balance(startDeposit)
                .build();

        new DepositRequester(RequestSpecs.userSpec(userAuthHeaderFirst), ResponseSpecs.requestReturnOk())
                .post(depositRequest);

        DepositUserResponse userFirstBalanceAfterDeposit = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk())
                .get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0);

        assertThat(userFirstBalanceAfterDeposit.getBalance())
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
        double senderBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthSender),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        double receiverBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthReceiver),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        //Делаем перевод

        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(transferBalance)
                .build();

        new TransferUserRequester(RequestSpecs.userSpec(userAuthSender), ResponseSpecs.requestReturnOk())
                .post(transferUserRequest);

        //Балансы после перевода

        DepositUserResponse senderAccountAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthSender),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0);

        DepositUserResponse receiverAccountAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthReceiver),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0);

        softly.assertThat(senderAccountAfter.getBalance())
                .as("Баланс отправителя уменьшился на сумму перевода")
                .isCloseTo(senderBalanceBefore - transferBalance, within(0.0001));

        softly.assertThat(receiverAccountAfter.getBalance())
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
        double senderBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthSender),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        double receiverBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthReceiver),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        //Делаем перевод

        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(transferBalance)
                .build();

        new TransferUserRequester(RequestSpecs.userSpec(userAuthSender), ResponseSpecs.requestReturnsBadRequestText("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferUserRequest);

        //Балансы после перевода
        DepositUserResponse senderAccountAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthSender),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0);

        DepositUserResponse receiverAccountAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthReceiver),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0);

        softly.assertThat(senderAccountAfter.getBalance())
                .as("Баланс отправителя не уменьшился на сумму перевода")
                .isCloseTo(senderBalanceBefore, within(0.0001));

        softly.assertThat(receiverAccountAfter.getBalance())
                .as("Баланс получателя не увеличился на сумму перевода")
                .isCloseTo(receiverBalanceBefore, within(0.0001));


    }
    @Test
    public void userInvalidTransferToUserWithoutAccount() {
        // У отправителя баланс ДО
        double senderBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        // Перевод на юзера без аккаунта
        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(userIdFirst)
                .receiverAccountId(userIdWithOutAccount)
                .amount(50)
                .build();

        new TransferUserRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnsBadRequestText("Invalid transfer: insufficient funds or invalid accounts")
        ).post(transferUserRequest);

        // Баланс отправителя после перевода (не должен измениться)
        double senderBalanceAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        softly.assertThat(senderBalanceAfter)
                .as("Баланс отправителя не должен измениться при переводе на пользователя без аккаунта")
                .isCloseTo(senderBalanceBefore, within(0.0001));
    }

    @Test
    public void userWithoutAccountCantTransfer() {
        // У юреза с аккаунтом баланс ДО
        double senderBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        // Перевод на юзера без аккаунта
        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(userIdWithOutAccount)
                .receiverAccountId(userIdFirst)
                .amount(50)
                .build();

        new TransferUserRequester(
                RequestSpecs.userSpec(userAuthHeaderWithOutAccount),
                ResponseSpecs.requestReturnsForbiddenDeposit()
        ).post(transferUserRequest);

        // Баланс пользователя с аккаунтом после перевода (не должен измениться)
        double senderBalanceAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

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
        double senderBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        double receiverBalanceBefore = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderSecond),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        //Делаем перевод

        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(transferBalance)
                .build();

        new TransferUserRequester(RequestSpecs.userSpec(userAuthSender), ResponseSpecs.requestReturnsForbiddenDeposit())
                .post(transferUserRequest);

        //Балансы после перевода

        double senderBalanceAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderFirst),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        softly.assertThat(senderBalanceAfter)
                .as("Баланс пользователя с аккаунтом не должен измениться при переводе на пользователя без аккаунта")
                .isCloseTo(senderBalanceBefore, within(0.0001));

        double receiverBalanceAfter = new GetUserAccountRequester(
                RequestSpecs.userSpec(userAuthHeaderSecond),
                ResponseSpecs.requestReturnOk()
        ).get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class)
                .get(0)
                .getBalance();

        softly.assertThat(receiverBalanceAfter)
                .as("Баланс пользователя не должен измениться при переводе на пользователя без аккаунта")
                .isCloseTo(receiverBalanceBefore, within(0.0001));
    }

}
