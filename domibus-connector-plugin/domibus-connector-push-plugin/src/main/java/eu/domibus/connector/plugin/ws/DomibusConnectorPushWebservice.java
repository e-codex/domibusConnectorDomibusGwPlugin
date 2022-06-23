package eu.domibus.connector.plugin.ws;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


public class DomibusConnectorPushWebservice extends AbstractDcPluginBackendConnector implements DomibusConnectorGatewaySubmissionWebService {

	public static final String PLUGIN_NAME = "DC_PUSH_PLUGIN";

	private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorPushWebservice.class);

	public DomibusConnectorPushWebservice() {
		super(PLUGIN_NAME);
	}

	@Autowired
	private DomibusConnectorGatewayDeliveryWebService deliveryClient;



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
			DomibsConnectorAcknowledgementType ack = deliveryClient.deliverMessage(message.getConnectorMessage());
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
