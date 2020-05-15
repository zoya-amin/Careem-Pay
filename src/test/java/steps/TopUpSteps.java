package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import util.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TopUpSteps {

    private static Response response;
    GlobalVariables globalVariables;
    String endpoint;
    String payload;
    Map headers = GlobalVariables.headers;
    String instrumentationId;

    @When("user choose amount {int} to top up via card")
    public void user_choose_amount_to_top_up_via_card(Integer topUpAmount) {
        {

            //headers
            headers.put("authorization", GlobalVariables.authorization);
            headers.put("X-Idempotency-Key", CommonMethods.generateRandomString());
            //get endpoint
            endpoint = Prop.readKeyValueFromPropFile("addCredit.topUp.card", GlobalVariables.ENDPOINT_PROP_FILE);

            // get paylaod
            payload = Payloads.topUpCard(topUpAmount, instrumentationId);

            // call the post API
            response = GlobalVariables.response = ApiHelper.postDetails(headers, payload, endpoint);
            CommonMethods.printResponse(response);
        }


    }

    @When("user get the instrumentation id")
    public void userGetTheInstrumentationId() {
    }

    @And("user select card")
    public void userSelectCard() {

    }

    @And("user select card {string}")
    public void userSelectCard(String cardNum) {
        //headers
        headers.put("authorization", GlobalVariables.authorization);
        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("instrumentation", GlobalVariables.ENDPOINT_PROP_FILE);

        // call the post API
        response = GlobalVariables.response = ApiHelper.getList(headers, endpoint);
        CommonMethods.printResponse(response);

        //Get instrumentation id
        List<Map> products = GlobalVariables.response.body().jsonPath().getList("data");
        Map product = CommonMethods.traverseMultiLevelMap(products, Arrays.asList(new String[]{"display"}), cardNum);
        instrumentationId = (String) product.get("id");


    }

    @And("user balance is current balance plus {int}")
    public void user_balance_is_current_balance_plus(Integer amount) {
        //get endpoint
        String endpoint = Prop.readKeyValueFromPropFile("wallet.getCurrentBalance", GlobalVariables.ENDPOINT_PROP_FILE);

        headers.put("authorization", GlobalVariables.authorization);

        // call API
        response = GlobalVariables.response = ApiHelper.getList(headers, endpoint);
        CommonMethods.printResponse(response);

        //verify response
        assertEquals(200, response.statusCode());
        JsonPath jsonResponse = response.body().jsonPath();
        int balanceAfter = jsonResponse.get("amount");
        System.out.println("balance before transaction" + GlobalVariables.currentBalance);
        assertEquals(GlobalVariables.currentBalance + amount, balanceAfter );
    }
}
