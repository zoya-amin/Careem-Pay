package util

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.restassured.response.Response
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult

import java.nio.file.Files
import java.nio.file.Paths

import static org.junit.Assert.assertTrue

class CommonMethods {

    public static final GSON = new Gson()


    static void verifyResponse(Response actualResponse, String expectedResponseNameFromJson) {
        String jsonActual = new String(actualResponse.body().asByteArray())
        String expectedResponseFromJsonFile = new String(Files.readAllBytes(Paths.get("src/main/resources/properties/responses.json")))
        String jsonExpected = GSON.fromJson(expectedResponseFromJsonFile, JsonElement.class).get(expectedResponseNameFromJson).toString()
        JSONCompareResult result =
                JSONCompare.compareJSON(jsonExpected,jsonActual, JSONCompareMode.STRICT)
        if (!result.passed()) {
            System.out.println(result.toString())
        }
        assertTrue(result.passed())
    }

    static void verifyResponseWithIgnore(Response actualResponse, String expectedResponseNameFromJson, List fieldsToIgnore) {
        String jsonActual = new String(actualResponse.body().asByteArray())
        String expectedResponseFromJsonFile = new String(Files.readAllBytes(Paths.get("src/main/resources/properties/responses.json")))
        String jsonExpected = GSON.fromJson(expectedResponseFromJsonFile, JsonElement.class).get(expectedResponseNameFromJson).toString()
        JSONCompareResult result =
                JSONCompare.compareJSON(jsonExpected,jsonActual, JSONCompareMode.STRICT);
        for (int i = 0; i < result.fieldFailures.size(); i++) {
            String field=  result.fieldFailures[i]._field.split("\\.")[1]
            if (fieldsToIgnore.toString().contains(field)) {
                //if this is expected then do nothing
            } else {
                System.out.println(result.toString())
                assertTrue(result.passed())
            }
        }
    }


    static JSONObject generateBody(Map bodyAttributes) {
        return (JSONObject) bodyAttributes
    }

    int extractNumberFromInteger(String data) {
        return Integer.parseInt(data.toString().replaceAll("\\D+", ""));
    }

    static void printResponse(response){
        System.out.println( "\n\033[1m*********  Response  *********\033[0m");
        response.prettyPrint();
    }

    static  String generateRandomString(){
        return  100000000 +new Random().nextInt(900000000)
    }


}
