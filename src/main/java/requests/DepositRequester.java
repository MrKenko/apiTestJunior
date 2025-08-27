package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.DepositUserRequest;

import static io.restassured.RestAssured.given;

public class DepositRequester extends Request<DepositUserRequest> {
    public DepositRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse post(DepositUserRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(DepositUserRequest model) {
        return null;
    }

    @Override
    public ValidatableResponse put(DepositUserRequest model) {
        return null;
    }
}
