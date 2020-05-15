package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.authentication.OAuthSignature;
import util.ApiHelper;


import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class AddCardSteps {
    String token = "eyJraWQiOiJhY2FhODE1OC1iMzhhLTRmOTktYjAxMi04MTA1MGFkMDdiYWUiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIyOTg3MzA2OCIsImFjY2Vzc190eXBlIjoiQ1VTVE9NRVIiLCJjYXB0YWluXC9pZGVudGl0eV9pZCI6Mjk4NzMwNjgsImlzcyI6Imh0dHBzOlwvXC9pZGVudGl0eS5xYS5jYXJlZW0tZW5naW5lZXJpbmcuY29tXC8iLCJjYXB0YWluXC9jaXR5X2lkIjoxLCJjYXB0YWluXC9waG9uZV9udW1iZXIiOiI5NzE1ODg0MzI3OTEiLCJjYXB0YWluXC9jYXB0YWluX2lkIjoxMTkzNTY4NCwiY2FwdGFpblwvbGltb19pZCI6MTMsImNhcHRhaW5cL2NpdHlfbmFtZSI6IkR1YmFpIiwidXNlcl9pZCI6Mjk4NzMwNjgsImF6cCI6IjE3YjYwM2MyLWMwYjItNDcwOC1iM2E1LWFiNTZlMjg0YTg2OCIsInNjb3BlIjoiZWRnZV9jYXB0YWluIG9wZW5pZCBvZmZsaW5lX2FjY2VzcyBwYXltZW50cyBwcm9maWxlIHhjbWEgeGRtYSBhdXRoX3YxX3Rva2VuIGVtYWlsIiwiY2FwdGFpblwvY291bnRyeV9pZCI6MSwiZXhwIjoxNTg1NzIzOTgyLCJpYXQiOjE1ODU2Mzc1ODIsImp0aSI6IjFlMGE2NTk3LTRlNWEtNDVhZS04ZmY1LTEyNzRmNzMyYjRiOCJ9.Vn7t7WVCScqp4zEUDhzkSMMKcXmbAjAehYfoF_XXQYS7XAamk3Ie2vLDX-ntORsxFK864YSuPYwT46Sthd_Z1W2zkQDL_vMknr3duWSYlBb1VmTFOSogLWHsNj4rJmSnQl5iBNcT4spbsuLJbvA1oSS_Bm5yVfhewBEJ8a4VIcexhZ1gpEZSs-QJWAnxdvw9srJ69dK8FRzCzS7vrDXuIpFgleLZof7IBHxEuewtduIF1KkhFSaMmi-ZeQ8SAwSabg6C6WlSdU8CXeTXWN70R1wcsu7uFs_2oJy3-qxpnCREDmz27g6s0sOP0i8zqcH7vVHvPXfvQYxOqSE1eqpjrA";
    @Given("user is authenticated")
    public void userIsAuthenticated() {
    }

    @When("I add the card")
    public void iAddTheCard() {
        given().auth().oauth2(token, OAuthSignature.HEADER).header("Provider-Access-Key","6ba82ffa").when().get("https://consumer-api.careem-internal.com/v1/wallets").then().log().body();
            when().get("https://consumer-api.careem-internal.com/v1/wallets").
                then().log().body();

    }

    @Then("the card is added successfully")
    public void theCardIsAddedSuccessfully() {}


}

