package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPullPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Arrays;

public class DCPullPluginPropertyManager extends AbstractDCPluginPropertyManager {

    public static final String DC_PULL_PLUGIN_ENABLED_PROPERTY_NAME = "dcplugin.pull.enabled";


    public DCPullPluginPropertyManager() {
        super(Arrays.asList(
                new DomibusPropertyMetadataDTO(DC_PULL_PLUGIN_CXF_PUBLISH_URL, DomibusPropertyMetadataDTO.Type.STRING, DCPullPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PULL_PLUGIN_ENABLED_PROPERTY_NAME, DomibusPropertyMetadataDTO.Type.BOOLEAN, DCPullPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL)
        ), DCPullPluginConfiguration.MODULE_NAME);

    }

}
