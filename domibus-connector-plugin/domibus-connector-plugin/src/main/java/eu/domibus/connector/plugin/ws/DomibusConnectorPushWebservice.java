package eu.domibus.connector.plugin.ws;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.ObjectFactory;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageRetrievalTransformer;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageSubmissionTransformer;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DomibusConnectorPushWebservice extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> implements DomibusConnectorGatewaySubmissionWebService {

	public static final String PLUGIN_NAME = "DC_PUSH_PLUGIN";

	private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorPushWebservice.class);


	public DomibusConnectorPushWebservice() {
		super(PLUGIN_NAME);
		this.requiredNotifications = Stream
				.of(NotificationType.MESSAGE_RECEIVED)
				.collect(Collectors.toList());
	}

	@Autowired
	private DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

	@Autowired
	private DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;
	
	@Autowired
	private DomibusConnectorGatewayDeliveryWebService deliveryClient;

	private static final ObjectFactory objectFactory = new ObjectFactory();


	@Override
	public DomibsConnectorAcknowledgementType submitMessage(DomibusConnectorMessageType submitMessageRequest) {
		return new SubmitMessage(submitMessageRequest, this).invoke();
	}



	@Override
	@Transactional
	public void deliverMessage(final DeliverMessageEvent event) {
		String messageId = event.getMessageId();
		LOGGER.debug("Download message "+messageId+" from Queue.");
		DomibusConnectorMessage message = new DomibusConnectorMessage(objectFactory.createDomibusConnectorMessageType());
		try {
			downloadMessage(messageId, message);
		} catch (MessageNotFoundException e) {
		    String error = String.format("Message with ID %s not found!", messageId);
			LOGGER.error(error, e);
			throw new RuntimeException(error, e);
		}

		if(isMessageValid(message)){

			LOGGER.debug("Successfully downloaded message " + messageId + " from Queue.");
			try {
				DomibsConnectorAcknowledgementType ack = deliveryClient.deliverMessage(message.getConnectorMessage());
				if(ack.isResult()) {
					LOGGER.info("Successfully delivered message " + messageId + " to domibusConnector.");
				}else {
					String error = "Message with ID " + messageId + " not delivered successfully to domibusConnector: "+ack.getResultMessage();
					LOGGER.error(error);
					throw new RuntimeException(error);
				}
			} catch (Exception e) {
				LOGGER.error("Error occured while delivering message " + messageId + " to connector", e);
			}
		}else{
			LOGGER.error("Message with ID " + messageId + " is not valid after download!");
            throw new RuntimeException("Message with id " + messageId + " is not valid after download!");
		}
	}

	private boolean isMessageValid(DomibusConnectorMessage message) {
		DomibusConnectorMessageType msg = message.getConnectorMessage();
		if (msg == null) {
			LOGGER.error("Message is null!");
			return false;
		}
		if (msg.getMessageDetails() == null) {
			LOGGER.error("Message contains no Message Details!");
			return false;
		}
		if (msg.getMessageContent() != null) {
			LOGGER.info("Message is a business message");
			return true;
		}
		if (msg.getMessageContent() == null && msg.getMessageConfirmations().size() > 0) {
			LOGGER.info("Message is a confirmation message!");
			return true;
		}
		LOGGER.error("Message has neither a content or a confirmation - message is empty!");
		return false;
	}

	@Override
	public MessageRetrievalTransformer<DomibusConnectorMessage> getMessageRetrievalTransformer() {
		return this.messageRetrievalTransformer;
	}

	@Override
	public MessageSubmissionTransformer<DomibusConnectorMessage> getMessageSubmissionTransformer() {
		return this.messageSubmissionTransformer;
	}

    public void messageReceiveFailed(MessageReceiveFailureEvent receiveFailureEvent) {
        LOGGER.error(String.format("Message receiveFailed: messageId: [%s] on endpoint [%s] with ErrorResult [%s]",
                receiveFailureEvent.getMessageId(),
                receiveFailureEvent.getEndpoint(),
                receiveFailureEvent.getErrorResult()
                ));
    }


}
