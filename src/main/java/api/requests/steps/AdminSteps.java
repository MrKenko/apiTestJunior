package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.reauests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

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

//    public static GetUserProfileResponse getAllUsers(){
//        return new CrudRequester(RequestSpecs.adminSpec(),
//                Endpoint.GET_ALL_USERS,
//                ResponseSpecs.requestReturnOk())
//                .get()
//                .extract()
//                .jsonPath()
//                .getList("", GetUserProfileResponse.class)
//                .get(0);
//    }

    public static List<CreateUserResponse> getAllUsers(){
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnOk()
        ).getAll(CreateUserResponse[].class);
    }

}