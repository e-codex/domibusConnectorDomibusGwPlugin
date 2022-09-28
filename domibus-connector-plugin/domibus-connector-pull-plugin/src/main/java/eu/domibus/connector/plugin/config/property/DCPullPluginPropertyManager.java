package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Arrays;
import java.util.List;

public class DCPullPluginPropertyManager extends DCPluginPropertyManager {


    public DCPullPluginPropertyManager() {
        super(Arrays.asList(
                new DomibusPropertyMetadataDTO(DC_PULL_PLUGIN_CXF_PUBLISH_URL, DomibusPropertyMetadataDTO.Type.STRING, DCPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL)
        ));

    }

}
