package eu.domibus.connector.plugin.config;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.TomcatCondition;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import static eu.domibus.connector.plugin.config.DCPluginConfiguration.DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN;
import static eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager.DEFAULT_DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME;

/**
 * Class responsible for the configuration of the plugin for Tomcat
 *
 */
@Conditional(TomcatCondition.class)
@Configuration
public class DCPushPluginTomcatConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DCPushPluginTomcatConfiguration.class);

    @Bean
    @Qualifier(DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_BEAN)
    public ActiveMQQueue pushPluginMessagesQueue() {
        return new ActiveMQQueue(DEFAULT_DC_PUSH_PLUGIN_NOTIFICATIONS_QUEUE_NAME);
    }

}
