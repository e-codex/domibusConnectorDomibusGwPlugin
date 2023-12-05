package eu.domibus.connector.plugin.config;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.ApplicationServerCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.Queue;

//import static eu.domibus.connector.plugin.config.DCPluginConfiguration.DC_PLUGIN_NOTIFICATIONS_QUEUE_BEAN;

/**
 * Class responsible for the configuration of the plugin for an application server, WebLogic and WildFly
 *
 */
@Conditional(ApplicationServerCondition.class)
@Configuration
public class DCPluginApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DCPluginApplicationServerConfiguration.class);

    //TODO: write for push and pull plugin!
//    @Bean(DC_PLUGIN_NOTIFICATIONS_QUEUE_BEAN)
//    public JndiObjectFactoryBean notifyBackendWebServiceQueue() {
//        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
//
//        String queueNotificationJndi = DCPluginConfiguration.DC_PLUGIN_NOTIFICATIONS_QUEUE_JNDI;
//        LOG.debug("Using queue messages jndi for dcMessages [{}]", queueNotificationJndi);
//        jndiObjectFactoryBean.setJndiName(queueNotificationJndi);
//
//        jndiObjectFactoryBean.setExpectedType(Queue.class);
//        return jndiObjectFactoryBean;
//    }

}
