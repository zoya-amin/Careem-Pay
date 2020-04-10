package steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import util.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// to read json


public class EntertainmentVouchersSteps {
    static String skucode;
    static String invoiceId;
    static String orderId;
    static String currency;
    static String voucherAmount;
    static JsonPath jsonResponse;
    static String voucherCode;
    private static Response response;
    GlobalVariables globalVariables;
    String endpoint;
    String payload;
    static int currentBalance;
    static int floatingDigits;
    List headers= Arrays.asList();

    @And("user check the current balance")
    public void userCheckTheCurrentBalance() {
        //get endpoint
        String endpoint = Prop.readKeyValueFromPropFile("wallet.getCurrentBalance", GlobalVariables.ENDPOINT_PROP_FILE);
        // call API
        response = ApiHelper.getList(Arrays.asList("Authorization"), endpoint);

        //verify response
        CommonMethods.printResponse(response);
        assertEquals(200, response.statusCode());
         jsonResponse = response.body().jsonPath();
        currentBalance=  jsonResponse.get("amount");
        floatingDigits= jsonResponse.get("fractionDigits");
        assertEquals("AED", jsonResponse.get("currency"));
    }

    @When("user get all vouchers")
    public void userGetAllVoucher() throws IOException, JSONException {
        String endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getAllVoucherOptions", GlobalVariables.ENDPOINT_PROP_FILE);
        response = ApiHelper.getList(Arrays.asList("Authorization"), endpoint);
        CommonMethods.printResponse(response);
        assertEquals(200, response.statusCode());
        CommonMethods.verifyResponse(response, "EntertainmentVoucher.GetAllVoucherExpectedResponse");
    }

    @And("user select a voucher to generate invoice")
    public void userSelectAVoucherToGenerateInvoice(DataTable table) {
        List<String> data = table.asList();
        voucherAmount = data.get(1).toString().split(" ")[0] + "00";
        currency = data.get(1).toString().split(" ")[1];

        //get skucode from response for the respective voucher
        JsonPath jsonResponse = response.body().jsonPath();
        List<Map> products = jsonResponse.getList("data.brands[0].stores[0].products");
        Map product = traverseMultiLevelMap(products, Arrays.asList(new String[]{"price", "chargeable", "sale", "value"}), voucherAmount);
        skucode = (String) product.get("skucode");

        //get payload
        String payload = Payloads.generateInvoice();

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.generateInvoice", GlobalVariables.ENDPOINT_PROP_FILE);
        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", skucode);

        // call the post API
        response = ApiHelper.postDetails(Arrays.asList("Authorization", "Content-Type"), payload, updatedEndpoint);
        CommonMethods.printResponse(response);

        //verify the response
        assertEquals(200, response.statusCode());
        jsonResponse = response.body().jsonPath();
        invoiceId = jsonResponse.get("data.invoiceId");
        assertNotNull(invoiceId);
        orderId = jsonResponse.get("data.orderId");
        assertEquals(voucherAmount, jsonResponse.get("data.invoiceValue.value").toString());
        assertEquals(currency, jsonResponse.get("data.invoiceValue.currency"));
    }

    @And("user purchase the voucher")
    public void userPurchaseTheVoucher() {

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.purchaseVoucher", GlobalVariables.ENDPOINT_PROP_FILE);

        // get paylaod
        payload = Payloads.purchaseVoucher(invoiceId);

        // call the post API
        response = ApiHelper.postDetails(Arrays.asList("Authorization", "Content-Type", "X-Idempotency-Key"), payload, endpoint);
        CommonMethods.printResponse(response);

        //verify response
        assertEquals(201, response.statusCode());
        jsonResponse = response.body().jsonPath();
        assertNotNull(jsonResponse.get("id"));
        assertEquals(invoiceId,jsonResponse.get("invoiceId"));
        assertEquals(voucherAmount,jsonResponse.get("total.amount").toString());
        assertEquals(currency, jsonResponse.get("total.currency"));
        assertEquals(orderId,jsonResponse.get("tags.orderId.value"));
        assertEquals("Steam", jsonResponse.get("tags.voucherBrand.value"));
        voucherCode = jsonResponse.get("tags.voucherCode.value");
        assertNotNull(voucherCode);
    }

    @Then("user verify that the voucher is purchased successfully")
    public void userVerifyThatTheVoucherIsPurchasedSuccessfully() {

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getOrder", GlobalVariables.ENDPOINT_PROP_FILE);
        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", orderId);

        // call the post API
        response = ApiHelper.getList(Arrays.asList("Authorization", "Content-Type"), updatedEndpoint);
        CommonMethods.printResponse(response);

        //verify response
        assertEquals(200, response.statusCode());
        jsonResponse = response.body().jsonPath();
        String orderStatus= jsonResponse.get("data.orderStatus").toString();

        // wait for order status to change from created to completed
        while(orderStatus.equalsIgnoreCase("Created")){
            response = ApiHelper.getList(Arrays.asList("Authorization", "Content-Type"), updatedEndpoint);
            jsonResponse = response.body().jsonPath();
            orderStatus= jsonResponse.get("data.orderStatus").toString();
            System.out.println(orderStatus);
        }

        CommonMethods.verifyResponseWithIgnore(response, "EntertainmentVoucher.getOrder", Arrays.asList("orderId,voucherCode,voucherProduct.price,data.voucherProduct.price.chargeable.sale.value,data.voucherProduct.price.receivable.value,data.voucherProduct.price.receivableExcludingTax.value,data.voucherProduct.skucode"));
        assertEquals(orderId, jsonResponse.get("data.orderId"));
//        assertEquals(voucherCode,jsonResponse.get("data.voucherCode"));
        assertEquals(skucode,jsonResponse.get("data.voucherProduct.skucode"));
        assertEquals(voucherAmount,jsonResponse.get("data.voucherProduct.price.receivable.value").toString());

    }

    @And("user CPAY account is deducted with voucher amount")
    public void userCPAYAccountIsDeductedWithAmount() {
        //get endpoint
        String endpoint = Prop.readKeyValueFromPropFile("wallet.getCurrentBalance", GlobalVariables.ENDPOINT_PROP_FILE);

        // call API
        response = ApiHelper.getList(Arrays.asList("Authorization"), endpoint);
        CommonMethods.printResponse(response);

        //verify response
        assertEquals(200, response.statusCode());
        jsonResponse = response.body().jsonPath();
        int balanceAfter=  jsonResponse.get("amount");
        System.out.println("balance before transaction"+currentBalance);
        assertEquals(balanceAfter,currentBalance-Integer.parseInt(voucherAmount));
    }


    Map traverseMultiLevelMap(List<Map> mapToTraverse, List keys, String valueToFind) {
        Map level;
        for (int i = 0; i < mapToTraverse.size(); i++) {
            level = mapToTraverse.get(i);
            for (int j = 0; j < keys.size() - 1; j++) {
                if (level.keySet().toString().contains(keys.get(j).toString())) {
                    level = (Map) level.get(keys.get(j));
                    System.out.println(level.keySet());
                } else {
                    System.out.println("Key is not found in the Map" + keys.get(j).toString());
                    return null;
                }
            }
            if (level.get(keys.get(keys.size() - 1)).toString().equals(valueToFind)) {
                return (Map) mapToTraverse.get(i);
            }

        }
        System.out.println("value  not found in the map ");
        return null;

    }


}