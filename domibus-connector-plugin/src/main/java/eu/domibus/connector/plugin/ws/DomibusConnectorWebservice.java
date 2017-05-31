package eu.domibus.connector.plugin.ws;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import connector.domibus.eu.domibusconnectorgatewayservice._1.AcknowledgementType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageErrorLogEntriesType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageErrorLogEntryType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessagesType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.ObjectFactory;
import connector.domibus.eu.domibusconnectorgatewayservice._1_0.DomibusConnectorGatewayServiceInterface;
import connector.domibus.eu.domibusconnectorgatewayservice._1_0.RequestPendingMessagesFault;
import connector.domibus.eu.domibusconnectorgatewayservice._1_0.SendMessageFault;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageStatus;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageRetrievalTransformer;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageSubmissionTransformer;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

public class DomibusConnectorWebservice extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> implements DomibusConnectorGatewayServiceInterface {

	private static final Log LOGGER = LogFactory.getLog(DomibusConnectorWebservice.class);

	public DomibusConnectorWebservice(String name) {
		super(name);
	}

	@Autowired
	private DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

	@Autowired
	private DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;

	private static final ObjectFactory of = new ObjectFactory();


	@Override
	public AcknowledgementType sendMessage(MessageType sendMessageRequest) throws SendMessageFault {

		String messageID = null;
		try {
			messageID = submit(new DomibusConnectorMessage(sendMessageRequest));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AcknowledgementType ack = new AcknowledgementType();
		if(messageID != null){
			ack.setMessageId(messageID);
			ack.setSuccess(true);
		}else{
			ack.setSuccess(false);
		}
		return ack;
	}


	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public MessagesType requestPendingMessages(String requestPendingMessagesRequest)
			throws RequestPendingMessagesFault {
		Collection<String> pendingMessages = listPendingMessages();

		if(!CollectionUtils.isEmpty(pendingMessages)){
			LOGGER.debug("Received "+pendingMessages.size()+" messages from Queue.");
			MessagesType messages = of.createMessagesType();
			for(String messageId: pendingMessages){
				LOGGER.debug("Download message "+messageId+" from Queue.");
				DomibusConnectorMessage message = new DomibusConnectorMessage(of.createMessageType());
				try {
					downloadMessage(messageId, message);
				} catch (MessageNotFoundException e) {
					LOGGER.error("Message with ID "+messageId+" not found!", e);
					continue;
				}

				if(isMessageValid(message)){

					LOGGER.debug("Successfully downloaded message "+messageId+" from Queue.");

					messages.getMessages().add(message.getConnectorMessage());
				}else{
					LOGGER.error("Message with ID "+messageId+" is not valid after download!");
				}
			}
			return messages;
		}
		return null;
	}

	private boolean isMessageValid(DomibusConnectorMessage message){
		return message.getConnectorMessage()!= null && message.getConnectorMessage().getMessageContent()!=null && message.getConnectorMessage().getMessageDetails()!=null;
	}

	@Override
	public String requestMessageStatus(String requestMessageStatusRequest) {
		MessageStatus status = getMessageStatus(requestMessageStatusRequest);
		return status.name();
	}

	@Override
	public MessageErrorLogEntriesType requestMessageErrors(String requestMessageErrorsRequest) {
		List<ErrorResult> errorsForMessage = getErrorsForMessage(requestMessageErrorsRequest);
		if(!CollectionUtils.isEmpty(errorsForMessage)){
			MessageErrorLogEntriesType messageErrors = of.createMessageErrorLogEntriesType();
			for(ErrorResult error: errorsForMessage){
				MessageErrorLogEntryType entry = of.createMessageErrorLogEntryType();
				entry.setErrorCode(error.getErrorCode().getErrorCodeName());
				entry.setErrorDetail(error.getErrorDetail());
				entry.setMessageInErrorId(error.getMessageInErrorId());
				entry.setNotified(dateToXMLGregorianCalendar(error.getNotified()));
				entry.setTimestamp(dateToXMLGregorianCalendar(error.getTimestamp()));

				messageErrors.getItem().add(entry);
			}

			return messageErrors;
		}
		return null;
	}


	private XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		XMLGregorianCalendar date2 = null;
		try {
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		return date2;
	}

	@Override
	public void messageSendFailed(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public MessageRetrievalTransformer<DomibusConnectorMessage> getMessageRetrievalTransformer() {
		return this.messageRetrievalTransformer;
	}

	@Override
	public MessageSubmissionTransformer<DomibusConnectorMessage> getMessageSubmissionTransformer() {
		return this.messageSubmissionTransformer;
	}



}
