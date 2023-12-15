package eu.domibus.connector.plugin.initialize;

import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.initialize.PluginInitializer;
import org.apache.cxf.jaxws.EndpointImpl;

public class DCPluginInitializer implements PluginInitializer {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DCPluginInitializer.class);
    private final String pluginName;
    private final DomibusPropertyExtServiceDelegateAbstract wsPluginPropertyManager;
    private final EndpointImpl endpoint;

    public DCPluginInitializer(String pluginName, DomibusPropertyExtServiceDelegateAbstract wsPluginPropertyManager,
                               EndpointImpl endpoint) {
        this.pluginName = pluginName;
        this.wsPluginPropertyManager = wsPluginPropertyManager;
        this.endpoint = endpoint;
    }

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public void initializeNonSynchronized() {
        String publishUrl = wsPluginPropertyManager.getKnownPropertyValue(AbstractDCPluginPropertyManager.DC_PLUGIN_CXF_PUBLISH_URL);
        LOGGER.info("Publishing plugin [{}] under [{}]", pluginName, publishUrl);
        endpoint.publish(publishUrl);
    }

    @Override
    public void initializeWithLockIfNeeded() {
        //not needed
    }
}
