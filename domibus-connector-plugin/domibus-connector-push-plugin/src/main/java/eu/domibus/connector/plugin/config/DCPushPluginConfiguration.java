package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.connector.plugin.config.property.DCPushPluginPropertyManager;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.connector.plugin.ws.DomibusConnectorPushWebservice;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWSService;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.*;

import static eu.domibus.connector.plugin.config.DCPluginConfiguration.*;

@Configuration
@Conditional(PushPluginEnabledCondition.class)
public class DCPushPluginConfiguration {

    public static final String MODULE_NAME = "DC_PUSH_PLUGIN";
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPushPluginConfiguration.class);


    public static final String DC_CXF_BUS_BEAN_NAME = "dcCxfBus";
    public static final String PUSH_CXF_LOGGING_FEATURE_BEAN_NAME = "pushCxfLogginFeatureBean";
    public static final String PUSH_POLICY_FEATURE_BEAN_NAME = "pushPolicyFeatureBean";
    public static final String DC_PUSH_PLUGIN_CXF_FEATURES = "pushPluginCxfFeaturesBean";
    public static final String PUSH_PLUGIN_WSS4J_ENC_PROPERTIES_BEAN_NAME = "pushPluginWss4JEncProperties";
    public static final String PUSH_PLUGIN_JAXWS_PROPERTIES_BEAN_NAME = "pushPluginJaxwsProperties";
;
    @Autowired
    ApplicationContext ctx;

    @PostConstruct
    public static void postConstruct() {
        LOGGER.info("Push Plugin is enabled");
    }


    @Bean
    public DomibusConnectorPushWebservice domibusConnectorPushWebservice() {
        return new DomibusConnectorPushWebservice();
    }

    @Bean
    public AbstractDCPluginPropertyManager dcPluginPropertyManager() {
        return new DCPushPluginPropertyManager();
    }


    /**
     * Create endpoint for Push
     *  SubmissionWebservice
     */
    @Bean("pushBackendWebserviceEndpoint")
    public EndpointImpl pushBackendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                                     DomibusConnectorGatewaySubmissionWebService backendWebService,
                                                     AuthenticationService authenticationService,
                                                     AbstractDCPluginPropertyManager wsPluginPropertyManager,
                                                     @Qualifier(DC_PUSH_PLUGIN_CXF_FEATURES) List<Feature> featureList,
                                                     @Qualifier(PUSH_PLUGIN_JAXWS_PROPERTIES_BEAN_NAME) Map<String, Object> jaxWsProperties

    ) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService);

        endpoint.setServiceName(DomibusConnectorGatewaySubmissionWSService.SERVICE);
        endpoint.setEndpointName(DomibusConnectorGatewaySubmissionWSService.DomibusConnectorGatewaySubmissionWebService);
        endpoint.setWsdlLocation(DomibusConnectorGatewaySubmissionWSService.WSDL_LOCATION.toString());

        LOGGER.debug("Activating the following features for DC-Plugin PushPlugin Endpoint: [{}]", featureList);
        endpoint.setFeatures(featureList);
        LOGGER.debug("Setting properties for DC PushPlugin: [{}]", jaxWsProperties);
        endpoint.setProperties(jaxWsProperties);
        if (authenticationService != null) {
            endpoint.getInInterceptors().add(authenticationService);
        }
        String publishUrl = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.DC_PUSH_PLUGIN_CXF_PUBLISH_URL);
        LOGGER.info("Publish URL for DC PushPlugin is: [{}]", publishUrl);
        endpoint.publish(publishUrl);
        return endpoint;
    }


    //Creating Client Proxy for Push Plugin
    @Bean
    public DomibusConnectorGatewayDeliveryWebService domibusConnectorGatewayDeliveryWebService(
            @Qualifier(DC_PUSH_PLUGIN_CXF_FEATURES) List<Feature> featureList,
            AbstractDCPluginPropertyManager wsPluginPropertyManager
//            @Qualifier(PUSH_PLUGIN_JAXWS_PROPERTIES_BEAN_NAME) Map<String, Object> jaxWsProperties
    ) {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(DomibusConnectorGatewayDeliveryWebService.class);

        LOGGER.debug("Activating the following features for DC-Plugin ClientProxy: [{}]", featureList);
        jaxWsProxyFactoryBean.getFeatures().addAll(featureList);
        jaxWsProxyFactoryBean.setServiceName(DomibusConnectorGatewayDeliveryWSService.SERVICE);
        jaxWsProxyFactoryBean.setEndpointName(DomibusConnectorGatewayDeliveryWSService.DomibusConnectorGatewayDeliveryWebService);
        jaxWsProxyFactoryBean.setWsdlLocation(DomibusConnectorGatewayDeliveryWSService.WSDL_LOCATION.toString());

        String cxfDeliveryAddr = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_DELIVERY_ENDPOINT_ADDRESS);
        LOGGER.info("Sending push messages to [{}]", cxfDeliveryAddr);
        Map<String, Object> wssProperties = getWssProperties(wsPluginPropertyManager);
        LOGGER.debug("Setting properties [{}] for DC-Plugin ClientProxy", wssProperties);
        jaxWsProxyFactoryBean.setProperties(wssProperties);
        jaxWsProxyFactoryBean.setAddress(cxfDeliveryAddr);


        return (DomibusConnectorGatewayDeliveryWebService) jaxWsProxyFactoryBean.create();
    }

    @Bean("asyncPushWebserviceNotification")
    public PluginAsyncNotificationConfiguration pushPluginAsyncNotificationConfiguration( @Qualifier(DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN) javax.jms.Queue notifyBackendWebServiceQueue,
                                                                                      DomibusConnectorPushWebservice backendConnector,
                                                                                      Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration
                = new PluginAsyncNotificationConfiguration(backendConnector, notifyBackendWebServiceQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_JNDI;
            LOGGER.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean
    public AuthenticationService certAuthenticationService(AbstractDCPluginPropertyManager wsPluginPropertyManager,
                                                           ApplicationContext ctx) {
        return new AuthenticationService(wsPluginPropertyManager, ctx);
    }

    @Bean
    @Qualifier(PUSH_PLUGIN_JAXWS_PROPERTIES_BEAN_NAME)
    public Map<String, Object> getWssProperties(
            AbstractDCPluginPropertyManager wsPluginPropertyManager
            @Qualifier(PUSH_PLUGIN_WSS4J_ENC_PROPERTIES_BEAN_NAME) Properties wss4jEncProperties
    ) {
        HashMap<String, Object> props = new HashMap<>();

        String encryptionUsername = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_ENCRYPT_ALIAS);

        props.put("mtom-enabled", true);
        props.put("security.encryption.properties", gwWsLinkEncryptProperties(ctx, wsPluginPropertyManager));
        props.put("security.encryption.username",  encryptionUsername);
        props.put("security.signature.properties", gwWsLinkEncryptProperties(ctx, wsPluginPropertyManager));
        props.put("security.callback-handler", new DefaultWsCallbackHandler());

        LOGGER.debug("{} now are [{}]", PUSH_PLUGIN_JAXWS_PROPERTIES_BEAN_NAME, props);

        return props;
    }


    @Bean
    @Qualifier(PUSH_PLUGIN_WSS4J_ENC_PROPERTIES_BEAN_NAME)
    public Properties gwWsLinkEncryptProperties(
            ApplicationContext ctx,
            AbstractDCPluginPropertyManager wsPluginPropertyManager
    ) {
        Properties props = new Properties();


        props.put("org.apache.wss4j.crypto.provider", "org.apache.wss4j.common.crypto.Merlin");
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
    @Qualifier(DC_PUSH_PLUGIN_CXF_FEATURES)
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
    @Qualifier(PUSH_POLICY_FEATURE_BEAN_NAME)
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
    @Qualifier(PUSH_CXF_LOGGING_FEATURE_BEAN_NAME)
    public LoggingFeature loggingFeature() {
        LOGGER.debug("CXFLoggingFeature is activated");
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        return loggingFeature;
    }


//    @Bean(DC_CXF_BUS_BEAN_NAME)
//    @Qualifier(DC_CXF_BUS_BEAN_NAME)
//    public Bus dcCxfBus() {
//        CXFBusFactory cxfBusFactory = new CXFBusFactory();
//        Bus b = cxfBusFactory.createBus();
//        return b;
//    }
//
//    public static final String DC_CXF_SERVLET_BEAN_NAME = "dcCxfServlet";
//    @Bean(DC_CXF_SERVLET_BEAN_NAME)
//    @Qualifier(DC_CXF_SERVLET_BEAN_NAME)
//    public CXFNonSpringServlet dcCxfServlet(@Qualifier(DC_CXF_BUS_BEAN_NAME) Bus bus) {
//        CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();
//        cxfServlet.setBus(bus);
//        return cxfServlet;
//    }
//
//    @Bean
//    public ServletRegistrationBean dcCxfServletRegistration(@Qualifier(DC_CXF_SERVLET_BEAN_NAME) CXFNonSpringServlet cxfServlet) {
//        ServletRegistrationBean bean = new ServletRegistrationBean(cxfServlet, "/dcservices");
//        return bean;
//    }

}
