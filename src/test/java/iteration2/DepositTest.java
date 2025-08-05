package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositTest {
    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;
    static String userAuthHeaderWithOutAccount;
    static String userNameFirst;
    static String userNameSecond;
    static String userNameWithOutAccount;
    static int userIdFirst;
    static int userIdSecond;
    static int userIdWithOutAccount;

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    @BeforeAll
    public static void setupUserName() {
        userNameFirst = "R11w1aq1q12171";
        userNameSecond = "R22w1aq1q12171";
        userNameWithOutAccount = "R33w1aq1q12171";
        //создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "%s",
                          "password": "Roma1123!",
                          "role": "USER"
                        }
                        """.formatted(userNameFirst))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //получение токена
        userAuthHeaderFirst = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        
                        {
                              "username": "%s",
                              "password": "Roma1123!"
                        }
                        """.formatted(userNameFirst))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        userIdFirst = given()
                .header("Authorization", userAuthHeaderFirst)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");


        //Создание второго пользователя с аккаунтом-счётом

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "%s",
                          "password": "Roma1123!",
                          "role": "USER"
                        }
                        """.formatted(userNameSecond))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //получение токена
        userAuthHeaderSecond = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        
                        {
                              "username": "%s",
                              "password": "Roma1123!"
                        }
                        """.formatted(userNameSecond))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        userIdSecond = given()
                .header("Authorization", userAuthHeaderSecond)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");


        // Создание пользователя без акаунт-счета
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "%s",
                          "password": "Roma1123!",
                          "role": "USER"
                        }
                        """.formatted(userNameWithOutAccount))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //получение токена
        userAuthHeaderWithOutAccount = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        
                        {
                              "username": "%s",
                              "password": "Roma1123!"
                        }
                        """.formatted(userNameWithOutAccount))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        userIdWithOutAccount = given()
                .header("Authorization", userAuthHeaderWithOutAccount)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");
    }




public static Stream<Arguments> userValidDeposit(){
        return Stream.of(
                Arguments.of(50.0, 50.0f),
                Arguments.of(0.01, 50.01f)
        );
}
    @ParameterizedTest
    @MethodSource("userValidDeposit")
    public void userCanDepositMoney(double depositBalance, float resultBalance ) {
        //депозит
        String body = String.format(Locale.ENGLISH, """
    {
      "id": %d,
      "balance": %.2f
    }
    """, userIdFirst, depositBalance);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderFirst)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(resultBalance))
        ;
    }

    public static Stream<Number> invalidDepositData() {
        return Stream.of(0, -100);
    }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userCanNotDeposit(int balance) {
        //депозит
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderFirst)
                .body("""
                        {
                        "id": %d,
                        "balance": %d
                        }
                        """.formatted(userIdFirst, balance))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
    public static Stream<String> invalidTokenData() {
        return Stream.of("Basic YWRtaW46YWRtaW4=", //токен Админа
                userAuthHeaderSecond,                      //токен другого пользователя
                userAuthHeaderWithOutAccount               //токен пользователя без аккаунт-счета
                );
    }
    @ParameterizedTest
    @MethodSource("invalidTokenData")
    public void userInvalidToken(String userAuth){
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuth)
                .body("""
                        {
                        "id": %d,
                        "balance": 50
                        }
                        """.formatted(userIdFirst))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

}
