package eu.domibus.connector.plugin.config;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.TomcatCondition;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import static eu.domibus.connector.plugin.config.DCPluginConfiguration.DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_BEAN;
import static eu.domibus.connector.plugin.config.DCPluginConfiguration.DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN;
import static eu.domibus.connector.plugin.config.property.DCPluginPropertyManager.DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_NAME;
import static eu.domibus.connector.plugin.config.property.DCPluginPropertyManager.DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME;

/**
 * Class responsible for the configuration of the plugin for Tomcat
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(TomcatCondition.class)
@Configuration
public class DCPullPluginTomcatConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DCPullPluginTomcatConfiguration.class);

    @Bean(DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_BEAN)
    public ActiveMQQueue pullPluginMessagesQueue() {
        return new ActiveMQQueue(DC_PULL_PLUGIN_NOTIFICATIONS_QUEUE_NAME);
    }


}
