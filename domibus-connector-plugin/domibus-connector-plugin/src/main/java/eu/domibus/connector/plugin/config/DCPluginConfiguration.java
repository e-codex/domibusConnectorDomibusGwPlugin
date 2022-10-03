package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.DCPluginPropertyManager;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

@Configuration
public class DCPluginConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPluginConfiguration.class);

//    public static final String MODULE_NAME = "DOMIBUS_CONNECTOR_PLUGIN";

    public static final String WEB_SERVICE_PROPERTIES_BEAN_NAME = "dcWebServiceProperties";
    public static final String POLICY_FEATURE_BEAN_NAME = "dcPolicyFeature";
    public static final String WSS4J_ENC_PROPERTIES = "dcWss4jEncProperties";

    public static final String JAXWS_PROPERTIES_BEAN_NAME = "jaxWsPropertiesBean";
    public static final String CXF_LOGGING_FEATURE_BEAN = "dcCxfLoggingFeature";
    public static final String DC_PLUGIN_CXF_FEATURE = "dcPluginCxfFeature";

    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN = "dcPushPluginMessageQueueBean";
    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_JNDI = "jms/domibus.dcpushplugin.notifications";

    public static final String DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_BEAN = "dcPullPluginMessageQueueBean";
    public static final String DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_JNDI = "jms/domibus.dcpullplugin.notifications";

    @Autowired
    ApplicationContext ctx;

//    @Bean
//    public DCPluginPropertyManager dcPluginPropertyManager() {
//        return new DCPluginPropertyManager();
//    }


    @Bean(JAXWS_PROPERTIES_BEAN_NAME)
    public Map<String, Object> getWssProperties(
            DCPluginPropertyManager wsPluginPropertyManager
    ) {
        HashMap<String, Object> props = new HashMap<>();

        String encryptionUsername = wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_ENCRYPT_ALIAS);

        props.put("mtom-enabled", true);
        props.put("security.encryption.properties", gwWsLinkEncryptProperties(wsPluginPropertyManager));
        props.put("security.encryption.username",  encryptionUsername);
        props.put("security.signature.properties", gwWsLinkEncryptProperties(wsPluginPropertyManager));
        props.put("security.callback-handler", new DefaultWsCallbackHandler());

        LOGGER.debug("{} now are [{}]", WEB_SERVICE_PROPERTIES_BEAN_NAME, props);

        return props;
    }


    @Bean(WSS4J_ENC_PROPERTIES)
    public Properties gwWsLinkEncryptProperties(
            DCPluginPropertyManager wsPluginPropertyManager
    ) {
        Properties props = new Properties();


        props.put("org.apache.wss4j.crypto.provider", "org.apache.wss4j.common.crypto.Merlin");
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.type", DCPluginPropertyManager.CXF_KEY_STORE_TYPE);
        props.put("org.apache.wss4j.crypto.merlin.keystore.file", checkLocation(ctx, wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_KEY_STORE_PATH_PROPERTY_NAME)));
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.password", DCPluginPropertyManager.CXF_KEY_STORE_PASSWORD);
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.alias", DCPluginPropertyManager.CXF_PRIVATE_KEY_ALIAS);
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.private.password", DCPluginPropertyManager.CXF_PRIVATE_KEY_PASSWORD);

        checkKeyStore(DCPluginPropertyManager.CXF_KEY_STORE, wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_KEY_STORE_TYPE), wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_KEY_STORE_PATH_PROPERTY_NAME), wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_KEY_STORE_PASSWORD));


        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.truststore.type", DCPluginPropertyManager.CXF_TRUST_STORE_TYPE_PROPERTY_NAME);

        String trustStoreLocation = wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_TRUST_STORE_PATH_PROPERTY_NAME);

        checkKeyStore(DCPluginPropertyManager.CXF_TRUST_STORE, "JKS", trustStoreLocation, wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME));

        trustStoreLocation = checkLocation(ctx, trustStoreLocation);
        props.put("org.apache.wss4j.crypto.merlin.truststore.file", trustStoreLocation);
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.truststore.password", DCPluginPropertyManager.CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME);

        return props;
    }

    private void putIfNotNull(DCPluginPropertyManager wsPluginPropertyManager, Properties props, String s, String cxfKeyStoreType) {
        String knownPropertyValue = wsPluginPropertyManager.getKnownPropertyValue(cxfKeyStoreType);
        if (knownPropertyValue == null) {
            throw new IllegalArgumentException(String.format("The property %s is null - this is not allowed!", s));
        }
        props.put(s, knownPropertyValue);
    }

    private String checkLocation(ApplicationContext ctx, String storeLocation) {
        Resource resource = ctx.getResource(storeLocation);
        if (resource.exists()) {
            try {
                storeLocation = resource.getURI().toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return storeLocation;
    }


    private void checkKeyStore(String propName, String storeType, String location, String password) {
        if (storeType == null) {
            throw new IllegalArgumentException(String.format("Property: [%s] is invalid: storeType is not allowed to be empty!", propName));
        }
        try {
            KeyStore ks = KeyStore.getInstance(storeType);
            Resource resource = ctx.getResource(location);
            ks.load(resource.getInputStream(), password.toCharArray());


        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            String error = String.format("Property: [%s] is invalid:Failed to load KeyStore from location [%s]", propName, location);
            throw new RuntimeException(error, e);
        }
    }

    @Bean(DC_PLUGIN_CXF_FEATURE)
    public List<Feature> featureList(DCPluginPropertyManager wsPluginPropertyManager,
            @Autowired WSPolicyFeature wsPolicyFeature, @Autowired(required = false) LoggingFeature loggingFeature) {
        List<Feature> featureList = new ArrayList<>();
        featureList.add(wsPolicyFeature);
        if ("true".equalsIgnoreCase(wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_LOGGING_FEATURE_PROPERTY_NAME))) {
            featureList.add(loggingFeature);
        }
        return featureList;
    }

    @Bean(POLICY_FEATURE_BEAN_NAME)
    public WSPolicyFeature wsPolicyFeature(DCPluginPropertyManager wsPluginPropertyManager, ApplicationContext ctx) {
        String policyLocation = wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_SECURITY_POLICY);
        Resource resource = ctx.getResource(policyLocation);
        if (resource.exists()) {
            WSPolicyFeature wsPolicyFeature = WsPolicyLoader.loadPolicyFeature(resource);
            return wsPolicyFeature;
        } else {
            String error = String.format("Was not able to load WS-Security Policy from: [%s=%s]", DCPluginPropertyManager.CXF_SECURITY_POLICY, policyLocation);
            throw new RuntimeException(error);
        }
    }

    @Bean(CXF_LOGGING_FEATURE_BEAN)
    public LoggingFeature loggingFeature() {
        LOGGER.debug("CXFLoggingFeature is activated");
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        return loggingFeature;
    }

    @Bean
    public AuthenticationService certAuthenticationService(DCPluginPropertyManager wsPluginPropertyManager,
                                                           ApplicationContext ctx) {
        return new AuthenticationService(wsPluginPropertyManager, ctx);
    }

}
