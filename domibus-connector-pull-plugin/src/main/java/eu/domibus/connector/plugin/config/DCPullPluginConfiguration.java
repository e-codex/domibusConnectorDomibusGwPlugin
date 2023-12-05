package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.connector.plugin.config.property.DCPullPluginPropertyManager;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.connector.plugin.ws.DomibusConnectorPullWebservice;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWSService;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWebService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;


@Configuration
@Conditional(PullPluginEnabledCondition.class)
public class DCPullPluginConfiguration extends DCPluginConfiguration {

    public static final String MODULE_NAME = "DC_PULL_PLUGIN" ;
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPullPluginConfiguration.class);

    @PostConstruct
    public static void postConstruct() {
        LOGGER.info("Push Plugin is enabled");
    }


    @Bean
    public DCPullPluginPropertyManager dcPluginPropertyManager() {
        return new DCPullPluginPropertyManager();
    }

    @Bean
    public DomibusConnectorPullWebservice domibusConnectorPullWebservice() {
        return new DomibusConnectorPullWebservice();
    }


    /**
     * Create endpoint for Pull
     *  SubmissionWebservice
     */
    @Bean("pullBackendWebserviceEndpoint")
    public EndpointImpl pushBackendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                                     DomibusConnectorPullWebservice backendWebService,
                                                     AuthenticationService authenticationService,
                                                     DCPullPluginPropertyManager wsPluginPropertyManager,
                                                     ApplicationContext ctx
    ) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService); //NOSONAR

        endpoint.setServiceName(DomibusConnectorGatewayWSService.SERVICE);
        endpoint.setEndpointName(DomibusConnectorGatewayWSService.DomibusConnectorGatewayWebService);
        endpoint.setWsdlLocation(DomibusConnectorGatewayWSService.WSDL_LOCATION.toString());

        List<Feature> featureList = getFeatureList(ctx, wsPluginPropertyManager);
        LOGGER.debug("Activating the following features for DC-Plugin PullPlugin: [{}]", featureList);
        endpoint.setFeatures(featureList);
        Map<String, Object> properties = getWssProperties(ctx, wsPluginPropertyManager);
        LOGGER.debug("Setting properties for DC PullPlugin: [{}]", properties);
        endpoint.setProperties(properties);
        if (authenticationService != null) {
            endpoint.getInInterceptors().add(authenticationService);
        }
        LOGGER.info("Publish URL for DC PullPlugin is: [{}]", wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.DC_PULL_PLUGIN_CXF_PUBLISH_URL));
        endpoint.publish(wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.DC_PULL_PLUGIN_CXF_PUBLISH_URL));
        return endpoint;
    }



}
