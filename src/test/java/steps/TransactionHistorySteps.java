package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import util.ApiHelper;
import util.CommonMethods;
import util.GlobalVariables;
import util.Prop;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.*;
import static org.junit.Assert.assertEquals;

public class TransactionHistorySteps {
    private static Response response;
    String endpoint;
    String payload;
    Map headers = GlobalVariables.headers;
    int transactionsCount;

    @When("user get transactions history")
    public void userGetTransactionsHistory() {
              //header
        headers.put("authorization", GlobalVariables.authorization);
        headers.put("Provider-Access-Key", "6ba82ffa");

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getTransactionsHistory", GlobalVariables.ENDPOINT_PROP_FILE);

        // call the post API
        GlobalVariables.response = ApiHelper.getListConsumer(headers, endpoint);

        CommonMethods.printResponse(GlobalVariables.response);
    }

    @And("user verify that transaction currency is {string}")
    public void userVerifyThatTransactionCurrencyIs(String currencyType) {
        ArrayList transList = GlobalVariables.response.body().jsonPath().get("transactionsList");
        String currency = (String) ((Map) transList.get(0)).get("currency");
        assertEquals(currencyType, currency);

    }


    @And("user verify latest transaction in transaction history is {string}")
    public void userVerifyLatestTransactionInTransactionHistoryIs(String transactionAmount) {
        String actualAmount = GlobalVariables.response.body().jsonPath().get("transactionsList[0].amount").toString();
        assertEquals(actualAmount, transactionAmount);
    }

    @Given("user wait for transaction list to be updated with {string}")
    public void userWaitForTransactionListToBeUpdatedWith(String transactionAmount) {
        await().atMost(300, SECONDS).with().pollInterval(10, TimeUnit.SECONDS).until(TransactionListIsUpdatedWith(transactionAmount));
    }

    private Callable<Boolean> TransactionListIsUpdatedWith(String transactionAmount) {
        return () -> {
            {
                headers.put("authorization", GlobalVariables.authorization);
                headers.put("Provider-Access-Key",  CommonMethods.generateRandomString());
                endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getTransactionsHistory", GlobalVariables.ENDPOINT_PROP_FILE);

                response=GlobalVariables.response = ApiHelper.getListConsumer(headers, endpoint);
                String actualAmount = GlobalVariables.response.body().jsonPath().get("transactionsList[0].amount").toString();
                System.out.println(actualAmount);
                return actualAmount.equals(transactionAmount);
            }
        };
    }




    @When("user get transactions history with unauthenticated user")
    public void userGetTransactionsHistoryWithUnauthenticatedUser() {
        //header
        headers.put("authorization", GlobalVariables.expiredAuthorization);
        headers.put("Provider-Access-Key",CommonMethods.generateRandomString());

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getTransactionsHistory", GlobalVariables.ENDPOINT_PROP_FILE);

        // call the post API
        GlobalVariables.response = ApiHelper.getListConsumer(headers, endpoint);

        CommonMethods.printResponse(GlobalVariables.response);
    }
}
