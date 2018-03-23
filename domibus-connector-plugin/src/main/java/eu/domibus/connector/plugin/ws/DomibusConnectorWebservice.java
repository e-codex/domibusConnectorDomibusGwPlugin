package eu.domibus.connector.plugin.ws;

import eu.domibus.common.MessageReceiveFailureEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.ObjectFactory;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageRetrievalTransformer;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageSubmissionTransformer;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

public class DomibusConnectorWebservice extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> implements DomibusConnectorGatewaySubmissionWebService {

	private static final Log LOGGER = LogFactory.getLog(DomibusConnectorWebservice.class);

	public DomibusConnectorWebservice(String name) {
		super(name);
	}

	@Autowired
	private DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

	@Autowired
	private DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;
	
	@Autowired
	private DomibusConnectorGatewayDeliveryWebService deliveryClient;

	private static final ObjectFactory of = new ObjectFactory();


	@Override
	public DomibsConnectorAcknowledgementType submitMessage(final DomibusConnectorMessageType submitMessageRequest) {

		String messageID = null;
		DomibsConnectorAcknowledgementType ack = new DomibsConnectorAcknowledgementType();
		try {
			LOGGER.debug("#submitMessage: call submit of parent class");
			messageID = submit(new DomibusConnectorMessage(submitMessageRequest));
		} catch (Exception e) {
			ack.setResult(false);
			ack.setResultMessage(e.getMessage());
			LOGGER.error("#submitMessage: Error occured while calling submit, setting submit result to false", e);
		}

        LOGGER.info(String.format("#submitMessage: messageID is [%s]", messageID));
		if (messageID != null) {
            LOGGER.debug(String.format("#submitMessage: Message successfully submitted"));
			ack.setMessageId(messageID);
			ack.setResult(true);
		} else {
		    LOGGER.error("#submitMessage: submit message failed, returning ack with result false");
        }
		return ack;
	}
	
	@Override
	@Transactional //(propagation=Propagation.REQUIRES_NEW)
	public void deliverMessage(final String messageId) {
		LOGGER.debug("Download message "+messageId+" from Queue.");
		DomibusConnectorMessage message = new DomibusConnectorMessage(of.createDomibusConnectorMessageType());
		try {
			downloadMessage(messageId, message);
		} catch (MessageNotFoundException e) {
		    String error = String.format("Message with ID %s not found!", messageId);
			LOGGER.error(error, e);
			throw new RuntimeException(error, e);
		}

		if(isMessageValid(message)){

			LOGGER.debug("Successfully downloaded message "+messageId+" from Queue.");

			DomibsConnectorAcknowledgementType ack = deliveryClient.deliverMessage(message.getConnectorMessage());
			if(ack.isResult()) {
				LOGGER.info("Successfully delivered message "+messageId+" to domibusConnector.");
			}else {
			    String error = "Message with ID "+messageId+" not delivered successfully to domibusConnector: "+ack.getResultMessage();
				LOGGER.error(error);
				throw new RuntimeException(error);
			}
		}else{
			LOGGER.error("Message with ID "+messageId+" is not valid after download!");
            throw new RuntimeException("Message is not valid after download!");
		}
	}

	private boolean isMessageValid(DomibusConnectorMessage message){
		return message.getConnectorMessage()!= null && message.getConnectorMessage().getMessageContent()!=null && message.getConnectorMessage().getMessageDetails()!=null;
	}

	@Override
	public MessageRetrievalTransformer<DomibusConnectorMessage> getMessageRetrievalTransformer() {
		return this.messageRetrievalTransformer;
	}

	@Override
	public MessageSubmissionTransformer<DomibusConnectorMessage> getMessageSubmissionTransformer() {
		return this.messageSubmissionTransformer;
	}

	@Override
	public void messageSendSuccess(String messageId) {
		//just ignore...
		LOGGER.debug(String.format("Message with ID {%s} successfully Sent", messageId));
	}

	@Override
	public void messageSendFailed(String messageId) {
		LOGGER.error(String.format("Send message with messageId [%s] failed", messageId));
	}

    public void messageReceiveFailed(MessageReceiveFailureEvent receiveFailureEvent) {
        LOGGER.error(String.format("Message receiveFailed: messageId: [%s] on endpoint [%s] with ErrorResult [%s]",
                receiveFailureEvent.getMessageId(),
                receiveFailureEvent.getEndpoint(),
                receiveFailureEvent.getErrorResult()
                ));
    }

}
