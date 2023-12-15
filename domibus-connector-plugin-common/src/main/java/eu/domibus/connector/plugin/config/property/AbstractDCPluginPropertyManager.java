package eu.domibus.connector.plugin.config.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractDCPluginPropertyManager extends DomibusPropertyExtServiceDelegateAbstract
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

    public static final String CXF_SECURITY_POLICY = "connector.delivery.service.service.security-policy";
    public static final String CXF_LOGGING_FEATURE_PROPERTY_NAME = "connector.delivery.service.service.logging-feature.enabled";
    public static final String DC_PLUGIN_MAX_MESSAGE_LIST = "connector.delivery.pull.messages.pending.list.max";
    public static final String DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME = "auth.username";
    public static final String DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME = "auth.roles";
    public static final String DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME = "auth.use-username-from"; //ALIAS, DN, DEFAULT

    public static final String DC_PLUGIN_CXF_PUBLISH_URL = "publish.url";
    public static final String DC_PLUGIN_ENABLED = "enabled";


    private final Map<String, DomibusPropertyMetadataDTO> knownProperties;
    private final String dcPluginPrefix;

    public AbstractDCPluginPropertyManager(List<DomibusPropertyMetadataDTO> properties, String moduleName, String propertyPrefix) {
        this.dcPluginPrefix = propertyPrefix;
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + DC_PLUGIN_ENABLED, Type.BOOLEAN, Module.WS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_KEY_STORE_PASSWORD, Type.PASSWORD, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_KEY_STORE_PATH_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_KEY_STORE_TYPE, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_PRIVATE_KEY_ALIAS, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_PRIVATE_KEY_PASSWORD, Type.PASSWORD, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_TRUST_STORE_TYPE_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_TRUST_STORE_PATH_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME, Type.PASSWORD, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_SECURITY_POLICY, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + CXF_LOGGING_FEATURE_PROPERTY_NAME, Type.BOOLEAN, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME, Type.COMMA_SEPARATED_LIST, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + DC_PLUGIN_MAX_MESSAGE_LIST, Type.NUMERIC, moduleName, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(propertyPrefix + "." + DC_PLUGIN_CXF_PUBLISH_URL, Type.STRING, moduleName, Usage.GLOBAL)
        );

        this.knownProperties = Stream.of(allProperties, properties)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(DomibusPropertyMetadataDTO::getName, Function.identity()));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

    public String getDomainEnabledPropertyName() {
        return withPluginPrefix(DC_PLUGIN_ENABLED);
    }

    public String withPluginPrefix(String dcPluginUseUsernameFromPropertyName) {
        return dcPluginPrefix + "." + dcPluginUseUsernameFromPropertyName;
    }
    public String getKnownPropertyValueWithPrefix(String propertyValue) {
        return super.getKnownPropertyValue(withPluginPrefix(propertyValue));
    }
}
