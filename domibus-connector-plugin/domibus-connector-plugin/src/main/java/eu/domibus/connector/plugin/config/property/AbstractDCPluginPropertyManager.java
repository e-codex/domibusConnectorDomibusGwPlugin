package eu.domibus.connector.plugin.config.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
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
    public static final String CXF_ENCRYPT_ALIAS = "connector.delivery.encrypt-alias";
    public static final String CXF_DELIVERY_ENDPOINT_ADDRESS = "connector.delivery.service.address";
    public static final String CXF_SECURITY_POLICY = "connector.delivery.service.service.security-policy";
    public static final String CXF_LOGGING_FEATURE_PROPERTY_NAME = "connector.delivery.service.service.logging-feature.enabled";
    public static final String DC_PLUGIN_MAX_MESSAGE_LIST = "connector.delivery.pull.messages.pending.list.max";
    public static final String DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME = "dcplugin.auth.username";
    public static final String DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME = "dcplugin.auth.roles";
    public static final String DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME = "dcplugin.auth.use-username-from"; //ALIAS, DN, DEFAULT

    public static final String DC_PUSH_PLUGIN_CXF_PUBLISH_URL = "dcplugin.push.publish.url";
    public static final String DC_PULL_PLUGIN_CXF_PUBLISH_URL = "dcplugin.pull.publish.url";

    public static final String DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME_PROPERTY_NAME = "dcplugin.push.notifications.queue";
    public static final String DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_NAME_PROPERTY_NAME = "dcplugin.pull.notifications.queue";
    public static final String DEFAULT_DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME = "domibus.dcplugin.push.notifications.queue";
    public static final String DEFAULT_DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_NAME = "domibus.dcplugin.pull.notifications.queue";
    private final Map<String, DomibusPropertyMetadataDTO> knownProperties;

//    protected String getPropertiesFileName() {
//        return "dc-plugin.properties";
//    }

    public AbstractDCPluginPropertyManager(List<DomibusPropertyMetadataDTO> properties, String moduleName) {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(CXF_DELIVERY_ENDPOINT_ADDRESS, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_ENCRYPT_ALIAS, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_KEY_STORE_PASSWORD, Type.PASSWORD, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_KEY_STORE_PATH_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_KEY_STORE_TYPE, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_PRIVATE_KEY_ALIAS, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_PRIVATE_KEY_PASSWORD, Type.PASSWORD, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_TRUST_STORE_TYPE_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_TRUST_STORE_PATH_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME, Type.PASSWORD, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_SECURITY_POLICY, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CXF_LOGGING_FEATURE_PROPERTY_NAME, Type.BOOLEAN, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME, Type.STRING, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME, Type.COMMA_SEPARATED_LIST, moduleName, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DC_PLUGIN_MAX_MESSAGE_LIST, Type.NUMERIC, moduleName, Usage.GLOBAL)

        );

        this.knownProperties = Stream.of(allProperties, properties)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(DomibusPropertyMetadataDTO::getName, Function.identity()));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }


}
