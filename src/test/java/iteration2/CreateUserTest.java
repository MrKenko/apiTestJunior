package iteration2;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.GetUserAccountResponse;
import models.GetUserProfileResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.reauests.CrudRequester;
import requests.skelethon.reauests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest{
    @Test
    public void adminCanCreateUserWithCorrectData(){
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);


        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        GetUserProfileResponse getUserProfileResponse = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.GET_USER_PROFILE,
                        ResponseSpecs.requestReturnOk())
                        .get()
                .extract()
                .as(GetUserProfileResponse.class);

        ModelAssertions.assertThatModels(createUserRequest,createUserResponse).match();

        //Проверка состояния
        ModelAssertions.assertThatModels(createUserResponse,getUserProfileResponse).match();



    }
public static Stream<Arguments> userInvalidData(){
        return Stream.of(
                Arguments.of(" ", "Password33!", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password33!", "USER", "username" , "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33!", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33!", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
}

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanCreateUserWithInvalidData(String username, String password, String role, String errorKey, String errorValue){
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

       new CrudRequester(RequestSpecs.adminSpec(),
        Endpoint.ADMIN_USER,
        ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }
}
