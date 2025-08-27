package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferTest {
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
        userNameFirst = "Kqsate59982";
        userNameSecond = "Kwsate69982";
        userNameWithOutAccount = "Kesate79982";
        //создание пользователя 1
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
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("id");

//депозит первому юзеру
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderFirst)
                .body("""
                        {
                        "id": %d,
                        "balance": 300
                        }
                        """.formatted(userIdFirst))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(300.0f));

//Проверка, что баланс обновился
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderFirst)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance[0]", Matchers.equalTo(300.0f));
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
    public void userCanTransfer(String userAuthSender, String userAuthReceiver, int senderId, int receiverId, double transferBalance, float resultAmount, float resultBalanceFirstUser, float resultBalanceSecondUser) {
        String body = String.format(Locale.ENGLISH, """
                  {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %.2f
                }
                """, senderId, receiverId, transferBalance);
//перевод
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthSender)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("amount", Matchers.equalTo(resultAmount));

        //проверка баланса счета первого юзера

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthSender)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance[0]", Matchers.equalTo(resultBalanceFirstUser));

        // Проверка баланса счета второго юзера

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthReceiver)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance[0]", Matchers.equalTo(resultBalanceSecondUser));

    }

    public static Stream<Arguments> userInvalidTransferData() {
        return Stream.of(
                Arguments.of(userAuthHeaderFirst, userIdFirst, userIdSecond, 0),
                Arguments.of(userAuthHeaderFirst, userIdFirst, userIdWithOutAccount, 50), //перевод на id без аккаунта
                Arguments.of(userAuthHeaderFirst,  userIdFirst, userIdSecond, -100)
        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidTransferData")
    public void userInvalidTransfer(String userAuthSender, int senderId, int receiverId, double transferBalance) {
        String body = String.format(Locale.ENGLISH, """
                  {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %.2f
                }
                """, senderId, receiverId, transferBalance);
//перевод
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthSender)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

// Проверка, что баланс не поменялся
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderFirst)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance[0]", Matchers.equalTo(259.99f));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderSecond)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance[0]", Matchers.equalTo(40.01f));

    }

    public static Stream<Arguments> userInvalidTokenData() {
        return Stream.of(
                Arguments.of(userAuthHeaderWithOutAccount, userIdWithOutAccount, userIdFirst, 50), //перевод с id без аккаунта
                Arguments.of(userAuthHeaderFirst, userIdSecond, userIdFirst, 50), //перевод с другого id на свой
                Arguments.of("Basic YWRtaW46YWRtaW4=",  userIdFirst, userIdSecond, 50)  //токен админа
        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidTokenData")
    public void userInvalidTransferToken(String userAuthSender, int senderId, int receiverId, double transferBalance) {
        String body = String.format(Locale.ENGLISH, """
                  {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %.2f
                }
                """, senderId, receiverId, transferBalance);
//перевод
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthSender)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);

//Проверка, что баланс не поменялся
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderFirst)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance[0]", Matchers.equalTo(259.99f));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderSecond)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance[0]", Matchers.equalTo(40.01f));
    }

}
