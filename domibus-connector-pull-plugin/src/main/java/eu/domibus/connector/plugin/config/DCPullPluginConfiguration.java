package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.DCPullPluginPropertyManager;
import eu.domibus.connector.plugin.dao.DCMessageLogDao;
import eu.domibus.connector.plugin.initialize.DCPluginInitializer;
import eu.domibus.connector.plugin.transformer.DCMessageTransformer;
import eu.domibus.connector.plugin.ws.AuthenticationService;
import eu.domibus.connector.plugin.ws.DomibusConnectorPullWebservice;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWSService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;


@Configuration
//@Conditional(PullPluginEnabledCondition.class)
public class DCPullPluginConfiguration extends DCPluginConfiguration {

    public static final String MODULE_NAME = "DC_PULL_PLUGIN" ;
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPullPluginConfiguration.class);

    public static final String PULL_PLUGIN_INITIALIZER = "pullPluginInitializer";
    public static final String PULL_BACKEND_WEBSERVICE_ENDPOINT_BEAN_NAME = "pullBackendWebserviceEndpoint";


    @Bean
    public DCPullPluginPropertyManager dcPullPluginPropertyManager() {
        return new DCPullPluginPropertyManager();
    }

    @Bean
    public DomibusConnectorPullWebservice domibusConnectorPullWebservice(DCMessageTransformer messageTransformer,
                                                                         DCMessageLogDao dcMessageLogDao,
                                                                         DCPullPluginPropertyManager wsPluginPropertyManager,
                                                                         DomibusConfigurationExtService domibusConfigurationExtService,
                                                                         DomainContextExtService domainContextExtService,
                                                                         @Lazy @Qualifier(PULL_PLUGIN_INITIALIZER) DCPluginInitializer pluginInitializer) {
        return new DomibusConnectorPullWebservice(messageTransformer,
                dcMessageLogDao, wsPluginPropertyManager, domibusConfigurationExtService, domainContextExtService, pluginInitializer);
    }


    /**
     * Create endpoint for Pull
     *  SubmissionWebservice
     */
    @Bean("pullBackendWebserviceEndpoint")
    public EndpointImpl pushBackendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                                     DomibusConnectorPullWebservice backendWebService,
                                                     @Qualifier("pullPluginAuthenticationService") AuthenticationService authenticationService,
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
        Map<String, Object> properties = getWssProperties(ctx, wsPluginPropertyManager, "useReqSigCert");
        LOGGER.debug("Setting properties for DC PullPlugin: [{}]", properties);
        endpoint.setProperties(properties);
        if (authenticationService != null) {
            endpoint.getInInterceptors().add(authenticationService);
        }
        return endpoint;
    }

    @Bean("pullPluginAuthenticationService")
    public AuthenticationService certAuthenticationService(DCPullPluginPropertyManager wsPluginPropertyManager,
                                                           ApplicationContext ctx) {
        return new AuthenticationService(wsPluginPropertyManager, ctx);
    }

    @Bean(PULL_PLUGIN_INITIALIZER)
    public DCPluginInitializer dcPluginInitializer(DCPullPluginPropertyManager propertyManager,
                                                   @Qualifier(PULL_BACKEND_WEBSERVICE_ENDPOINT_BEAN_NAME) EndpointImpl endpoint) {
        return new DCPluginInitializer(MODULE_NAME, propertyManager, endpoint);
    }


}
