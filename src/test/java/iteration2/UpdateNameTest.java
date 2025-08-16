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
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UpdateNameTest {
    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;
    static String userNameFirst;
    static String userNameSecond;


    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    @BeforeAll
    public static void setupUserName() {
        userNameFirst = "R11w1aq1q112171";
        userNameSecond = "R11w1aq1q122171";

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

        //создание пользователя 2
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
    }

    public static Stream<Arguments> userValidNameAccountData() {
        return Stream.of(
                Arguments.of(userAuthHeaderFirst, "Murka"),  //англ буквы + верхний и нижний регистр
                Arguments.of(userAuthHeaderSecond, "Murka"),  //проверка уникальности
                Arguments.of(userAuthHeaderFirst, "Мурка"), //русскике буквы
                Arguments.of(userAuthHeaderFirst, "ムルカ"), //японские буквы
                Arguments.of(userAuthHeaderFirst, "M"), //короткое имя
                Arguments.of(userAuthHeaderFirst, "аааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааа"), //длинное имя
                Arguments.of(userAuthHeaderFirst, "Murka123!%,._-+"), //разные типы
                Arguments.of(userAuthHeaderFirst, ""), //пустая строка
                Arguments.of(userAuthHeaderFirst, "   ") //пробелы
        );
    }

    @ParameterizedTest
    @MethodSource("userValidNameAccountData")
    public void userCanUpdateAccountName(String userAuth, String updateName) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuth)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(updateName))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", equalTo(updateName));

        //Проверка, что имя обновилось
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuth)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(updateName));

    }

    @Test
    public void updateNameWithInvalidToken() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "name": "Мурзик"
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        //Проверка, что имя не поменялось у первого пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderFirst)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.not(equalTo("Мурзик")));

        //Проверка, что имя не поменялось у второго пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeaderSecond)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.not(equalTo("Мурзик")));
    }
}
