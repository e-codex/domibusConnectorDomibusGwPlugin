package eu.domibus.connector.plugin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;

public class PluginInitializer {

    @Autowired
    public ApplicationContext context;

    @PostConstruct
    public void init() {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(new String[]
                {"classpath:eu/domibus/connector/plugin/domibus-connector-plugin-child-context.xml"},
                context);
        Object domibusConnectorWebservice = classPathXmlApplicationContext.getBean("domibusConnectorWebservice");

    }

}
