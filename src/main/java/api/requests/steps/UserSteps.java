package api.requests.steps;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.reauests.CrudRequester;
import api.requests.skelethon.reauests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private static String username;
    private static String password;
    private static String token;

    public UserSteps(String username, String password) {
        UserSteps.username = username;
        UserSteps.password = password;
        UserSteps.token = loginAndGetTokenForUi();
    }

    public static String loginAndGetToken(CreateUserRequest userRequest) {
        return new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnOk())
                .post(LoginUserRequest.builder()
                        .username(userRequest.getUsername())
                        .password(userRequest.getPassword())
                        .build())
                .extract()
                .header("Authorization");
    }

    public static String loginAndGetTokenForUi() {
        return new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnOk())
                .post(LoginUserRequest.builder()
                        .username(username)
                        .password(password)
                        .build())
                .extract()
                .header("Authorization");
    }

    public String getToken() {
        return token;
    }

    public static int createAccount(String authHeader) {
        return new CrudRequester(
                RequestSpecs.userSpec(authHeader),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");
    }
    public String createAccountAndGetNumberUi() {
        CreateAccountResponse response = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        return response.getAccountNumber();
    }

    public static String createAccountAndGetNumber(String authHeader) {
        CreateAccountResponse response = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.userSpec(authHeader),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        return response.getAccountNumber();
    }

    public static int getUserProfile(String authHeader) {

       return new CrudRequester(
                RequestSpecs.userSpec(authHeader),
                Endpoint.GET_USER_PROFILE,
                ResponseSpecs.requestReturnOk())
                .get()
                .extract()
                .path("id");
    }

    public static double getUserBalance(String authHeader) {
        return new CrudRequester(
                RequestSpecs.userSpec(authHeader),
                Endpoint.GET_USER_ACCOUNT,
                ResponseSpecs.requestReturnOk()
        ).get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountResponse.class)
                .getFirst()
                .getBalance();
    }

    public static DepositUserResponse deposit(String authUser, int userId, double balance) {

        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userId)
                .balance(balance)
                .build();

        return new ValidatedCrudRequester<DepositUserResponse>(RequestSpecs.userSpec(authUser), Endpoint.DEPOSIT, ResponseSpecs.requestReturnOk())
                .post(depositRequest);
    }

    public static DepositUserResponse depositForUi(int userId, double balance) {

        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userId)
                .balance(balance)
                .build();

        return new ValidatedCrudRequester<DepositUserResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnOk())
                .post(depositRequest);
    }

    public static GetUserAccountResponse getUserAccount(String authHeader) {
        return new CrudRequester(
                RequestSpecs.userSpec(authHeader),
                Endpoint.GET_USER_ACCOUNT,
                ResponseSpecs.requestReturnOk())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountResponse.class)
                .get(0);
    }

    public  GetUserAccountResponse getUserAccountUi() {
        return new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.GET_USER_ACCOUNT,
                ResponseSpecs.requestReturnOk())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountResponse.class)
                .get(0);
    }

    public static void userMakeTransfer(String authUser, int senderId, int receiverId, double transferBalance){
        TransferUserRequest transferUserRequest = TransferUserRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(transferBalance)
                .build();

        new ValidatedCrudRequester<TransferUserResponse>(RequestSpecs.userSpec(authUser), Endpoint.TRANSFER, ResponseSpecs.requestReturnOk())
                .post(transferUserRequest);

    }

    public  List<CreateAccountResponse> getAllAccounts(){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.GET_USER_ACCOUNT,
                ResponseSpecs.requestReturnOk())
                .getAll(CreateAccountResponse[].class);
    }


    }
