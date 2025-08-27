package iteration2;

import generators.RandomData;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest{

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();


        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
        ResponseSpecs.entityWasCreated())
                .post(null);


        // получение списка аккаунтов, проверка что аккаунт создан
//        given()
//                .header("Authorization", userAuthHeader)
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .get("http://localhost:4111/api/v1/customer/profile")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK)
//                .body("accounts", Matchers.notNullValue());
   }
}
