package util

class UrlBuilder {
    private static final io.cucumber.core.logging.Logger LOG = io.cucumber.core.logging.LoggerFactory.getLogger(UrlBuilder.class);
    private static final String ENDPOINT_PROPERTIES = "endpoint.properties";
    private static URL basePath;

    public static URL getApiUrlForEndPoint(String endpoint) {
        Prop.loadPropertiesFile(ENDPOINT_PROPERTIES);
        basePath = new URL(Prop.getProp("baseUrl"));
        return createApiUrl(endpoint);
    }

    public static URL createApiUrl(String endpoint) {
        try {
            return new URL(basePath.getProtocol(), basePath.getHost(), basePath.getPort(), endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


}



