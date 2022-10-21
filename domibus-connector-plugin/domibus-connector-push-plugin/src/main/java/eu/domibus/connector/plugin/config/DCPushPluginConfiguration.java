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
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

@Configuration
@Conditional(PushPluginEnabledCondition.class)
public class DCPushPluginConfiguration extends DCPluginConfiguration {

    public static final String MODULE_NAME = "DC_PUSH_PLUGIN";
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPushPluginConfiguration.class);

    public static final String DC_PUSH_PLUGIN_CXF_FEATURES = "pushPluginCxfFeaturesBean";

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
                                                     AbstractDCPluginPropertyManager wsPluginPropertyManager

    ) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService);

        endpoint.setServiceName(DomibusConnectorGatewaySubmissionWSService.SERVICE);
        endpoint.setEndpointName(DomibusConnectorGatewaySubmissionWSService.DomibusConnectorGatewaySubmissionWebService);
        endpoint.setWsdlLocation(DomibusConnectorGatewaySubmissionWSService.WSDL_LOCATION.toString());

        List<Feature> features = getFeatureList(ctx, wsPluginPropertyManager);
        LOGGER.debug("Activating the following features for DC-Plugin PushPlugin Endpoint: [{}]", features);
        endpoint.setFeatures(features);

        HashMap<String, Object> wssProperties = getWssProperties(ctx, wsPluginPropertyManager);
        LOGGER.debug("Setting properties for DC PushPlugin: [{}]", wssProperties);
        endpoint.setProperties(wssProperties);

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
            AbstractDCPluginPropertyManager wsPluginPropertyManager
    ) {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(DomibusConnectorGatewayDeliveryWebService.class);

        List<Feature> featureList = getFeatureList(ctx, wsPluginPropertyManager);
        LOGGER.debug("Activating the following features for DC-Plugin ClientProxy: [{}]", featureList);
        jaxWsProxyFactoryBean.getFeatures().addAll(featureList);
        jaxWsProxyFactoryBean.setServiceName(DomibusConnectorGatewayDeliveryWSService.SERVICE);
        jaxWsProxyFactoryBean.setEndpointName(DomibusConnectorGatewayDeliveryWSService.DomibusConnectorGatewayDeliveryWebService);
        jaxWsProxyFactoryBean.setWsdlLocation(DomibusConnectorGatewayDeliveryWSService.WSDL_LOCATION.toString());

        String cxfDeliveryAddr = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.CXF_DELIVERY_ENDPOINT_ADDRESS);
        LOGGER.info("Sending push messages to [{}]", cxfDeliveryAddr);

        HashMap<String, Object> wssProperties = getWssProperties(ctx, wsPluginPropertyManager);
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




}
