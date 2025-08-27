package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.LoginUserRequester;
import requests.UserProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class UpdateNameTest extends BaseTest{
    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;

    @BeforeAll
    public static void usersData(){
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
        UpdateProfileNameRequest updateProfileNameRequest = UpdateProfileNameRequest.builder()
                .name(updateName)
                .build();

        UpdateProfileNameResponse response = new UserProfileRequester(RequestSpecs.userSpec(userAuth), ResponseSpecs.requestReturnOk())
                .put(updateProfileNameRequest)
               .extract()
                .as(UpdateProfileNameResponse.class);

        // Проверяем, что имя обновилось
        softly.assertThat(response.getCustomer().getName())
                .as("Имя в профиле должно обновиться")
                .isEqualTo(updateName);
    }



    @Test
    public void updateNameWithInvalidToken() {
        UpdateProfileNameRequest updateProfileNameRequest = UpdateProfileNameRequest.builder()
                .name("Мурзик")
                .build();

        new UserProfileRequester(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsForbiddenDeposit())
                .put(updateProfileNameRequest);

        CreateUserResponse response = new UserProfileRequester(RequestSpecs.userSpec(userAuthHeaderFirst), ResponseSpecs.requestReturnOk())
                .get(null)
                .extract()
                .as(CreateUserResponse.class);



        // Проверяем, что имя не обновилось
        softly.assertThat(response.getName())
                .as("Имя в профиле не должно обновиться")
                .isNotEqualTo("Мурзик");

    }
}
