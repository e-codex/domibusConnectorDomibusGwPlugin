package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPushPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Arrays;

public class DCPushPluginPropertyManager extends AbstractDCPluginPropertyManager {

    public static final String DC_PUSH_PLUGIN_PROPERTY_PREFIX = "dcplugin.push";

    public static final String CXF_ENCRYPT_ALIAS = DC_PUSH_PLUGIN_PROPERTY_PREFIX + ".connector.delivery.encrypt-alias";
    public static final String CXF_DELIVERY_ENDPOINT_ADDRESS = DC_PUSH_PLUGIN_PROPERTY_PREFIX + ".connector.delivery.service.address";
    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME_PROPERTY_NAME = DC_PUSH_PLUGIN_PROPERTY_PREFIX + ".notifications.queue";

    protected String getPropertiesFileName() {
        return "dc-push-plugin.properties";
    }

    public DCPushPluginPropertyManager() {
        super(Arrays.asList(
                new DomibusPropertyMetadataDTO(CXF_DELIVERY_ENDPOINT_ADDRESS, DomibusPropertyMetadataDTO.Type.STRING, DCPushPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.DOMAIN),
                new DomibusPropertyMetadataDTO(CXF_ENCRYPT_ALIAS, DomibusPropertyMetadataDTO.Type.STRING, DCPushPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.DOMAIN),
                new DomibusPropertyMetadataDTO(DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME_PROPERTY_NAME, DomibusPropertyMetadataDTO.Type.STRING, DCPushPluginConfiguration.MODULE_NAME, DomibusPropertyMetadataDTO.Usage.GLOBAL
                )

        ), DCPushPluginConfiguration.MODULE_NAME, DC_PUSH_PLUGIN_PROPERTY_PREFIX);
    }

}
