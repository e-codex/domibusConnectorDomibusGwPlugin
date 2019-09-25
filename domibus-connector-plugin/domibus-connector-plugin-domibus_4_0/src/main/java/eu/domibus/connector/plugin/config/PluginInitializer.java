package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.ws.DomibusConnectorWebservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;

public class PluginInitializer {

    @Autowired
    public ApplicationContext context;

    private DomibusConnectorWebservice domibusConnectorWebservice;

    @PostConstruct
    public void init() {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(new String[]
                {"classpath:eu/domibus/connector/plugin/domibus-connector-plugin-child-context.xml"},
                context);
        this.domibusConnectorWebservice = classPathXmlApplicationContext.getBean("domibusConnectorWebservice", DomibusConnectorWebservice.class);
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public DomibusConnectorWebservice getDomibusConnectorWebservice() {
        return domibusConnectorWebservice;
    }

    public void setDomibusConnectorWebservice(DomibusConnectorWebservice domibusConnectorWebservice) {
        this.domibusConnectorWebservice = domibusConnectorWebservice;
    }
}
