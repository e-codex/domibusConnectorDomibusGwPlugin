package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;


public abstract class DCPluginConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPluginConfiguration.class);

    public static final String PLUGIN_CXF_FEATURES_BEAN_NAME = "cxfFeaturesBean";
    public static final String POLICY_FEATURE_BEAN_NAME = "policyFeatureBean";
    public static final String CXF_LOGGING_FEATURE_BEAN_NAME = "cxfLoggingFeatureBean";

    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN = "dcPushPluginMessageQueueBean";
    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_JNDI = "jms/domibus.dcpushplugin.notifications";

    public static final String DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_BEAN = "dcPullPluginMessageQueueBean";
    public static final String DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_JNDI = "jms/domibus.dcpullplugin.notifications";


    public static void putIfNotNull(AbstractDCPluginPropertyManager wsPluginPropertyManager, Properties props, String s, String cxfKeyStoreType) {
        String knownPropertyValue = wsPluginPropertyManager.getKnownPropertyValue(cxfKeyStoreType);
        if (knownPropertyValue == null) {
            throw new IllegalArgumentException(String.format("The property %s is null - this is not allowed!", s));
        }
        props.put(s, knownPropertyValue);
    }

    public static String checkKeyStore(ApplicationContext ctx, String propName, String storeType, String location, String password) {
        if (storeType == null) {
            throw new IllegalArgumentException(String.format("Property: [%s] is invalid: storeType is not allowed to be empty!", propName));
        }
//        return location;
        try {
            KeyStore ks = KeyStore.getInstance(storeType);
            Resource resource = ctx.getResource(location);
            URL url = resource.getFile().toURI().toURL();

            ks.load(url.openStream(), password.toCharArray());

            return url.toString();

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            String error = String.format("Property: [%s] is invalid:Failed to load KeyStore from location [%s]", propName, location);
            throw new RuntimeException(error, e);
        }
    }

    @Bean
    public AuthenticationService certAuthenticationService(AbstractDCPluginPropertyManager wsPluginPropertyManager,
                                                           ApplicationContext ctx) {
        return new AuthenticationService(wsPluginPropertyManager, ctx);
    }


    public static HashMap<String, Object> getWssProperties(
            ApplicationContext ctx,
            AbstractDCPluginPropertyManager wsPluginPropertyManager
    ) {
        HashMap<String, Object> props = new HashMap<>();

        String encryptionUsername = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_ENCRYPT_ALIAS);

        props.put("mtom-enabled", true);

        props.put("security.encryption.properties", gwWsLinkEncryptProperties(ctx, wsPluginPropertyManager));
        props.put("security.encryption.username",  encryptionUsername);
        props.put("security.signature.properties", gwWsLinkEncryptProperties(ctx, wsPluginPropertyManager));
        props.put("security.callback-handler", new DefaultWsCallbackHandler());

        return props;
    }


    public static Properties gwWsLinkEncryptProperties(
            ApplicationContext ctx,
            AbstractDCPluginPropertyManager wsPluginPropertyManager
    ) {
        Properties props = new Properties();

        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.type", AbstractDCPluginPropertyManager.CXF_KEY_STORE_TYPE);

        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.password", AbstractDCPluginPropertyManager.CXF_KEY_STORE_PASSWORD);
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.alias", AbstractDCPluginPropertyManager.CXF_PRIVATE_KEY_ALIAS);
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.private.password", AbstractDCPluginPropertyManager.CXF_PRIVATE_KEY_PASSWORD);

        String keystoreLocation = checkKeyStore(ctx, AbstractDCPluginPropertyManager.CXF_KEY_STORE,
                wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_KEY_STORE_TYPE),
                wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_KEY_STORE_PATH_PROPERTY_NAME),
                wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_KEY_STORE_PASSWORD));

        props.put("org.apache.wss4j.crypto.merlin.keystore.file", keystoreLocation);


        putIfNotNull(wsPluginPropertyManager,
                props,
                "org.apache.wss4j.crypto.merlin.truststore.type",
                AbstractDCPluginPropertyManager.CXF_TRUST_STORE_TYPE_PROPERTY_NAME);

        String trustStoreLocation = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_TRUST_STORE_PATH_PROPERTY_NAME);

        trustStoreLocation = checkKeyStore(ctx, AbstractDCPluginPropertyManager.CXF_TRUST_STORE,
                wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_TRUST_STORE_TYPE_PROPERTY_NAME),
                trustStoreLocation,
                wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME));

        props.put("org.apache.wss4j.crypto.merlin.truststore.file", trustStoreLocation);
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.truststore.password", AbstractDCPluginPropertyManager.CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME);

        return props;
    }

    @Bean
    @Qualifier(PLUGIN_CXF_FEATURES_BEAN_NAME)
    public List<Feature> featureList(AbstractDCPluginPropertyManager wsPluginPropertyManager,
                                     @Autowired WSPolicyFeature wsPolicyFeature,
                                     @Autowired(required = false) LoggingFeature loggingFeature) {
        List<Feature> featureList = new ArrayList<>();
        featureList.add(wsPolicyFeature);
        if ("true".equalsIgnoreCase(wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_LOGGING_FEATURE_PROPERTY_NAME))) {
            featureList.add(loggingFeature);
        }
        return featureList;
    }

    @Bean
    @Qualifier(POLICY_FEATURE_BEAN_NAME)
    public WSPolicyFeature wsPolicyFeature(AbstractDCPluginPropertyManager wsPluginPropertyManager, ApplicationContext ctx) {
        String policyLocation = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_SECURITY_POLICY);
        Resource resource = ctx.getResource(policyLocation);
        if (resource.exists()) {
            WSPolicyFeature wsPolicyFeature = WsPolicyLoader.loadPolicyFeature(resource);
            return wsPolicyFeature;
        } else {
            String error = String.format("Was not able to load WS-Security Policy from: [%s=%s]", AbstractDCPluginPropertyManager.CXF_SECURITY_POLICY, policyLocation);
            throw new RuntimeException(error);
        }
    }

    @Bean
    @Qualifier(CXF_LOGGING_FEATURE_BEAN_NAME)
    public LoggingFeature loggingFeature() {
        LOGGER.debug("CXFLoggingFeature is activated");
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        return loggingFeature;
    }






}
