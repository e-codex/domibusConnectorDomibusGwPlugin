package eu.domibus.core.pmode;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Service
public class PModeLoadFromFileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeLoadFromFileService.class);
    public static final String PMODE_RESOURCE_URL_PROPERTY_NAME = "ext.load-pmodes.url";
    private final DomibusPropertyProvider domibusPropertyProvider;
    private final PModeProvider pModeProvider;

    private final ResourceLoader resourceLoader;

    public PModeLoadFromFileService(DomibusPropertyProvider domibusPropertyProvider,
                                    PModeProvider pModeProvider,
                                    ResourceLoader resourceLoader) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.pModeProvider = pModeProvider;


        this.resourceLoader = resourceLoader;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() == null) {
            LOG.info("Skipping event: we are processing only the web application context event");
            return;
        }

        uploadPModesFromUrl();

        LOG.info("Finished processing ContextRefreshedEvent");
    }

    public void uploadPModesFromUrl() {
        String pModeUrl = domibusPropertyProvider.getProperty(PMODE_RESOURCE_URL_PROPERTY_NAME);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("admin", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        if (pModeUrl != null) {
            LOG.warn("Automatic P-Mode import is activated because property [{}] exists! P-Modes will be updated from [{}]", PMODE_RESOURCE_URL_PROPERTY_NAME, pModeUrl);
            Resource resource = resourceLoader.getResource(pModeUrl);
            if (!resource.isReadable()) {
                String error = String.format("Cannot load resource from [%s] because resource does not exist, check property: [%s]", pModeUrl, PMODE_RESOURCE_URL_PROPERTY_NAME);
                LOG.error(error);
                return;
            }
            try (InputStream is = resource.getInputStream()) {
                String pModes = StreamUtils.copyToString(is, StandardCharsets.UTF_8);

                PropertyPlaceholderHelper.PlaceholderResolver resolver = placeholderName -> domibusPropertyProvider.getProperty(placeholderName);
                PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
                String replacedPModes = propertyPlaceholderHelper.replacePlaceholders(pModes, resolver);

                pModeProvider.updatePModes(replacedPModes.getBytes(StandardCharsets.UTF_8), "Auto uploaded p-Modes during startup");
            } catch (IOException | XmlProcessingException e) {
                String error = String.format("Cannot load resource from [%s], check property: [%s]", pModeUrl, PMODE_RESOURCE_URL_PROPERTY_NAME);
                LOG.error(error, e);
            }
        }

    }


}
