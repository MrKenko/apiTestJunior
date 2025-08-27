package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.TransferUserRequest;

import static io.restassured.RestAssured.given;

public class TransferUserRequester extends Request<TransferUserRequest>{
    public TransferUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(TransferUserRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(TransferUserRequest model) {
        return null;
    }

    @Override
    public ValidatableResponse put(TransferUserRequest model) {
        return null;
    }
}
