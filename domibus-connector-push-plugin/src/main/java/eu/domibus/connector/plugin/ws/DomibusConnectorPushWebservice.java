package eu.domibus.connector.plugin.ws;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.config.property.DCPushPluginPropertyManager;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.transformer.DCMessageTransformer;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.initialize.PluginInitializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.ws.BindingProvider;
import java.util.Map;


public class DomibusConnectorPushWebservice extends AbstractDcPluginBackendConnector implements DomibusConnectorGatewaySubmissionWebService {

	public static final String PLUGIN_NAME = "DC_PUSH_PLUGIN";

	private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorPushWebservice.class);
	private final ObjectProvider<DomibusConnectorGatewayDeliveryWebService> deliveryClientObjectFactory;

	public DomibusConnectorPushWebservice(DCMessageTransformer messageTransformer,
										  DCPushPluginPropertyManager wsPluginPropertyManager,
										  ObjectProvider<DomibusConnectorGatewayDeliveryWebService> deliveryClientObjectFactory,
										  PluginInitializer pluginInitializer) {
		super(PLUGIN_NAME, wsPluginPropertyManager, messageTransformer, pluginInitializer);
		this.deliveryClientObjectFactory = deliveryClientObjectFactory;
	}

	@Override
    public DomibsConnectorAcknowledgementType submitMessage(DomibusConnectorMessageType submitMessageRequest) {
        return new SubmitMessage(submitMessageRequest, this).invoke();
    }

	@Override
	@Transactional
	public void deliverMessage(final DeliverMessageEvent deliverMessageEvent) {
		String messageId = deliverMessageEvent.getMessageId();
		LOGGER.debug("Download message [{}] from Queue.", messageId);
		DomibusConnectorMessage message = new DomibusConnectorMessage(objectFactory.createDomibusConnectorMessageType());
		try {
			downloadMessage(messageId, message);
		} catch (MessageNotFoundException e) {
			String error = String.format("Message with ID %s not found!", messageId);
			LOGGER.error(error, e);
			throw new RuntimeException(error, e);
		}

		LOGGER.debug("Successfully downloaded message [{}] from Queue.", messageId);
		try {
			DomibsConnectorAcknowledgementType ack = deliveryClientObjectFactory.getObject().deliverMessage(message.getConnectorMessage());
			if(ack.isResult()) {
				LOGGER.info("Successfully delivered message [{}] to domibusConnector.", messageId);

			}else {
				String error = "Message with ID " + messageId + " not delivered successfully to domibusConnector: "+ack.getResultMessage();
				LOGGER.error(error);
				throw new RuntimeException(error);
			}
		} catch (Exception e) {
			LOGGER.error("Error occured while delivering message " + messageId + " to connector", e);
		}

	}

}
