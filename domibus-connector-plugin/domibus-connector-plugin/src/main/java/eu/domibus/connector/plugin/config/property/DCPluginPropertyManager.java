package eu.domibus.connector.plugin.config.property;

import eu.domibus.connector.plugin.config.DCPluginConfiguration;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DCPluginPropertyManager extends DomibusPropertyExtServiceDelegateAbstract
        implements DomibusPropertyManagerExt {

    private final Map<String, DomibusPropertyMetadataDTO> knownProperties;

    protected String getPropertiesFileName() {
        return "dc-plugin.properties";
    }

    public DCPluginPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_DELIVERY_ENDPOINT_ADDRESS, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_ENCRYPT_ALIAS, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_KEY_STORE_PASSWORD, Type.PASSWORD, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_KEY_STORE_PATH_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_KEY_STORE_TYPE, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_PRIVATE_KEY_ALIAS, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_PRIVATE_KEY_PASSWORD, Type.PASSWORD, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_TRUST_STORE_TYPE_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_TRUST_STORE_PATH_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME, Type.PASSWORD, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_SECURITY_POLICY, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.PLUGIN_DELIVERY_MODE, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_LOGGING_FEATURE_PROPERTY_NAME, Type.BOOLEAN, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.CXF_PUBLISH_URL, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME, Type.STRING, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DCPluginConfiguration.DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME, Type.COMMA_SEPARATED_LIST, DCPluginConfiguration.MODULE_NAME, Usage.GLOBAL)

        );
        this.knownProperties = allProperties.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }


}
