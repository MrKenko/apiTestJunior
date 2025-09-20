package iteration2.api;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.GetUserAccountResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.reauests.CrudRequester;
import requests.skelethon.reauests.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest{

    @Test
    public void userCanGenerateAuthTokenTest() {

        CreateUserRequest userRequest = AdminSteps.createUser();


        CreateAccountResponse createAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
        Endpoint.ACCOUNTS,
        ResponseSpecs.entityWasCreated())
                .post(null)
        ;

        GetUserAccountResponse getUserAccountResponse = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.GET_USER_ACCOUNT,
                ResponseSpecs.requestReturnOk())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserAccountResponse.class)
                .get(0);

        //проверка что аккаунт создан

        ModelAssertions.assertThatModels(createAccountResponse,getUserAccountResponse).match();

   }
}
