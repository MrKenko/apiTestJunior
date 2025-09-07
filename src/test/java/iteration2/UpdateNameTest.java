package iteration2;

import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.reauests.CrudRequester;
import requests.skelethon.reauests.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class UpdateNameTest extends BaseTest{
    static String userAuthHeaderFirst;
    static String userAuthHeaderSecond;

    @BeforeAll
    public static void usersData(){
            // Первый юзер
            CreateUserRequest userRequestFirst = AdminSteps.createUser();
            userAuthHeaderFirst = UserSteps.loginAndGetToken(userRequestFirst);

            // Второй юзер
            CreateUserRequest userRequestSecond = AdminSteps.createUser();
            userAuthHeaderSecond = UserSteps.loginAndGetToken(userRequestSecond);
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

        UpdateProfileNameResponse updateProfileNameResponse = new CrudRequester(RequestSpecs.userSpec(userAuth), Endpoint.UPDATE_PROFILE, ResponseSpecs.requestReturnOk())
                .put(updateProfileNameRequest)
                .extract()
                .as(UpdateProfileNameResponse.class);

        GetUserProfileResponse getUserProfile = new ValidatedCrudRequester<GetUserProfileResponse>(RequestSpecs.userSpec(userAuth), Endpoint.GET_USER_PROFILE, ResponseSpecs.requestReturnOk())
                .get();

        // Проверяем, что имя обновилось ("Имя в профиле должно обновиться")
        ModelAssertions.assertThatModels(updateProfileNameRequest,updateProfileNameResponse.getCustomer()).match();

        // Проверка состояния
        ModelAssertions.assertThatModels(updateProfileNameResponse.getCustomer(),getUserProfile).match();
    }



    @Test
    public void updateNameWithInvalidToken() {
        UpdateProfileNameRequest updateProfileNameRequest = UpdateProfileNameRequest.builder()
                .name("Мурзик")
                .build();

        // Состояние профиля до попытки поменять имя

        GetUserProfileResponse getUserProfileBefore = new ValidatedCrudRequester<GetUserProfileResponse>(RequestSpecs.userSpec(userAuthHeaderFirst), Endpoint.GET_USER_PROFILE,ResponseSpecs.requestReturnOk())
                .get();

        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.UPDATE_PROFILE, ResponseSpecs.requestReturnsForbiddenDeposit())
                .put(updateProfileNameRequest);

        CreateUserResponse response = new CrudRequester(RequestSpecs.userSpec(userAuthHeaderFirst), Endpoint.GET_USER_PROFILE, ResponseSpecs.requestReturnOk())
                .get()
                .extract()
                .as(CreateUserResponse.class);


        //Состояние профиля после попытки поменять имя
        GetUserProfileResponse getUserProfileAfter = new ValidatedCrudRequester<GetUserProfileResponse>(RequestSpecs.userSpec(userAuthHeaderFirst), Endpoint.GET_USER_PROFILE,ResponseSpecs.requestReturnOk())
                .get();

        // Проверяем, что имя не обновилось
        ModelAssertions.assertThatModels(getUserProfileBefore, getUserProfileAfter).match();


    }
}
