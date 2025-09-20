package requests.steps;

import generators.RandomModelGenerator;
import models.*;
import requests.skelethon.Endpoint;
import requests.skelethon.reauests.CrudRequester;
import requests.skelethon.reauests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class AdminSteps {
    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        return userRequest;
    }

    public static GetUserProfileResponse getAllUsers(){
        return new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.GET_ALL_USERS,
                ResponseSpecs.requestReturnOk())
                .get()
                .extract()
                .jsonPath()
                .getList("", GetUserProfileResponse.class)
                .get(0);
    }

}