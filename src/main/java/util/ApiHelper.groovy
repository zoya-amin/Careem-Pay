package util


import io.restassured.RestAssured
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

import static io.restassured.RestAssured.given

public class ApiHelper {
    static String HEADER_PROPERTY_FILE = "header.properties"

    public static Response getList(Map Headers, String endpoint) {
        return givenConfig(Headers).log().everything().
                when().
                get(UrlBuilder.getApiUrlForEndPoint(endpoint));
    }

    public static Response getListConsumer(Map Headers, String endpoint) {
        return givenConfig(Headers).log().everything().
                when().
                get(UrlBuilder.getApiUrlForConsumerEndPoint(endpoint));
    }

    public static Response postDetails(Map headers, String payload, String endpointName) {
        return givenConfig(headers).log().all().
                body(payload).
                when().
                post(UrlBuilder.getApiUrlForEndPoint(endpointName))
    }


//    public static Response updateDetails(List<ItemModel> itemModels) {
//        return givenConfig().
//                body(gson().toJson(itemModels)).
//                when().
//                put("posts/1");
//    }


    public static Response deleteItem(String id) {
        return givenConfig().
                when().delete("posts/" + id);
    }

//    // header reading from properties file
//    protected static RequestSpecification givenConfig(List<String> keys) {
//        RestAssured.useRelaxedHTTPSValidation();
//        Map<String, String> requestHeaders = new HashMap<>();
//        for (String key : keys) {
//            String value = Prop.readKeyValueFromPropFile(key, HEADER_PROPERTY_FILE)
//            requestHeaders.put(key, value)
//        }
//        return given().headers(requestHeaders)
//    }

    // header reading from Global variable file
//    protected static RequestSpecification givenConfig(List<String> keys) {
//        RestAssured.useRelaxedHTTPSValidation();
//        Map<String, String> requestHeaders = new HashMap<>();
//        for (String key : keys) {
//            switch (key) {
//                case "Authorization":
//                    requestHeaders.put(key, GlobalVariables.authorization);
//                    break;
//                case "X-Idempotency-Key":
//                    requestHeaders.put(key,CommonMethods.generateRandomString());
//                    break;
//            }
//
//        }
//        requestHeaders.put("Content-Type", "application/json;charset=UTF-8")
//        return given().headers(requestHeaders)
//    }


    protected static RequestSpecification givenConfig(Map headers) {
        String authToken = headers.get("authorization")
        headers.remove("authorization");
        RestAssured.useRelaxedHTTPSValidation();
        headers.put("Content-Type", "application/json;charset=UTF-8")
        return given().auth().oauth2(authToken).headers(headers)
    }


}
