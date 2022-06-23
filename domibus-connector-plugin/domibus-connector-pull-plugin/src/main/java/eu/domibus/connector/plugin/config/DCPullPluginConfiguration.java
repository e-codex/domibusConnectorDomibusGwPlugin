package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.DCPluginPropertyManager;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.connector.plugin.ws.DomibusConnectorPullWebservice;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static eu.domibus.connector.plugin.config.DCPluginConfiguration.DC_PLUGIN_CXF_FEATURE;
import static eu.domibus.connector.plugin.config.DCPluginConfiguration.JAXWS_PROPERTIES_BEAN_NAME;

@Configuration
public class DCPullPluginConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPullPluginConfiguration.class);


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
                                                     DCPluginPropertyManager wsPluginPropertyManager,
                                                     @Qualifier(DC_PLUGIN_CXF_FEATURE) List<Feature> featureList,
                                                     @Qualifier(JAXWS_PROPERTIES_BEAN_NAME) Map<String, Object> jaxWsProperties

    ) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService); //NOSONAR

        endpoint.setServiceName(DomibusConnectorGatewaySubmissionWSService.SERVICE);
        endpoint.setEndpointName(DomibusConnectorGatewaySubmissionWSService.DomibusConnectorGatewaySubmissionWebService);
        endpoint.setWsdlLocation(DomibusConnectorGatewaySubmissionWSService.WSDL_LOCATION.toString());

        LOGGER.debug("Activating the following features for DC-Plugin PullPlugin: [{}]", featureList);
        endpoint.setFeatures(featureList);
        Map<String, Object> properties = jaxWsProperties;
        LOGGER.debug("Setting properties for DC PullPlugin: [{}]", properties);
        endpoint.setProperties(properties);
        if (authenticationService != null) {
            endpoint.getInInterceptors().add(authenticationService);
        }
        LOGGER.info("Publish URL for DC PullPlugin is: [{}]", wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_PUBLISH_URL));
        endpoint.publish(wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_PUBLISH_URL));
        return endpoint;
    }



}
