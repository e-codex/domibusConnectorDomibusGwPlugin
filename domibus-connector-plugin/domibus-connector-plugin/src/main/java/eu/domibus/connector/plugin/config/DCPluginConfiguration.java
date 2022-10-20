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


public class DCPluginConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPluginConfiguration.class);

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
        return location;
//        try {
//            KeyStore ks = KeyStore.getInstance(storeType);
//            Resource resource = ctx.getResource(location);
//            URL url = resource.getFile().toURI().toURL();
//
//            ks.load(url.openStream(), password.toCharArray());
//
//            return url.toString();
//
//        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
//            String error = String.format("Property: [%s] is invalid:Failed to load KeyStore from location [%s]", propName, location);
//            throw new RuntimeException(error, e);
//        }
    }






}
