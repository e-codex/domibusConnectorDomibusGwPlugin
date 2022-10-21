package eu.domibus.connector.plugin.config;

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

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.connector.plugin.config.DCPluginConfiguration.*;
import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.connector.plugin.ws.AuthenticationService;

@Configuration
public class DCPluginGeneralConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPluginGeneralConfiguration.class);




    @Bean
    public AuthenticationService certAuthenticationService(AbstractDCPluginPropertyManager wsPluginPropertyManager,
                                                           ApplicationContext ctx) {
        return new AuthenticationService(wsPluginPropertyManager, ctx);
    }
}
