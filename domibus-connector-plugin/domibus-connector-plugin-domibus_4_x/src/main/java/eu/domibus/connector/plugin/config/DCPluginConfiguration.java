package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.DCPluginPropertyManager;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.connector.plugin.ws.DomibusConnectorPullWebservice;
import eu.domibus.connector.plugin.ws.DomibusConnectorPushWebservice;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWSService;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWSService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.MessageLister;
import eu.domibus.plugin.QueueMessageLister;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.jms.Queue;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

@Configuration
public class DCPluginConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPluginConfiguration.class);

    public static final String MODULE_NAME = "DOMIBUS_CONNECTOR_PLUGIN";

    public static final String CXF_TRUST_STORE = "connector.delivery.trust-store";
    public static final String CXF_TRUST_STORE_PATH_PROPERTY_NAME = "connector.delivery.trust-store.file";
    public static final String CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME = "connector.delivery.trust-store.password";
    public static final String CXF_TRUST_STORE_TYPE_PROPERTY_NAME = "connector.delivery.trust-store.type";
    public static final String CXF_KEY_STORE_PATH_PROPERTY_NAME = "connector.delivery.key-store.file";
    public static final String CXF_KEY_STORE_PASSWORD = "connector.delivery.key-store.password";
    public static final String CXF_KEY_STORE = "connector.delivery.key-store";
    public static final String CXF_KEY_STORE_TYPE_PROPERTY_NAME = "connector.delivery.key-store.type";
    public static final String CXF_PRIVATE_KEY_ALIAS = "connector.delivery.private-key.alias";
    public static final String CXF_PRIVATE_KEY_PASSWORD = "connector.delivery.private-key.password";
    public static final String CXF_ENCRYPT_ALIAS = "connector.delivery.encrypt-alias";
    public static final String CXF_DELIVERY_ENDPOINT_ADDRESS = "connector.delivery.service.address";
    public static final String CXF_SECURITY_POLICY = "connector.delivery.service.service.security-policy";
    public static final String CXF_LOGGING_FEATURE_PROPERTY_NAME = "connector.delivery.service.service.logging-feature.enabled";
    public static final String PLUGIN_DELIVERY_MODE = "connector.delivery.mode";
    public static final String CXF_PUBLISH_URL = "connector.delivery.service.publish";

    public static final String WEB_SERVICE_PROPERTIES_BEAN_NAME = "dcWebServiceProperties";
    public static final String POLICY_FEATURE_BEAN_NAME = "dcPolicyFeature";
    public static final String WSS4J_ENC_PROPERTIES = "dcWss4jEncProperties";
    public static final String CXF_LOGGING_FEATURE_BEAN = "dcCxfLoggingFeature";
    public static final String DC_PLUGIN_CXF_FEATURE = "dcPluginCxfFeature";

    public static final String DC_PLUGIN_NOTIFICATIONS_QUEUE_BEAN = "dcpluginMessageQueueBean";
    public static final String DC_PLUGIN_NOTIFICATIONS_QUEUE_JNDI = "jms/domibus.dcplugin.notifications";
    public static final String DC_PLUGIN_NOTIFICATIONS_QUEUE_NAME = "domibus.dcplugin.notifications";

    public static final String PULL_PLUGIN_QUEUE_MESSAGE_LISTER_BEAN_NAME = "dcPullPluginMessageListerBean";

    public static final String DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME = "dcplugin.auth.username";
    public static final String DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME = "dcplugin.auth.roles";
    public static final String DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME = "dcplugin.auth.use-username-from"; //ALIAS, DN, DEFAULT
//    public static final String DC_PLUGIN_PW_PROPERTY_NAME = "dcplugin.auth.password";


    @Value("file:///${domibus.config.location}/plugins/config/dc-plugin.properties")
    private String dcPluginExternalPropertiesFile;

    public static class IsPullPluginCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "PULL".equalsIgnoreCase(context.getEnvironment().getProperty(PLUGIN_DELIVERY_MODE));
        }
    }

    public static class IsPushPluginCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "PUSH".equalsIgnoreCase(context.getEnvironment().getProperty(PLUGIN_DELIVERY_MODE));
        }
    }

    @Autowired
    ApplicationContext ctx;

    @Bean("backendPullPluginWebservice")
    @Conditional(IsPullPluginCondition.class)
    public AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> createDomibusConnectorPullWebservice(
            DomibusPropertyExtService domibusPropertyExtService,
            @Qualifier(PULL_PLUGIN_QUEUE_MESSAGE_LISTER_BEAN_NAME) MessageLister messageLister
            ) {
        AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> connector;
        connector = new DomibusConnectorPullWebservice();
        connector.setLister(messageLister);
        return connector;
    }

    @Bean("backendPushPluginWebserver")
    @Conditional({IsPushPluginCondition.class})
    public AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> createDomibusConnectorPushWebservice() {
        AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> connector;
        connector = new DomibusConnectorPushWebservice();
        return connector;
    }




    @Conditional(IsPullPluginCondition.class)
    @Bean("pullBackendWebserviceEndpoint")
    public EndpointImpl pullBackendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                                 DomibusConnectorPullWebservice backendWebService,
                                                 AuthenticationService authenticationService,
                                                 DCPluginPropertyManager wsPluginPropertyManager,
                                                     @Qualifier(DC_PLUGIN_CXF_FEATURE) List<Feature> featureList


    ) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService); //NOSONAR

        endpoint.setServiceName(DomibusConnectorGatewayWSService.SERVICE);
        endpoint.setEndpointName(DomibusConnectorGatewayWSService.DomibusConnectorGatewayWebService);
        endpoint.setWsdlLocation(DomibusConnectorGatewayWSService.WSDL_LOCATION.toString());

        LOGGER.debug("Activating the following features for DC-Plugin PullPlugin: [{}]", featureList);
        endpoint.setFeatures(featureList);
        Map<String, Object> properties = getWssProperties(wsPluginPropertyManager);
        LOGGER.debug("Setting properties for DC-Plugin DC-Plugin PullPlugin: [{}]", properties);
        endpoint.setProperties(properties);
        if (authenticationService != null) {
            endpoint.getInInterceptors().add(authenticationService);
        }
        endpoint.publish(wsPluginPropertyManager.getKnownPropertyValue(CXF_PUBLISH_URL));
        return endpoint;
    }


    /**
     * Create endpoint for Push
     *  SubmissionWebservice
     */
    @Conditional(IsPushPluginCondition.class)
    @Bean("pushBackendWebserviceEndpoint")
    public EndpointImpl pushBackendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                                     DomibusConnectorGatewaySubmissionWebService backendWebService,
                                                     AuthenticationService authenticationService,
                                                     DCPluginPropertyManager wsPluginPropertyManager,
                                                     @Qualifier(DC_PLUGIN_CXF_FEATURE) List<Feature> featureList

    ) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService); //NOSONAR

        endpoint.setServiceName(DomibusConnectorGatewaySubmissionWSService.SERVICE);
        endpoint.setEndpointName(DomibusConnectorGatewaySubmissionWSService.DomibusConnectorGatewaySubmissionWebService);
        endpoint.setWsdlLocation(DomibusConnectorGatewaySubmissionWSService.WSDL_LOCATION.toString());

        LOGGER.debug("Activating the following features for DC-Plugin PushPlugin: [{}]", featureList);
        endpoint.setFeatures(featureList);
        Map<String, Object> properties = getWssProperties(wsPluginPropertyManager);
        LOGGER.debug("Setting properties for DC-Plugin DC-Plugin PushPlugin: [{}]", properties);
        endpoint.setProperties(properties);
        if (authenticationService != null) {
            endpoint.getInInterceptors().add(authenticationService);
        }
        endpoint.publish(wsPluginPropertyManager.getKnownPropertyValue(CXF_PUBLISH_URL));
        return endpoint;
    }


    //Creating Client Proxy for Push Plugin
    @Conditional(IsPushPluginCondition.class)
    @Bean
    public DomibusConnectorGatewayDeliveryWebService domibusConnectorGatewayDeliveryWebService(
            @Qualifier(DC_PLUGIN_CXF_FEATURE) List<Feature> featureList,
            DCPluginPropertyManager wsPluginPropertyManager
    ) {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(DomibusConnectorGatewayDeliveryWebService.class);

        LOGGER.debug("Activating the following features for DC-Plugin ClientProxy: [{}]", featureList);
        jaxWsProxyFactoryBean.setFeatures(featureList);
        jaxWsProxyFactoryBean.setServiceName(DomibusConnectorGatewayDeliveryWSService.SERVICE);
        jaxWsProxyFactoryBean.setEndpointName(DomibusConnectorGatewayDeliveryWSService.DomibusConnectorGatewayDeliveryWebService);
        jaxWsProxyFactoryBean.setWsdlLocation(DomibusConnectorGatewayDeliveryWSService.WSDL_LOCATION.toString());

        String cxfDeliveryAddr = wsPluginPropertyManager.getKnownPropertyValue(CXF_DELIVERY_ENDPOINT_ADDRESS);
        Map<String, Object> properties = getWssProperties(wsPluginPropertyManager);
        LOGGER.debug("Setting properties [{}] for DC-Plugin ClientProxy\nDeliveryAddress would be [{}]", properties, cxfDeliveryAddr);
        jaxWsProxyFactoryBean.setProperties(properties);
        jaxWsProxyFactoryBean.setAddress(cxfDeliveryAddr);

        return (DomibusConnectorGatewayDeliveryWebService) jaxWsProxyFactoryBean.create();
    }


    public Map<String, Object> getWssProperties(
            DCPluginPropertyManager wsPluginPropertyManager
    ) {
        HashMap<String, Object> props = new HashMap<>();

        String encryptionUsername = wsPluginPropertyManager.getKnownPropertyValue(CXF_ENCRYPT_ALIAS);

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

        StoreConfig keyStore = checkKeyStore(wsPluginPropertyManager, CXF_KEY_STORE);
        props.put("org.apache.wss4j.crypto.merlin.keystore.type", keyStore.storeType);
        props.put("org.apache.wss4j.crypto.merlin.keystore.file", keyStore.location.toString());
        props.put("org.apache.wss4j.crypto.merlin.keystore.password", keyStore.password);

        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.alias", CXF_PRIVATE_KEY_ALIAS);
        putIfNotNull(wsPluginPropertyManager, props, "org.apache.wss4j.crypto.merlin.keystore.private.password", CXF_PRIVATE_KEY_PASSWORD);

        StoreConfig trustStore = checkKeyStore(wsPluginPropertyManager, CXF_TRUST_STORE);
        props.put("org.apache.wss4j.crypto.merlin.truststore.type", trustStore.storeType);
        props.put("org.apache.wss4j.crypto.merlin.truststore.file", trustStore.location.toString());
        props.put("org.apache.wss4j.crypto.merlin.truststore.password", trustStore.password);

        return props;
    }

    private void putIfNotNull(DCPluginPropertyManager wsPluginPropertyManager, Properties props, String s, String propertyName) {
        String knownPropertyValue = wsPluginPropertyManager.getKnownPropertyValue(propertyName);
        if (knownPropertyValue == null) {
            throw new IllegalArgumentException(String.format("The property %s is null - this is not allowed!", s));
        }
        props.put(s, knownPropertyValue);
    }

    private StoreConfig checkKeyStore(DCPluginPropertyManager wsPluginPropertyManager, String propName) {
        StoreConfig storeConfig = new StoreConfig();

        storeConfig.locationString = wsPluginPropertyManager.getKnownPropertyValue(propName + ".file");
        storeConfig.storeType = wsPluginPropertyManager.getKnownPropertyValue(propName + ".type");
        storeConfig.password = wsPluginPropertyManager.getKnownPropertyValue(propName + ".password");

        if (storeConfig.storeType == null) {
            throw new IllegalArgumentException(String.format("Property: [%s.type] is invalid: storeType is not allowed to be empty!", propName));
        }
        if (storeConfig.password == null) {
            throw new IllegalArgumentException(String.format("Property: [%s.password] is invalid: storeType is not allowed to be empty!", propName));
        }
        if (storeConfig.locationString == null) {
            throw new IllegalArgumentException(String.format("Property: [%s.file] is invalid: storeType is not allowed to be empty!", propName));
        }

        try {
            KeyStore ks = KeyStore.getInstance(storeConfig.storeType);
            storeConfig.location = checkLocation(ctx, storeConfig.locationString);
            ks.load(storeConfig.location.openStream(), storeConfig.password.toCharArray());
            return storeConfig;
        } catch (IllegalArgumentException | KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            String error = String.format("Property: [%s.*] is invalid: Failed to load KeyStore from location [%s] (URL [%s]) due ", propName, storeConfig.locationString, storeConfig.location, e.getMessage());
            throw new RuntimeException(error, e);
        }
    }

    private URL checkLocation(ApplicationContext ctx, String storeLocation) {
        Resource resource = ctx.getResource(storeLocation);
        if (resource.exists()) {
            try {
                return resource.getURL();
            } catch (IOException e) {
                String error = String.format("IOException occurred while getting URL from [%s]", storeLocation);
                throw new IllegalArgumentException(error, e);
            }
        }
        throw new IllegalArgumentException(String.format("A resource with name [%s] does not exist!", storeLocation));
    }

    private static class StoreConfig {
        String locationString;
        URL location;
        String storeType;
        String password;
    }

    @Bean(DC_PLUGIN_CXF_FEATURE)
    public List<Feature> featureList(DCPluginPropertyManager wsPluginPropertyManager,
            @Autowired WSPolicyFeature wsPolicyFeature, @Autowired(required = false) LoggingFeature loggingFeature) {
        List<Feature> featureList = new ArrayList<>();
        featureList.add(wsPolicyFeature);
        if ("true".equalsIgnoreCase(wsPluginPropertyManager.getKnownPropertyValue(CXF_LOGGING_FEATURE_PROPERTY_NAME))) {
            featureList.add(loggingFeature);
        }
        return featureList;
    }

    @Bean(POLICY_FEATURE_BEAN_NAME)
    public WSPolicyFeature wsPolicyFeature(DCPluginPropertyManager wsPluginPropertyManager, ApplicationContext ctx) {
        String policyLocation = wsPluginPropertyManager.getKnownPropertyValue(CXF_SECURITY_POLICY);
        Resource resource = ctx.getResource(policyLocation);
        if (resource.exists()) {
            WSPolicyFeature wsPolicyFeature = WsPolicyLoader.loadPolicyFeature(resource);
            return wsPolicyFeature;
        } else {
            String error = String.format("Was not able to load WS-Security Policy from: [%s=%s]", CXF_SECURITY_POLICY, policyLocation);
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



    @Conditional(IsPullPluginCondition.class)
    @Bean(PULL_PLUGIN_QUEUE_MESSAGE_LISTER_BEAN_NAME)
    public QueueMessageLister QueueMessageLister(
            JMSExtService jmsExtService,
            @Qualifier(DC_PLUGIN_NOTIFICATIONS_QUEUE_BEAN) Queue pullMessageQueue
    ) {
        QueueMessageLister q = new QueueMessageLister(jmsExtService, pullMessageQueue, DomibusConnectorPullWebservice.PLUGIN_NAME);
        return q;
    }

    @Conditional(IsPullPluginCondition.class)
    @Bean("asyncPullWebserviceNotification")
    public PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration( @Qualifier(DC_PLUGIN_NOTIFICATIONS_QUEUE_BEAN) Queue notifyBackendWebServiceQueue,
                                                                                     DomibusConnectorPullWebservice backendWebService,
                                                                                     Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration
                = new PluginAsyncNotificationConfiguration(backendWebService, notifyBackendWebServiceQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = DC_PLUGIN_NOTIFICATIONS_QUEUE_JNDI;
            LOGGER.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        LOGGER.info("Initializing asyncPullWebserviceNotification service");
        return pluginAsyncNotificationConfiguration;
    }


    @Conditional(IsPushPluginCondition.class)
    @Bean("asyncPushWebserviceNotification")
    public PluginAsyncNotificationConfiguration pushPluginAsyncNotificationConfiguration( @Qualifier(DC_PLUGIN_NOTIFICATIONS_QUEUE_BEAN) Queue notifyBackendWebServiceQueue,
                                                                                      DomibusConnectorPushWebservice backendWebService,
                                                                                      Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration
                = new PluginAsyncNotificationConfiguration(backendWebService, notifyBackendWebServiceQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = DC_PLUGIN_NOTIFICATIONS_QUEUE_JNDI;
            LOGGER.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean
    public AuthenticationService certAuthenticationService(DCPluginPropertyManager wsPluginPropertyManager,
                                                           ApplicationContext ctx) {
        return new AuthenticationService(wsPluginPropertyManager, ctx);
    }

}
