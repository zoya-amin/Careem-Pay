package util

import io.cucumber.core.logging.Logger
import io.cucumber.core.logging.LoggerFactory

import static java.lang.System.out

class Prop {
    private static final Logger LOG = LoggerFactory.getLogger(Prop.class);
    private static Properties prop;
    static String propertiesFileDir = GlobalVariables.PROPERTIES_FILE_DIR

    public static String getProp(String key) {
        if ((key == null) || key.isEmpty()) {
            return "";
        } else {
            return prop.getProperty(key);

        }
    }

    public static void loadPropertiesFile(String propertyFileLocation) {
        prop = new Properties();
        try (InputStream inputStream = Prop.class.getClassLoader().getResourceAsStream("properties/" + propertyFileLocation)) {
            prop.load(inputStream);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    public static String readKeyValueFromPropFile(String key, String fileLocation) {
        prop = new Properties();
        try (InputStream inputStream = Prop.class.getClassLoader().getResourceAsStream("properties/" + fileLocation)) {
            prop.load(inputStream);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return prop.getProperty(key)
    }

//    public static updateProperty(String propertyName, String updatedValue) {
//        ResourceBundle rb = ResourceBundle.getBundle("properties/endpoint");
//        String propertyValue = rb.getObject("entertainmentVouchers.generateInvoice");
//        String updatedEndpoint = propertyValue.replaceAll("(?<=\\{).*?(?=\\})", updatedValue)
//        FileOutputStream out =  new FileOutputStream(Prop.class.getClassLoader().getResource("properties/" + UrlBuilder.ENDPOINT_PROPERTIES));
//        prop.setProperty(propertyValue, updatedEndpoint);
//        prop.store(out, null);
//        out.close();
//    }

}
