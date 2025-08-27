package specs;

import generators.RandomData;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.DepositUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.LoginUserRequester;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.requestSpecification;

public class RequestSpecs {
    private RequestSpecs(){};

    private static RequestSpecBuilder defaultRequestBuilder(){
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri("http://localhost:4111");
    }

    public static RequestSpecification unauthSpec(){
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec(){
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification userSpec(String autToken){
        return defaultRequestBuilder()
                .addHeader("Authorization", autToken)
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password){
        String userAuthHeader = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnOk())
                .post(LoginUserRequest.builder().username(username).password(password).build())
                .extract()
                .header("Authorization");

        return defaultRequestBuilder()
                .addHeader("Authorization", userAuthHeader)
                .build();
    }
}
