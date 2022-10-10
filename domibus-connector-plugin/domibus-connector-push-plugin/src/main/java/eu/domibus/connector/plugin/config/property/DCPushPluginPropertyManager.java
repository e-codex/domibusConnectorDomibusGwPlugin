package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPushPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Arrays;

public class DCPushPluginPropertyManager extends AbstractDCPluginPropertyManager {

    public static final String DC_PUSH_PLUGIN_ENABLED_PROPERTY_NAME = "dcplugin.push.enabled";

    public DCPushPluginPropertyManager() {
        super(Arrays.asList(
                new DomibusPropertyMetadataDTO(DC_PUSH_PLUGIN_CXF_PUBLISH_URL, DomibusPropertyMetadataDTO.Type.STRING, DCPushPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL)

        ), DCPushPluginConfiguration.MODULE_NAME);
    }

}
