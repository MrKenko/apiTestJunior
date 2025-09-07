package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),


    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    DEPOSIT(
            "/accounts/deposit",
           DepositUserRequest.class,
           DepositUserResponse.class
    ),

    TRANSFER(
            "/accounts/transfer",
            TransferUserRequest.class,
            TransferUserResponse.class
    ),

    UPDATE_PROFILE(
            "/customer/profile",
            UpdateProfileNameRequest.class,
            UpdateProfileNameResponse.class
    ),

    GET_USER_PROFILE(
            "/customer/profile",
            BaseModel.class,
            GetUserProfileResponse.class
    ),

    GET_USER_ACCOUNT(
            "/customer/accounts",
            BaseModel.class,
            GetUserAccountResponse.class
    ),

    GET_ALL_USERS(
            "/admin/users",
            BaseModel.class,
            GetUserProfileResponse.class
    )

    ;





    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
