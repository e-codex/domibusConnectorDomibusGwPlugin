package eu.domibus.connector.plugin.integrationtests;

import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageDetailsType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.config.DefaultWsCallbackHandler;
import eu.domibus.connector.plugin.config.WsPolicyLoader;
import eu.domibus.connector.plugin.ws.DomibusConnectorPullWebservice;
import eu.domibus.connector.testdata.TransitionCreator;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWSService;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWebService;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsRequest;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsResponse;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void testPullPlugin() throws InterruptedException {
        String GW_URL = "http://localhost:8080/domibus/services/dcpullplugin";

        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(DomibusConnectorGatewayWebService.class);

        List<Feature> featureList = featureList();
        LOGGER.debug("Activating the following features for DC-Plugin ClientProxy: [{}]", featureList);
        jaxWsProxyFactoryBean.setFeatures(featureList);
        jaxWsProxyFactoryBean.setServiceName(DomibusConnectorGatewayWSService.SERVICE);
        jaxWsProxyFactoryBean.setEndpointName(DomibusConnectorGatewayWSService.DomibusConnectorGatewayWebService);
        jaxWsProxyFactoryBean.setWsdlLocation(DomibusConnectorGatewayWSService.WSDL_LOCATION.toString());


//        String cxfDeliveryAddr = wsPluginPropertyManager.getKnownPropertyValue(DCPluginPropertyManager.CXF_DELIVERY_ENDPOINT_ADDRESS);
        String cxfDeliveryAddr = GW_URL;
        Map<String, Object> properties = jaxWsProperties();
        LOGGER.info("Sending push messages to [{}]", cxfDeliveryAddr);
        LOGGER.debug("Setting properties [{}] for DC-Plugin ClientProxy", properties);
        jaxWsProxyFactoryBean.setProperties(properties);
        jaxWsProxyFactoryBean.setAddress(cxfDeliveryAddr);


        DomibusConnectorMessageType epoMessage = TransitionCreator.createEpoMessage();
        epoMessage.getMessageDetails().getFromParty().setPartyId("gw01");
        epoMessage.getMessageDetails().getFromParty().setPartyIdType("urn:oasis:names:tc:ebcore:partyid-type:eevidence");
        epoMessage.getMessageDetails().getFromParty().setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        epoMessage.getMessageDetails().getToParty().setPartyId("gw01");
        epoMessage.getMessageDetails().getToParty().setPartyIdType("urn:oasis:names:tc:ebcore:partyid-type:eevidence");
        epoMessage.getMessageDetails().getToParty().setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        epoMessage.getMessageDetails().getService().setService("service1");
        epoMessage.getMessageDetails().getService().setServiceType("urn:e-codex:services:");
        epoMessage.getMessageDetails().getAction().setAction("action1");






        //submit message to GW
        DomibusConnectorGatewayWebService client =  (DomibusConnectorGatewayWebService) jaxWsProxyFactoryBean.create();
        DomibsConnectorAcknowledgementType domibsConnectorAcknowledgementType = client.submitMessage(epoMessage);
        assertThat(domibsConnectorAcknowledgementType.isResult()).isTrue();

        //TODO: pull message from GW
        ListPendingMessageIdsRequest listPendingMessageIdsRequest = new ListPendingMessageIdsRequest();
        ListPendingMessageIdsResponse listPendingMessageIdsResponse = client.listPendingMessageIds(listPendingMessageIdsRequest);

        //10s
        Thread.sleep(10000);

        //assume 1 pending message
        assertThat(listPendingMessageIdsResponse.getMessageIds()).hasSize(1);


    }


    public List<Feature> featureList() {
        List<Feature> featureList = new ArrayList<>();
        featureList.add(wsPolicyFeature());
//        featureList.add(loggingFeature());
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
        props.put("security.encryption.username",  "gw");
        props.put("security.signature.properties", gwWsLinkEncryptProperties());
        props.put("security.callback-handler", new DefaultWsCallbackHandler());

        return props;
    }

    public Properties gwWsLinkEncryptProperties() {
        Properties props = new Properties();

        props.put("org.apache.wss4j.crypto.merlin.keystore.type", "PKCS12");
        props.put("org.apache.wss4j.crypto.merlin.keystore.file", "keystores/connector-gwlink-keystore.p12");
        props.put("org.apache.wss4j.crypto.merlin.keystore.password", "12345");
        props.put("org.apache.wss4j.crypto.merlin.keystore.alias", "connector");
        props.put("org.apache.wss4j.crypto.merlin.keystore.private.password", "12345");
        props.put("org.apache.wss4j.crypto.merlin.truststore.type", "PKCS12");
        props.put("org.apache.wss4j.crypto.merlin.truststore.file", "keystores/connector-gwlink-truststore.p12");
        props.put("org.apache.wss4j.crypto.merlin.truststore.password", "12345");


        return props;
    }

}
