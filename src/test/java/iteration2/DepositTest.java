package iteration2;

import models.CreateUserRequest;
import models.DepositUserRequest;
import models.DepositUserResponse;
import models.GetUserAccountResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.reauests.CrudRequester;
import requests.skelethon.reauests.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;
    static String userAuthHeaderWithOutAccount;
    static int userIdFirst;
    static int userIdSecond;
    static int userIdWithOutAccount;

    @BeforeAll
    public static void setupUsersData() {
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
    }




    public static Stream<Arguments> userValidDeposit() {
        return Stream.of(
                Arguments.of(50.0),
                Arguments.of(0.01)
        );
    }

    @ParameterizedTest
    @MethodSource("userValidDeposit")
    public void userCanDepositMoney(double depositBalance) {

        DepositUserResponse depositUserResponse = UserSteps.deposit(userAuthHeaderFirst, userIdFirst, depositBalance);

        GetUserAccountResponse getUserAccountResponse = new CrudRequester(RequestSpecs.userSpec(userAuthHeaderFirst), Endpoint.GET_USER_ACCOUNT, ResponseSpecs.requestReturnOk())
                .get()
               .extract()
                .jsonPath()
                .getList("", GetUserAccountResponse.class)
               .get(0);

        //Проверка состояния
        ModelAssertions.assertThatModels(depositUserResponse,getUserAccountResponse).match();

    }


    public static Stream<Arguments> invalidDepositData() {
        return Stream.of(
                Arguments.of(0, "Invalid account or amount"),
                Arguments.of(-100, "Invalid account or amount")
                );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userCanNotDeposit(int balance, String errorValue) {

        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userIdFirst)
                .balance(balance)
                .build();

        // Состояние аккаунта до депозита
        GetUserAccountResponse getUserBalanceBefore = UserSteps.getUserAccount(userAuthHeaderFirst);

        new CrudRequester(RequestSpecs.userSpec(userAuthHeaderFirst), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsBadRequestText(errorValue))
                .post(depositRequest);
        // Сотояние аккаунта после попытки сделать депозит с невалидными значениями
        GetUserAccountResponse getUserBalanceAfter = UserSteps.getUserAccount(userAuthHeaderFirst);

        // Проверка что баланс не поменялся
        ModelAssertions.assertThatModels(getUserBalanceBefore,getUserBalanceAfter).match();
    }


    public static Stream<Arguments> invalidTokenData() {
        return Stream.of(Arguments.of("Basic YWRtaW46YWRtaW4=", userIdFirst), //токен Админа
                Arguments.of(userAuthHeaderSecond, userIdFirst),                      //токен другого пользователя
               Arguments.of(userAuthHeaderWithOutAccount, userIdFirst)               //токен пользователя без аккаунт-счета
                );
    }
    @ParameterizedTest
    @MethodSource("invalidTokenData")
    public void userInvalidToken(String userAuth, int userId) {

        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userId)
                .balance(50)
                .build();

        // Состояние аккаунта до депозита
        GetUserAccountResponse getUserBalanceBefore = UserSteps.getUserAccount(userAuthHeaderFirst);

        new CrudRequester(RequestSpecs.userSpec(userAuth), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsForbiddenDeposit())
                .post(depositRequest);

        // Сотояние аккаунта после попытки сделать депозит с невалидными значениями
        GetUserAccountResponse getUserBalanceAfter = UserSteps.getUserAccount(userAuthHeaderFirst);

        // Проверка что баланс не поменялся
        ModelAssertions.assertThatModels(getUserBalanceBefore,getUserBalanceAfter).match();


    }
}

