package iteration2.api;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.GetUserAccountResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.reauests.CrudRequester;
import api.requests.skelethon.reauests.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
