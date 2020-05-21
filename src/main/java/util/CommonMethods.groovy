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
import com.google.common.collect.Sets;

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

    static void verifyResponseWithIgnore(Response actualResponse, String expectedResponseNameFromJson, String fieldsToIgnore) {
        String jsonActual = new String(actualResponse.body().asByteArray())
        String expectedResponseFromJsonFile = new String(Files.readAllBytes(Paths.get("src/main/resources/properties/responses.json")))
        String jsonExpected = GSON.fromJson(expectedResponseFromJsonFile, JsonElement.class).get(expectedResponseNameFromJson).toString()
        JSONCompareResult result =
                JSONCompare.compareJSON(jsonExpected,jsonActual, JSONCompareMode.STRICT);

        Set<Integer> fieldsToIgnoreSet = new HashSet<>(Arrays.asList(fieldsToIgnore.replaceAll("\\s+","").split(",")));
        Set<Integer> failureSet = result.fieldFailures.field;
        try
        {
            assertTrue(fieldsToIgnoreSet.containsAll(failureSet))
        }

        catch(java.lang.AssertionError e){
            Set<String> diff = Sets.difference(fieldsToIgnoreSet, failureSet);
            System.println("/n/n diff" + diff)
            assertTrue(false)
        }
    }


    static JSONObject generateBody(Map bodyAttributes) {
        return (JSONObject) bodyAttributes
    }

   static int extractNumberFromInteger(String data) {
        return Integer.parseInt(data.toString().replaceAll("\\D+", ""));
    }

    static void printResponse(response){
        System.out.println( "\n\033[1m*********  Response  *********\033[0m");
        response.prettyPrint();
    }

    static String generateRandomString(){
        return  100000000 +new Random().nextInt(900000000)
    }


    // usage e.g  traverseMultiLevelMap(products, Arrays.asList(new String[]{"price", "chargeable", "sale", "value"}), voucherAmount)
    static Map traverseMultiLevelMap(List<Map> mapToTraverse, List keys, String valueToFind) {
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


    static String replaceEndPointBracesWith(String endpoint, value){
        return endpoint.replaceAll("(\\{.*\\})", value);
    }
}
