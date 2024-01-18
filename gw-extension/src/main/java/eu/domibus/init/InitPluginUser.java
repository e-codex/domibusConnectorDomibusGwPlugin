package eu.domibus.init;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.api.user.plugin.PluginUserService;
import eu.domibus.core.pmode.PModeLoadFromFileService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InitPluginUser {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeLoadFromFileService.class);
    public static final String DEFAULT_PLUGIN_USER_NAME = "ext.default-plugin-user.name";
    public static final String DEFAULT_PLUGIN_USER_PASSWORD = "ext.default-plugin-user.password";

    private final DomibusPropertyProvider domibusPropertyProvider;
    private final PluginUserService pluginUserService;

    public InitPluginUser(DomibusPropertyProvider domibusPropertyProvider, PluginUserService pluginUserService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.pluginUserService = pluginUserService;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() == null) {
            LOG.info("Skipping event: we are processing only the web application context event");
            return;
        }

        createPluginUser();

        LOG.info("Finished processing ContextRefreshedEvent");
    }

    private void createPluginUser() {
        String pluginUserName = domibusPropertyProvider.getProperty(DEFAULT_PLUGIN_USER_NAME);
        String pluginUserPassword = domibusPropertyProvider.getProperty(DEFAULT_PLUGIN_USER_PASSWORD);

        if (StringUtils.hasText(pluginUserName) && StringUtils.hasText(pluginUserPassword)) {
            LOG.info("Default Plugin User is set, creating it.");

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("admin", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            AuthenticationEntity e = new AuthenticationEntity();
            e.setUserName(pluginUserName);
            e.setPassword(pluginUserPassword);
            e.setAuthRoles("ROLE_ADMIN");
            e.setActive(true);

            pluginUserService.updateUsers(Stream.of(e).collect(Collectors.toList()), new ArrayList<>(), new ArrayList<>());
            LOG.info("Default Plugin User has been created");

        } else {
            LOG.info("No default plugin user is set for configuration - continue");
            return;
        }



    }

}
