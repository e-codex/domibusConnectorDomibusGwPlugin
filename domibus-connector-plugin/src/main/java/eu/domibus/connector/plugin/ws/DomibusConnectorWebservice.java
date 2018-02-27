package eu.domibus.connector.plugin.ws;

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
import eu.domibus.connector.ws.delivery.service.DomibusConnectorDeliveryWS;
import eu.domibus.connector.ws.submission.service.DomibusConnectorSubmissionWS;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

public class DomibusConnectorWebservice extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> implements DomibusConnectorSubmissionWS {

	private static final Log LOGGER = LogFactory.getLog(DomibusConnectorWebservice.class);

	public DomibusConnectorWebservice(String name) {
		super(name);
	}

	@Autowired
	private DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

	@Autowired
	private DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;
	
	@Autowired
	private DomibusConnectorDeliveryWS deliveryClient;

	private static final ObjectFactory of = new ObjectFactory();


	@Override
	public DomibsConnectorAcknowledgementType submitMessage(final DomibusConnectorMessageType submitMessageRequest) {

		String messageID = null;
		DomibsConnectorAcknowledgementType ack = new DomibsConnectorAcknowledgementType();
		try {
			messageID = submit(new DomibusConnectorMessage(submitMessageRequest));
		} catch (Exception e) {
			ack.setResult(false);
			ack.setResultMessage(e.getMessage());
			e.printStackTrace();
		}

		if(messageID != null){
			ack.setMessageId(messageID);
			ack.setResult(true);
		}
		return ack;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void deliverMessage(final String messageId) {
		LOGGER.debug("Download message "+messageId+" from Queue.");
		DomibusConnectorMessage message = new DomibusConnectorMessage(of.createDomibusConnectorMessageType());
		try {
			downloadMessage(messageId, message);
		} catch (MessageNotFoundException e) {
			LOGGER.error("Message with ID "+messageId+" not found!", e);
		}

		if(isMessageValid(message)){

			LOGGER.debug("Successfully downloaded message "+messageId+" from Queue.");

			DomibsConnectorAcknowledgementType ack = deliveryClient.deliverMessage(message.getConnectorMessage());
			if(ack.isResult()) {
				LOGGER.info("Successfully delivered message "+messageId+" to domibusConnector.");
			}else {
				LOGGER.error("Message with ID "+messageId+" not delivered successfully to domibusConnector: "+ack.getResultMessage());
			}
		}else{
			LOGGER.error("Message with ID "+messageId+" is not valid after download!");
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
	public void messageSendFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}

}
