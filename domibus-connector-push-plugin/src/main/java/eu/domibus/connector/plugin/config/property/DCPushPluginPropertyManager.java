package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPushPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Arrays;

public class DCPushPluginPropertyManager extends AbstractDCPluginPropertyManager {

    public static final String DC_PUSH_PLUGIN_ENABLED_PROPERTY_NAME = "dcplugin.push.enabled";

    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME_PROPERTY_NAME = "dcplugin.push.notifications.queue";

    protected String getPropertiesFileName() {
        return "dc-push-plugin.properties";
    }

    public DCPushPluginPropertyManager() {
        super(Arrays.asList(
                new DomibusPropertyMetadataDTO(DC_PUSH_PLUGIN_CXF_PUBLISH_URL, DomibusPropertyMetadataDTO.Type.STRING, DCPushPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME_PROPERTY_NAME, DomibusPropertyMetadataDTO.Type.STRING, DCPushPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL
                )

        ), DCPushPluginConfiguration.MODULE_NAME);
    }

}
