package requests.steps;

import models.*;
import requests.skelethon.Endpoint;
import requests.skelethon.reauests.CrudRequester;
import requests.skelethon.reauests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UserSteps {

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

    public static int createAccount(String authHeader) {
        return new CrudRequester(
                RequestSpecs.userSpec(authHeader),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");
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
                .get(0)
                .getBalance();
    }

    public static DepositUserResponse deposit(String authUser, int userId, double balance) {

        DepositUserRequest depositRequest = DepositUserRequest.builder()
                .id(userId)
                .balance(balance)
                .build();

        DepositUserResponse depositUserResponse = new ValidatedCrudRequester<DepositUserResponse>(RequestSpecs.userSpec(authUser), Endpoint.DEPOSIT, ResponseSpecs.requestReturnOk())
                .post(depositRequest);
        return depositUserResponse;
    }

    public static GetUserAccountResponse getUserAccount(String authHeader) {
        return new CrudRequester(
                RequestSpecs.userSpec(authHeader),
                Endpoint.GET_USER_ACCOUNT,
                ResponseSpecs.requestReturnOk()
        ).get()
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


    }
