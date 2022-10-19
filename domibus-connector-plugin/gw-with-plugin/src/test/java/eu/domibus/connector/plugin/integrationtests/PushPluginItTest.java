package eu.domibus.connector.plugin.integrationtests;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.config.DefaultWsCallbackHandler;
import eu.domibus.connector.plugin.config.WsPolicyLoader;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.util.*;

public class PushPluginItTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PushPluginItTest.class);

    @Test
    public void testPushPlugin() {

        //todo get GW connection!!

//        String GW_URL = "https://ecx-gateway-lab04-ju-eu-ejustice-eqs.apps.a2.cp.cna.at/domibus/services/dcpushplugin";
        String GW_URL = "http://localhost:8080/domibus/services/dcpushplugin";

        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(DomibusConnectorGatewaySubmissionWebService.class);

        List<Feature> featureList = featureList();
        LOGGER.debug("Activating the following features for DC-Plugin ClientProxy: [{}]", featureList);
        jaxWsProxyFactoryBean.setFeatures(featureList);
        jaxWsProxyFactoryBean.setServiceName(DomibusConnectorGatewaySubmissionWSService.SERVICE);
        jaxWsProxyFactoryBean.setEndpointName(DomibusConnectorGatewaySubmissionWSService.DomibusConnectorGatewaySubmissionWebService);
        jaxWsProxyFactoryBean.setWsdlLocation(DomibusConnectorGatewaySubmissionWSService.WSDL_LOCATION.toString());


//        String cxfDeliveryAddr = wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_DELIVERY_ENDPOINT_ADDRESS);
        String cxfDeliveryAddr = GW_URL;
        Map<String, Object> properties = jaxWsProperties();
        LOGGER.info("Sending push messages to [{}]", cxfDeliveryAddr);
        LOGGER.debug("Setting properties [{}] for DC-Plugin ClientProxy", properties);
        jaxWsProxyFactoryBean.setProperties(properties);
        jaxWsProxyFactoryBean.setAddress(cxfDeliveryAddr);

        DomibusConnectorMessageType t = new DomibusConnectorMessageType();

        DomibusConnectorGatewaySubmissionWebService client =  (DomibusConnectorGatewaySubmissionWebService) jaxWsProxyFactoryBean.create();
        client.submitMessage(t);


    }


    public List<Feature> featureList() {
        List<Feature> featureList = new ArrayList<>();
        featureList.add(wsPolicyFeature());
        featureList.add(loggingFeature());
//        featureList.add(new WSAddressingFeature());
        return featureList;
    }

    public WSPolicyFeature wsPolicyFeature() {

        ClassPathResource resource = new ClassPathResource("/wsdl/backend.policy.xml");
        if (!resource.exists()) {
            throw new RuntimeException("error");
        }
        WSPolicyFeature wsPolicyFeature = WsPolicyLoader.loadPolicyFeature(resource);
        return wsPolicyFeature;

    }

    public LoggingFeature loggingFeature() {
        LOGGER.debug("CXFLoggingFeature is activated");
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        return loggingFeature;
    }

    public Map<String, Object> jaxWsProperties() {
        HashMap<String, Object> props = new HashMap<>();

        props.put("mtom-enabled", true);
        props.put("security.encryption.properties", gwWsLinkEncryptProperties());
        props.put("security.encryption.username",  "test");
        props.put("security.signature.properties", gwWsLinkEncryptProperties());
//        props.put("security.callback-handler", new DefaultWsCallbackHandler());

        return props;
    }

    public Properties gwWsLinkEncryptProperties() {
        Properties props = new Properties();

//        props.put("org.apache.wss4j.crypto.provider", "org.apache.wss4j.common.crypto.Merlin");
        props.put("org.apache.wss4j.crypto.merlin.keystore.type", "PKCS12");
        props.put("org.apache.wss4j.crypto.merlin.keystore.file", "file:///C:/Entwicklung/test.pcks");
        props.put("org.apache.wss4j.crypto.merlin.keystore.password", "test");
        props.put("org.apache.wss4j.crypto.merlin.keystore.alias", "test");
        props.put("org.apache.wss4j.crypto.merlin.keystore.private.password", "test");
        props.put("org.apache.wss4j.crypto.merlin.truststore.type", "PKCS12");
        props.put("org.apache.wss4j.crypto.merlin.truststore.file", "file:///C:/Entwicklung/test.pcks");
        props.put("org.apache.wss4j.crypto.merlin.truststore.password", "test");

        return props;
    }

}
