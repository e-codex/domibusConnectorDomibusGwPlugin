package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.DCPushPluginPropertyManager;
import eu.domibus.connector.plugin.initialize.DCPluginInitializer;
import eu.domibus.connector.plugin.transformer.DCMessageTransformer;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.connector.plugin.ws.DomibusConnectorPushWebservice;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWSService;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.initialize.PluginInitializer;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.List;

@Configuration
public class DCPushPluginConfiguration extends DCPluginConfiguration {

    public static final String MODULE_NAME = "DC_PUSH_PLUGIN";
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPushPluginConfiguration.class);

    public static final String DC_PUSH_PLUGIN_CXF_FEATURES = "pushPluginCxfFeaturesBean";

    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN = "dcPushPluginMessageQueueBean";
    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_JNDI = "jms/domibus.dcpushplugin.notifications";
    public static final String PUSH_BACKEND_WEBSERVICE_ENDPOINT_BEAN_NAME = "pushBackendWebserviceEndpoint";
    public static final String PUSH_PLUGIN_INITIALIZER_BEAN_NAME = "pushPluginInitializer";
    public static final String PUSH_PLUGIN_AUTHENTICATION_SERVICE_BEAN_NAME = "pushPluginAuthenticationService";


    @Bean
    public DomibusConnectorPushWebservice domibusConnectorPushWebservice(DCMessageTransformer messageTransformer,
                                                                         DCPushPluginPropertyManager wsPluginPropertyManager,
                                                                         ObjectProvider<DomibusConnectorGatewayDeliveryWebService> deliveryClientObjectFactory,
                                                                         @Lazy  @Qualifier(PUSH_PLUGIN_INITIALIZER_BEAN_NAME) PluginInitializer pluginInitializer) {
        return new DomibusConnectorPushWebservice(messageTransformer, wsPluginPropertyManager, deliveryClientObjectFactory, pluginInitializer);
    }

    @Bean
    public DCPushPluginPropertyManager dcPushPluginPropertyManager() {
        return new DCPushPluginPropertyManager();
    }


    /**
     * Create endpoint for Push
     *  SubmissionWebservice
     */
    @Bean(PUSH_BACKEND_WEBSERVICE_ENDPOINT_BEAN_NAME)
    public EndpointImpl pushBackendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                                     ApplicationContext ctx,
                                                     DomibusConnectorGatewaySubmissionWebService backendWebService,
                                                     @Qualifier(PUSH_PLUGIN_AUTHENTICATION_SERVICE_BEAN_NAME) AuthenticationService authenticationService,
                                                     DCPushPluginPropertyManager wsPluginPropertyManager

    ) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService);

        endpoint.setServiceName(DomibusConnectorGatewaySubmissionWSService.SERVICE);
        endpoint.setEndpointName(DomibusConnectorGatewaySubmissionWSService.DomibusConnectorGatewaySubmissionWebService);
        endpoint.setWsdlLocation(DomibusConnectorGatewaySubmissionWSService.WSDL_LOCATION.toString());

        List<Feature> features = getFeatureList(ctx, wsPluginPropertyManager);
        LOGGER.debug("Activating the following features for DC-Plugin PushPlugin Endpoint: [{}]", features);
        endpoint.setFeatures(features);

        HashMap<String, Object> wssProperties = getWssProperties(ctx, wsPluginPropertyManager, "useReqSigCert");
        LOGGER.debug("Setting properties for DC PushPlugin: [{}]", wssProperties);
        endpoint.setProperties(wssProperties);

        if (authenticationService != null) {
            endpoint.getInInterceptors().add(authenticationService);
        }
//        String publishUrl = wsPluginPropertyManager.getKnownPropertyValue(DCPushPluginPropertyManager.DC_PUSH_PLUGIN_CXF_PUBLISH_URL);
//        LOGGER.info("Publish URL for DC PushPlugin is: [{}]", publishUrl);
//        endpoint.publish(publishUrl);
        return endpoint;
    }


    //Creating Client Proxy for Push Plugin
    @Bean
    public ObjectFactory<DomibusConnectorGatewayDeliveryWebService> domibusConnectorGatewayDeliveryWebService(
            DCPushPluginPropertyManager wsPluginPropertyManager,
            ApplicationContext ctx,
            DomibusConfigurationExtService domibusConfigurationExtService,
            DomainContextExtService domainContextExtService
    ) {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(DomibusConnectorGatewayDeliveryWebService.class);

        List<Feature> featureList = getFeatureList(ctx, wsPluginPropertyManager);
        LOGGER.debug("Activating the following features for DC-Plugin ClientProxy: [{}]", featureList);
        jaxWsProxyFactoryBean.getFeatures().addAll(featureList);
        jaxWsProxyFactoryBean.setServiceName(DomibusConnectorGatewayDeliveryWSService.SERVICE);
        jaxWsProxyFactoryBean.setEndpointName(DomibusConnectorGatewayDeliveryWSService.DomibusConnectorGatewayDeliveryWebService);
        jaxWsProxyFactoryBean.setWsdlLocation(DomibusConnectorGatewayDeliveryWSService.WSDL_LOCATION.toString());


        return () -> {
            String cxfDeliveryAddr = "";
            String alias = "";
            if (domibusConfigurationExtService.isMultiTenantAware()) {
                String domainCode = domainContextExtService.getCurrentDomainSafely().getCode();
                cxfDeliveryAddr = wsPluginPropertyManager.getKnownPropertyValue(domainCode, DCPushPluginPropertyManager.CXF_DELIVERY_ENDPOINT_ADDRESS);
                alias = wsPluginPropertyManager.getKnownPropertyValue(domainCode, DCPushPluginPropertyManager.CXF_ENCRYPT_ALIAS);
            } else {
                cxfDeliveryAddr = wsPluginPropertyManager.getKnownPropertyValue(DCPushPluginPropertyManager.CXF_DELIVERY_ENDPOINT_ADDRESS);
                alias = wsPluginPropertyManager.getKnownPropertyValue(DCPushPluginPropertyManager.CXF_ENCRYPT_ALIAS);
            }

            LOGGER.info("Sending push messages to [{}]", cxfDeliveryAddr);
            LOGGER.info("Using encryption alias [{}]", alias);


            HashMap<String, Object> wssProperties = getWssProperties(ctx, wsPluginPropertyManager, alias);
            LOGGER.debug("Setting properties [{}] for DC-Plugin ClientProxy", wssProperties);
            jaxWsProxyFactoryBean.setProperties(wssProperties);
            jaxWsProxyFactoryBean.setAddress(cxfDeliveryAddr);
            return (DomibusConnectorGatewayDeliveryWebService) jaxWsProxyFactoryBean.create();
        };
    }

    @Bean("asyncPushWebserviceNotification")
    public PluginAsyncNotificationConfiguration pushPluginAsyncNotificationConfiguration( @Qualifier(DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN) javax.jms.Queue notifyBackendWebServiceQueue,
                                                                                      DomibusConnectorPushWebservice backendConnector,
                                                                                      DCPushPluginPropertyManager dcPushPluginPropertyManager,
                                                                                      Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration
                = new PluginAsyncNotificationConfiguration(backendConnector, notifyBackendWebServiceQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = dcPushPluginPropertyManager.getKnownPropertyValue(DCPushPluginPropertyManager.DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME_PROPERTY_NAME);
            LOGGER.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean(PUSH_PLUGIN_INITIALIZER_BEAN_NAME)
    public PluginInitializer pluginInitializer(DCPushPluginPropertyManager wsPluginPropertyManager,
                                               @Qualifier(PUSH_BACKEND_WEBSERVICE_ENDPOINT_BEAN_NAME) EndpointImpl endpoint
                                               ) {
        return new DCPluginInitializer(DomibusConnectorPushWebservice.PLUGIN_NAME, wsPluginPropertyManager, endpoint);
    }

    @Bean(PUSH_PLUGIN_AUTHENTICATION_SERVICE_BEAN_NAME)
    public AuthenticationService certAuthenticationService(DCPushPluginPropertyManager wsPluginPropertyManager,
                                                           ApplicationContext ctx) {
        return new AuthenticationService(wsPluginPropertyManager, ctx);
    }



}
