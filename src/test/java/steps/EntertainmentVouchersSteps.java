package steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import util.*;

import javax.xml.bind.annotation.XmlElementDecl;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class EntertainmentVouchersSteps {
    static String skucode;
    static String invoiceId;
    static String orderId;
    static String currency;
    static String voucherAmount;
    static JsonPath jsonResponse;
    static String voucherCode;
    static int currentBalance;
    static int floatingDigits;
    private static Response response;
    GlobalVariables globalVariables;
    String endpoint;
    String payload;
    Map headers = GlobalVariables.headers;

    @And("user check the current balance")
    public void userCheckTheCurrentBalance() {
        //headers
        headers.put("authorization", GlobalVariables.authorization);

        //get endpoint
        String endpoint = Prop.readKeyValueFromPropFile("wallet.getCurrentBalance", GlobalVariables.ENDPOINT_PROP_FILE);
        // call API
        response = GlobalVariables.response = ApiHelper.getList(headers, endpoint);

        //verify response
        CommonMethods.printResponse(response);
        assertEquals(200, response.statusCode());
        jsonResponse = response.body().jsonPath();
        currentBalance = jsonResponse.get("amount");
        GlobalVariables.currentBalance= currentBalance;
        floatingDigits = jsonResponse.get("fractionDigits");
        assertEquals("AED", jsonResponse.get("currency"));
    }

    @When("user get all vouchers")
    public void userGetAllVoucher() throws IOException, JSONException {
        //headers
        headers.put("authorization", GlobalVariables.authorization);
        String endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getAllVoucherOptions", GlobalVariables.ENDPOINT_PROP_FILE);
        GlobalVariables.response = ApiHelper.getList(headers, endpoint);
        CommonMethods.printResponse(GlobalVariables.response);
        assertEquals(200, GlobalVariables.response.statusCode());
        CommonMethods.verifyResponse(GlobalVariables.response, "EntertainmentVoucher.GetAllVoucherExpectedResponse");
    }

    @And("user select a voucher to generate invoice")
    public void userSelectAVoucherToGenerateInvoice(DataTable table) {
        List<String> data = table.asList();
        voucherAmount = data.get(1).toString().split(" ")[0] + "00";
        currency = data.get(1).toString().split(" ")[1];

        //get skucode from response for the respective voucher
        JsonPath jsonResponse = GlobalVariables.response.body().jsonPath();
        List<Map> products = jsonResponse.getList("data.brands[0].stores[0].products");
        Map product = traverseMultiLevelMap(products, Arrays.asList(new String[]{"price", "chargeable", "sale", "value"}), voucherAmount);
        skucode = (String) product.get("skucode");

        //headers
        headers.put("authorization", GlobalVariables.authorization);

        //get payload
        String payload = Payloads.generateInvoice("");

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.generateInvoice", GlobalVariables.ENDPOINT_PROP_FILE);
        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", skucode);

        // call the post API
        response = GlobalVariables.response = ApiHelper.postDetails(headers, payload, updatedEndpoint);
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

        //headers
        headers.put("authorization", GlobalVariables.authorization);
        headers.put("X-Idempotency-Key", CommonMethods.generateRandomString());
        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.purchaseVoucher", GlobalVariables.ENDPOINT_PROP_FILE);

        // get paylaod
        payload = Payloads.purchaseVoucher(invoiceId);

        // call the post API
        response = GlobalVariables.response = ApiHelper.postDetails(headers, payload, endpoint);
        CommonMethods.printResponse(response);

        //verify response
        assertEquals(201, response.statusCode());
        jsonResponse = response.body().jsonPath();
        assertNotNull(jsonResponse.get("id"));
        assertEquals(invoiceId, jsonResponse.get("invoiceId"));
        assertEquals(voucherAmount, jsonResponse.get("total.amount").toString());
        assertEquals(currency, jsonResponse.get("total.currency"));
        assertEquals(orderId, jsonResponse.get("tags.orderId.value"));
        assertEquals("Steam", jsonResponse.get("tags.voucherBrand.value"));
        voucherCode = jsonResponse.get("tags.voucherCode.value");
        assertNotNull(voucherCode);
    }

    @Then("user verify that the voucher is purchased successfully")
    public void userVerifyThatTheVoucherIsPurchasedSuccessfully() {

        //header
        headers.put("authorization", GlobalVariables.authorization);

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getOrder", GlobalVariables.ENDPOINT_PROP_FILE);
        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", orderId);

        // call the post API
        response = GlobalVariables.response = ApiHelper.getList(headers, updatedEndpoint);
        CommonMethods.printResponse(response);

        //verify response
        assertEquals(200, response.statusCode());
        jsonResponse = response.body().jsonPath();
        String orderStatus = jsonResponse.get("data.orderStatus").toString();

        // wait for order status to change from created to completed
        while (orderStatus.equalsIgnoreCase("Created") || orderStatus.equalsIgnoreCase("Processing")) {
            headers.put("authorization", GlobalVariables.authorization);
            response = GlobalVariables.response = ApiHelper.getList(headers, updatedEndpoint);
            jsonResponse = response.body().jsonPath();
            orderStatus = jsonResponse.get("data.orderStatus").toString();
            System.out.println(orderStatus);
        }



        CommonMethods.verifyResponseWithIgnore(response, "EntertainmentVoucher.getOrder", "data.orderId, data.voucherCode,data.voucherProduct.price.chargeable.list.value, data.voucherProduct.price.chargeable.sale.value,data.voucherProduct.price.receivable.value,data.voucherProduct.price.receivableExcludingTax.value,data.voucherProduct.skucode");
        assertEquals(orderId, jsonResponse.get("data.orderId"));
//        assertEquals(voucherCode,jsonResponse.get("data.voucherCode"));
        assertEquals(skucode, jsonResponse.get("data.voucherProduct.skucode"));
        assertEquals(voucherAmount, jsonResponse.get("data.voucherProduct.price.receivable.value").toString());

    }

    @And("user CPAY account is deducted with voucher amount")
    public void userCPAYAccountIsDeductedWithAmount() {
        //get endpoint
        String endpoint = Prop.readKeyValueFromPropFile("wallet.getCurrentBalance", GlobalVariables.ENDPOINT_PROP_FILE);

        headers.put("authorization", GlobalVariables.authorization);

        // call API
        response = GlobalVariables.response = ApiHelper.getList(headers, endpoint);
        CommonMethods.printResponse(response);

        //verify response
        assertEquals(200, response.statusCode());
        jsonResponse = response.body().jsonPath();
        int balanceAfter = jsonResponse.get("amount");
        System.out.println("balance before transaction" + currentBalance);
        assertEquals(balanceAfter, currentBalance - Integer.parseInt(voucherAmount));
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


    @Given("user get all vouchers with invalid authentication token")
    public void userGetAllVouchersWithInvalidAuthenticationToken() {
        String endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getAllVoucherOptions", GlobalVariables.ENDPOINT_PROP_FILE);
        headers.put("authorization", GlobalVariables.invalidAuthorization);
        response = GlobalVariables.response = ApiHelper.getList(headers, endpoint);
        CommonMethods.printResponse(response);
    }

    @Then("user verify status code {int} in response with message {string}")
    public void userVerifyStatusCodeInResponseWithMessage(int statusCode, String responseMessage) {
        assertEquals(statusCode, response.statusCode());
        assertTrue(((RestAssuredResponseImpl) response).getContent().toString().contains(responseMessage));
    }

    @Given("user get all vouchers with expired authentication token")
    public void userGetAllVouchersWithExpiredAuthenticationToken() {
        //header
        headers.put("authorization", GlobalVariables.expiredAuthorization);
        //endpoint
        String endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.getAllVoucherOptions", GlobalVariables.ENDPOINT_PROP_FILE);

        // call the api
        response = GlobalVariables.response = ApiHelper.getList(headers, endpoint);
        CommonMethods.printResponse(response);
    }

//    @And("user select a <voucher> to generate invoice using the promo <promo code>")
//    public void userSelectAVoucherToGenerateInvoiceUsingThePromoPromoCode(DataTable dataTable) {
//        dataTable.asList();
//        List<String> data = table.asList();
//        voucherAmount = data.get(1).toString().split(" ")[0] + "00";
//        currency = data.get(1).toString().split(" ")[1];
//
//        //get skucode from response for the respective voucher
//        JsonPath jsonResponse = response.body().jsonPath();
//        List<Map> products = jsonResponse.getList("data.brands[0].stores[0].products");
//        Map product = traverseMultiLevelMap(products, Arrays.asList(new String[]{"price", "chargeable", "sale", "value"}), voucherAmount);
//        skucode = (String) product.get("skucode");
//
//        //get payload
//        String payload = Payloads.generateInvoice();
//
//        //get endpoint
//        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.generateInvoice", GlobalVariables.ENDPOINT_PROP_FILE);
//        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", skucode);
//
//        // call the post API
//        GlobalVariables.response = ApiHelper.postDetails(headers, payload, updatedEndpoint);
//        CommonMethods.printResponse(response);
//
//        //verify the response
//        assertEquals(200, response.statusCode());
//        jsonResponse = response.body().jsonPath();
//        invoiceId = jsonResponse.get("data.invoiceId");
//        assertNotNull(invoiceId);
//        orderId = jsonResponse.get("data.orderId");
//        assertEquals(voucherAmount, jsonResponse.get("data.invoiceValue.value").toString());
//        assertEquals(currency, jsonResponse.get("data.invoiceValue.currency"));


//    }

    @When("user select a <voucher> to generate invoice using the promo <PromoCode>")
    public void userSelectAVoucherToGenerateInvoiceUsingThePromoPromoCode(DataTable dataTable) {
        dataTable.asList();

    }

    @When("user select a {string} to generate invoice using the promo {string}")
    public void userSelectAToGenerateInvoiceUsingThePromo(String voucherAmount, String promoCode) {
        //get payload
        String payload = Payloads.generateInvoice(promoCode);

        headers.put("authorization", globalVariables.authorization);
        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.generateInvoice", GlobalVariables.ENDPOINT_PROP_FILE);
        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", "Z9AEAE24215");

        // call the post API
        response = GlobalVariables.response = ApiHelper.postDetails(headers, payload, updatedEndpoint);
        CommonMethods.printResponse(response);
    }

    @When("user hit generate invoice API by passing an additional field in payload")
    public void userHitGenerateInvoiceAPIByPassingAnAdditionalFieldInPayload(DataTable table) {
        headers.put("authorization", globalVariables.authorization);

        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.generateInvoice", GlobalVariables.ENDPOINT_PROP_FILE);
        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", "Z9AEAE24215");

        payload = table.asList().get(1);

        // call the post API
        response = GlobalVariables.response = ApiHelper.postDetails(headers, payload, updatedEndpoint);
        CommonMethods.printResponse(response);
    }

    @Then("user verify status code {int} in response")
    public void userVerifyStatusCodeInResponse(int statusCode) {
        assertEquals(statusCode, GlobalVariables.response.statusCode());
    }

    @When("user generate invoice with invalid skucode {string}")
    public void userGenerateInvoiceWithInvalidSkucode(String skucode) {
        headers.put("authorization", GlobalVariables.authorization);
        //get endpoint
        endpoint = Prop.readKeyValueFromPropFile("entertainmentVouchers.generateInvoice", GlobalVariables.ENDPOINT_PROP_FILE);
        String updatedEndpoint = endpoint.replaceAll("(\\{.*\\})", skucode);

        payload = Payloads.generateInvoice("");

        // call the post API
        response = GlobalVariables.response = ApiHelper.postDetails(headers, payload, updatedEndpoint);
        CommonMethods.printResponse(response);
    }


}