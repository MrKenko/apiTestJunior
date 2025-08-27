package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
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

        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userIdFirst)
                .balance(depositBalance)
                .build();

       DepositUserResponse depositUserResponse = new DepositRequester(RequestSpecs.userSpec(userAuthHeaderFirst), ResponseSpecs.requestReturnOk())
                .post(depositRequest)
                .extract()
                .as(DepositUserResponse.class);


       List<DepositUserResponse> accounts = new GetUserAccountRequester(RequestSpecs.userSpec(userAuthHeaderFirst), ResponseSpecs.requestReturnOk())
                .get(null)
               .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class);

        DepositUserResponse getUserAccountResponse = accounts.get(0);

        softly.assertThat(depositUserResponse.getBalance()).isEqualTo(getUserAccountResponse.getBalance());
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

        new DepositRequester(RequestSpecs.userSpec(userAuthHeaderFirst), ResponseSpecs.requestReturnsBadRequestText(errorValue))
                .post(depositRequest);

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


        new DepositRequester(RequestSpecs.userSpec(userAuth), ResponseSpecs.requestReturnsForbiddenDeposit())
                .post(depositRequest);


        List<DepositUserResponse> accounts = new GetUserAccountRequester(RequestSpecs.userSpec(userAuthHeaderFirst), ResponseSpecs.requestReturnOk())
                .get(null)
                .extract()
                .jsonPath()
                .getList("", DepositUserResponse.class);

        DepositUserResponse getUserAccountResponse = accounts.get(0);

        softly.assertThat(depositRequest.getBalance()).isNotEqualTo(getUserAccountResponse.getBalance());


    }}

