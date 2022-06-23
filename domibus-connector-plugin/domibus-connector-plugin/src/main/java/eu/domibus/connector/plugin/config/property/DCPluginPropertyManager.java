package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DCPluginPropertyManager extends DomibusPropertyExtServiceDelegateAbstract
        implements DomibusPropertyManagerExt {

    public static final String CXF_TRUST_STORE_PATH_PROPERTY_NAME = "connector.delivery.trust-store.file";
    public static final String CXF_TRUST_STORE = "connector.delivery.trust-store";
    public static final String CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME = "connector.delivery.trust-store.password";
    public static final String CXF_TRUST_STORE_TYPE_PROPERTY_NAME = "connector.delivery.trust-store.type";
    public static final String CXF_KEY_STORE_PATH_PROPERTY_NAME = "connector.delivery.key-store.file";
    public static final String CXF_KEY_STORE_PASSWORD = "connector.delivery.key-store.password";
    public static final String CXF_KEY_STORE = "connector.delivery.key-store";
    public static final String CXF_KEY_STORE_TYPE = "connector.delivery.key-store.type";
    public static final String CXF_PRIVATE_KEY_ALIAS = "connector.delivery.private-key.alias";
    public static final String CXF_PRIVATE_KEY_PASSWORD = "connector.delivery.private-key.password";
    public static final String CXF_ENCRYPT_ALIAS = "connector.delivery.encrypt-alias";
    public static final String CXF_DELIVERY_ENDPOINT_ADDRESS = "connector.delivery.service.address";
    public static final String CXF_SECURITY_POLICY = "connector.delivery.service.service.security-policy";
    public static final String CXF_LOGGING_FEATURE_PROPERTY_NAME = "connector.delivery.service.service.logging-feature.enabled";
    public static final String PLUGIN_DELIVERY_MODE = "connector.delivery.mode";
    public static final String DC_PLUGIN_MAX_MESSAGE_LIST = "connector.delivery.pull.messages.pending.list.max";
    public static final String DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME = "dcplugin.auth.username";
    public static final String DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME = "dcplugin.auth.roles";
    public static final String DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME = "dcplugin.auth.use-username-from"; //ALIAS, DN, DEFAULT
    public static final String CXF_PUBLISH_URL = "connector.delivery.service.publish";
    public static final String DC_PLUGIN_NOTIFICATIONS_QUEUE_NAME = "domibus.dcplugin.notifications";
    private final Map<String, DomibusPropertyMetadataDTO> knownProperties;

    protected String getPropertiesFileName() {
        return "dc-plugin.properties";
    }

    public DCPluginPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(CXF_DELIVERY_ENDPOINT_ADDRESS, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_ENCRYPT_ALIAS, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_KEY_STORE_PASSWORD, Type.PASSWORD, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_KEY_STORE_PATH_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_KEY_STORE_TYPE, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_PRIVATE_KEY_ALIAS, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_PRIVATE_KEY_PASSWORD, Type.PASSWORD, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_TRUST_STORE_TYPE_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_TRUST_STORE_PATH_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME, Type.PASSWORD, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_SECURITY_POLICY, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(PLUGIN_DELIVERY_MODE, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_LOGGING_FEATURE_PROPERTY_NAME, Type.BOOLEAN, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_PUBLISH_URL, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME, Type.COMMA_SEPARATED_LIST, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_MAX_MESSAGE_LIST, Type.NUMERIC, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL)

        );
        this.knownProperties = allProperties.stream().collect(Collectors.toMap(DomibusPropertyMetadataDTO::getName, Function.identity()));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }


}
