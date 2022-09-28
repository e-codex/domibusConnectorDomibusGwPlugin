package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Arrays;

import static eu.domibus.connector.plugin.config.property.DCPluginPropertyManager.DC_PUSH_PLUGIN_CXF_PUBLISH_URL;

public class DCPushPluginPropertyManager extends DCPluginPropertyManager {

    public DCPushPluginPropertyManager() {
        super(Arrays.asList(
                new DomibusPropertyMetadataDTO(DC_PUSH_PLUGIN_CXF_PUBLISH_URL, DomibusPropertyMetadataDTO.Type.STRING, DCPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL)
        ));
    }

}
